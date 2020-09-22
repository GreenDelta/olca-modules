package org.openlca.core.library;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openlca.core.database.CategorizedEntityDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.matrix.io.npy.Npz;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.jsonld.Json;
import org.slf4j.LoggerFactory;

public class Library {

	/**
	 * The folder where the library files are stored.
	 */
	public final File folder;
	private LibraryInfo info;

	private final Map<LibraryMatrix, IMatrix> matrixCache = new HashMap<>();

	public Library(File folder) {
		this.folder = folder;
	}

	/**
	 * Creates a new library by writing the given matrix data into the given
	 * folder. It will try to derive the name and version of the library from
	 * the name of the given folder which should follow this pattern
	 * `<lib-name>_<version>`. Also, it will try to calculate the respective
	 * matrices with best available solver (so you should load any native
	 * libraries before calling this function).
	 */
	public static Library create(MatrixData data, File folder) {
		return MatrixDataExport.of(data, folder);
	}

	public LibraryInfo getInfo() {
		if (info != null)
			return info;
		var file = new File(folder, "library.json");
		var obj = Json.readObject(file);
		if (obj.isEmpty())
			throw new RuntimeException("failed to read " + file);
		info = LibraryInfo.fromJson(obj.get());
		return info;
	}

	/**
	 * Get the product index of this library.
	 */
	public Proto.ProductIndex getProductIndex() {
		var file = new File(folder, "index_A.bin");
		if (!file.exists())
			return Proto.ProductIndex.getDefaultInstance();
		try (var stream = new FileInputStream(file)) {
			return Proto.ProductIndex.parseFrom(stream);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to read product index from " + file, e);
			return Proto.ProductIndex.getDefaultInstance();
		}
	}

	/**
	 * Returns the products of the library in matrix order. If this library has
	 * no product index or if this index is not in sync with the database, an
	 * empty option is returned.
	 */
	public Optional<TechIndex> syncProducts(IDatabase db) {
		var processes = descriptors(new ProcessDao(db));
		var products = descriptors(new FlowDao(db));
		TechIndex index = null;
		var proto = getProductIndex();
		int size = proto.getProductCount();
		for (int i = 0; i < size; i++) {
			var entry = proto.getProduct(i);
			var process = processes.get(entry.getProcess().getId());
			var product = products.get(entry.getProduct().getId());
			if (process == null || product == null)
				return Optional.empty();
			if (index == null) {
				index = new TechIndex(ProcessProduct.of(process, product));
			} else {
				index.put(ProcessProduct.of(process, product));
			}
		}
		return Optional.ofNullable(index);
	}

	/**
	 * Get the elementary flow index of this library.
	 */
	public Proto.ElemFlowIndex getElemFlowIndex() {
		var file = new File(folder, "index_B.bin");
		if (!file.exists())
			return Proto.ElemFlowIndex.getDefaultInstance();
		try (var stream = new FileInputStream(file)) {
			return Proto.ElemFlowIndex.parseFrom(stream);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to read elem. flow index from " + file, e);
			return Proto.ElemFlowIndex.getDefaultInstance();
		}
	}

	/**
	 * Returns the elementary flows of the library in matrix order. If this
	 * information is not present or something went wrong while synchronizing
	 * the flow index with the database, an empty array is returned.
	 */
	public Optional<FlowIndex> syncElementaryFlows(IDatabase db) {
		var proto = getElemFlowIndex();
		if (proto.getFlowCount() == 0)
			return Optional.empty();

		var info = getInfo();
		var index = info.isRegionalized
				? FlowIndex.createRegionalized()
				: FlowIndex.create();

		var flows = descriptors(new FlowDao(db));
		var locations = descriptors(new LocationDao(db));
		int size = proto.getFlowCount();
		for (int i = 0; i < size; i++) {
			var entry = proto.getFlow(i);
			var flow = flows.get(entry.getFlow().getId());
			var location = locations.get(entry.getLocation().getId());
			if (flow == null)
				return Optional.empty();
			if (entry.getIsInput()) {
				index.putInput(flow, location);
			} else {
				index.putOutput(flow, location);
			}
		}
		return Optional.of(index);
	}

	private <T extends CategorizedDescriptor> Map<String, T> descriptors(
			CategorizedEntityDao<?, T> dao) {
		return dao.getDescriptors()
				.stream()
				.collect(Collectors.toMap(
						d -> d.refId,
						d -> d));
	}

