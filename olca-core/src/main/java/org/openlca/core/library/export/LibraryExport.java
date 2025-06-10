package org.openlca.core.library.export;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryInfo;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.MatrixConfig;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.io.MatrixExport;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.core.matrix.io.index.IxContext;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.matrix.io.index.IxImpactIndex;
import org.openlca.core.matrix.io.index.IxTechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.npy.Npy;
import org.openlca.npy.NpyDoubleArray;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	final IDatabase db;
	final File folder;
	final LibraryInfo info;

	private AllocationMethod allocation;
	private boolean withCosts;
	private boolean withUncertainties;
	private MatrixData data;
	private boolean withInversion;

	public LibraryExport(IDatabase db, File folder) {
		this.db = db;
		this.folder = folder;
		this.info = LibraryInfo.of(folder.getName());
	}

	public LibraryExport withAllocation(AllocationMethod method) {
		this.allocation = method;
		return this;
	}

	public LibraryExport withConfig(LibraryInfo info) {
		if (info == null)
			return this;
		this.info.name(info.name())
				.isRegionalized(info.isRegionalized())
				.description(info.description());
		this.info.dependencies().clear();
		this.info.dependencies().addAll(info.dependencies());
		return this;
	}

	/// Uses the given matrix data for the export. Note that this will modify
	/// the matrix data for processes that are not scaled to 1 unit of product
	/// output or waste input.
	public LibraryExport withData(MatrixData data) {
		if (data == null)
			return this;
		this.data = data;
		withCosts = data.costVector != null;
		withUncertainties = data.techUncertainties != null
				|| data.enviUncertainties != null
				|| data.impactUncertainties != null;
		info.isRegionalized(
				data.enviIndex != null && data.enviIndex.isRegionalized());
		return this;
	}

	public LibraryExport withCosts(boolean b) {
		withCosts = b;
		return this;
	}

	/**
	 * If set to {@code true}, the export will calculate and store the inverse
	 * of the technology matrix as well as the inventory intensities if the
	 * respective matrices are available.
	 */
	public LibraryExport withInversion(boolean b) {
		this.withInversion = b;
		return this;
	}

	public LibraryExport withUncertainties(boolean b) {
		this.withUncertainties = b;
		return this;
	}

	@Override
	public void run() {
		log.info("start library export of database {}", db.getName());
		var lib = Library.of(folder);

		buildMatrices();
		var scaler = Scaler.scale(data);

		var exec = Executors.newFixedThreadPool(4);
		exec.execute(new JsonWriter(this, scaler));
		writeMatrices(lib, exec);

		try {
			exec.shutdown();
			while (true) {
				var finished = exec.awaitTermination(1, TimeUnit.HOURS);
				if (finished)
					break;
			}
			// finally write the library info, note that information like library
			// dependencies are collected during the export, so this should be the
			// last step
			info.writeTo(lib);
		} catch (Exception e) {
			throw new RuntimeException("failed to wait for export to finish", e);
		}
	}

	private void writeMatrices(Library lib, ExecutorService exec) {
		exec.execute(() -> {
			log.info("write matrices");
			MatrixExport.toNpy(db, folder, data).writeMatrices();
			log.info("write indices");
			var ctx = IxContext.of(db);
			var target = lib.folder();
			if (data.techIndex != null) {
				IxTechIndex.of(data.techIndex, ctx).writeToDir(target);
			}
			if (data.enviIndex != null) {
				IxEnviIndex.of(data.enviIndex, ctx).writeToDir(target);
			}
			if (data.impactIndex != null) {
				IxImpactIndex.of(data.impactIndex).writeToDir(target);
			}
		});

		if (withInversion) {
			exec.execute(this::createInverse);
		}
	}

	private void buildMatrices() {
		if (data != null)
			return;

		// build inventory if possible
		// This makes no sense if the processes in the database are linked to
		// another library, but this should be caught before running an export.
		// Calculation dependencies between libraries are not allowed.
		var techIdx = TechIndex.of(db);
		if (!techIdx.isEmpty()) {
			log.info("build inventory of {} processes", techIdx.size());
			data = MatrixConfig.of(db, techIdx)
					.withCosts(withCosts)
					.withUncertainties(withUncertainties)
					.withRegionalization(info.isRegionalized())
					.withAllocation(allocation)
					.build();
		}

		// here we filter out LCIA categories from other libraries; this is fine
		var impacts = new ImpactCategoryDao(db).getDescriptors()
				.stream()
				.filter(d -> Strings.nullOrEmpty(d.dataPackage))
				.toList();
		if (impacts.isEmpty())
			return;

		// create the impact index
		var impactIdx = ImpactIndex.of(impacts);
		if (data == null) {
			data = new MatrixData();
		}
		data.impactIndex = impactIdx;

		// make sure all elementary flows of the impacts are in the envi index
		var impactEnviIdx = info.isRegionalized()
				? EnviIndex.createRegionalized(db, impactIdx)
				: EnviIndex.create(db, impactIdx);
		if (data.enviIndex == null) {
			data.enviIndex = impactEnviIdx;
		} else {
			data.enviIndex.addAll(impactEnviIdx);
		}

		// when there is a technosphere matrix and an intervention index
		// but no intervention matrix, we create an empty matrix B
		if (data.techMatrix != null && !data.enviIndex.isEmpty()) {
			if (data.enviMatrix == null) {
				data.enviMatrix = new HashPointMatrix(
						data.enviIndex.size(), data.techIndex.size());
			} else {
				data.enviMatrix = MatrixShape.ensureIfPresent(data.enviMatrix,
						data.enviIndex.size(), data.techIndex.size());
			}
		}

		// build the
		ImpactBuilder.of(db, data.enviIndex)
				.withUncertainties(withUncertainties)
				.build()
				.addTo(data);
	}

	private void createInverse() {
		if (data.techMatrix == null)
			return;

		var solver = MatrixSolver.get();
		if (!solver.isNative()) {
			log.warn("no native libraries loaded");
		}
		log.info("create matrix INV");
		var inv = solver.invert(data.techMatrix);
		NpyMatrix.write(folder, "INV", inv);

		if (data.enviMatrix != null) {
			log.info("create matrix M");
			var m = solver.multiply(data.enviMatrix, inv);
			NpyMatrix.write(folder, "M", m);
		}

		if (data.costVector != null) {
			log.info("create vector costs_i");
			int n = data.costVector.length;
			var costs = new DenseMatrix(1, n, data.costVector);
			var costsI = solver.multiply(costs, inv);
			var data = costsI instanceof DenseMatrix dense
					? dense.data
					: costsI.getRow(0);
			var array = new NpyDoubleArray(new int[]{n}, data, false);
			var file = new File(folder, "costs_i.npy");
			Npy.write(file, array);
		}
	}
}