	public boolean hasMatrix(LibraryMatrix m) {
		var npy = new File(folder, m.name() + ".npy");
		if (npy.exists())
			return true;
		var npz = new File(folder, m.name() + ".npz");
		return npz.exists();
	}

	public Optional<IMatrix> getMatrix(LibraryMatrix m) {
		var matrix = matrixCache.get(m);
		if (matrix != null)
			return Optional.of(matrix);

		try {
			var npy = new File(folder, m.name() + ".npy");
			if (npy.exists()) {
				matrix = Npy.load(npy);
				matrixCache.put(m, matrix);
				return Optional.of(matrix);
			}

			var npz = new File(folder, m.name() + ".npz");
			if (npz.exists()) {
				matrix = Npz.load(npz);
				matrixCache.put(m, matrix);
				return Optional.of(matrix);
			}

			return Optional.empty();

		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to read matrix from " + folder, e);
			return Optional.empty();
		}
	}

	public Optional<double[]> getColumn(LibraryMatrix m, int column) {
		var matrix = matrixCache.get(m);
		if (matrix != null)
			return Optional.of(matrix.getColumn(column));

		try {

			// do not cache dense matrices
			var npy = new File(folder, m.name() + ".npy");
			if (npy.exists())
				return Optional.of(Npy.loadColumn(npy, column));

			// force caching of sparse matrices
			matrix = getMatrix(m).orElse(null);
			return matrix == null
					? Optional.empty()
					: Optional.of(matrix.getColumn(column));

		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to read matrix column "
					+ column + " from " + m + " in " + folder, e);
			return Optional.empty();
		}
	}

	/**
	 * Get the diagonal of the given library matrix.
	 */
	public Optional<double[]> getDiagonal(LibraryMatrix m) {
		var matrix = matrixCache.get(m);
		if (matrix != null)
			return Optional.of(matrix.diag());

		try {
			// do not cache dense matrices
			var npy = new File(folder, m.name() + ".npy");
			if (npy.exists())
				return Optional.of(Npy.loadDiagonal(npy));

			// force caching of sparse matrices
			matrix = getMatrix(m).orElse(null);
			return matrix == null
					? Optional.empty()
					: Optional.of(matrix.diag());

		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to read matrix diagonal"
					+ " from " + m + " in " + folder, e);
			return Optional.empty();
		}
	}

	/**
	 * Creates a list of exchanges from the library matrices that describe the
	 * inputs and outputs of the given library product. The meta-data of the
	 * exchanges are synchronized with the given databases. Thus, this library
	 * needs to be mounted to the given database.
	 */
	public List<Exchange> getExchanges(ProcessProduct product, IDatabase db) {
		if (product == null || db == null)
			return Collections.emptyList();
		var products = syncProducts(db).orElse(null);
		if (products == null)
			return Collections.emptyList();

		// find the library index of the given product
		int index = products.getIndex(product);
		if (index < 0)
			return Collections.emptyList();

		// read the product inputs and outputs
		var exchanges = new ArrayList<Exchange>();
		var flowDao = new FlowDao(db);
		var colA = getColumn(LibraryMatrix.A, index).orElse(null);
		if (colA == null)
			return Collections.emptyList();
		for (int i = 0; i < colA.length; i++) {
			double val = colA[i];
			if (val == 0)
				continue;
			product = products.getProviderAt(i);
			var flow = flowDao.getForId(product.flowId());
			if (flow == null)
				continue;
			var exchange = val < 0
					? Exchange.input(flow, Math.abs(val))
					: Exchange.output(flow, val);
			if (i != index) {
				exchange.defaultProviderId = product.id();
			}
			exchanges.add(exchange);
		}

		// read the the elementary flow inputs and outputs
		var colB = getColumn(LibraryMatrix.B, index).orElse(null);
		if (colB == null)
			return exchanges;
		var iFlows = syncElementaryFlows(db).orElse(null);
		if (iFlows == null)
			return exchanges;

		var locDao = new LocationDao(db);
		for (int i = 0; i < colB.length; i++) {
			double val = colB[i];
			if (val == 0)
				continue;
			var iFlow = iFlows.at(i);
			if (iFlow == null)
				continue;
			var flow = flowDao.getForId(iFlow.flow.id);
			if (flow == null)
				continue;
			var exchange = iFlow.isInput
					? Exchange.input(flow, -val)
					: Exchange.output(flow, val);
			if (iFlow.location != null) {
				exchange.location = locDao.getForId(
						iFlow.location.id);
			}
			exchanges.add(exchange);
		}

		return exchanges;
	}
}
