package org.openlca.core.matrix.matlib;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.FlowTypeTable;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Exports a matrix into the openLCA matrix-library (=matlib) format.The matlib
 * format is still experimental so this may change in future.
 * 
 * TODO: unit conversion; allocation factors
 */
public class MatlibExport implements Runnable {

	private final IDatabase db;
	private final File dir;
	private final IMatrixSolver solver;

	public boolean withResults = false;

	private ProcessTable processTable;
	private TechIndex techIndex;
	private LongIndex flowIndex;
	private IMatrix techMatrix;
	private IMatrix enviMatrix;

	public MatlibExport(IDatabase db, IMatrixSolver solver, File dir) {
		this.db = db;
		this.dir = dir;
		this.solver = solver;
	}

	@Override
	public void run() {
		try {
			if (!dir.exists())
				dir.mkdirs();
			initMatrices();
			fillMatrices();
			writeMetaData();
			writeMatrices();
		} catch (Exception e) {
			throw new RuntimeException("MatlibExport failed", e);
		}
	}

	private void fillMatrices() throws SQLException {
		String query = "select f_owner, f_flow, is_input, "
				+ "resulting_amount_value from tbl_exchanges";
		NativeSql.on(db).query(query, r -> {
			long processId = r.getLong(1);
			long flowId = r.getLong(2);
			boolean isInput = r.getBoolean(3);
			double amount = r.getDouble(4);
			if (flowIndex.contains(flowId))
				setEnvi(processId, flowId, amount);
			else
				setTech(processId, flowId, isInput, amount);
			return true;
		});
	}

	private void setTech(long processId, long flowId, boolean isInput, double amount) {
		if (!isInput) {
			LongPair p = LongPair.of(processId, flowId);
			int idx = techIndex.getIndex(p);
			if (idx < 0)
				return;
			techMatrix.set(idx, idx, amount);
			return;
		}
		long[] providers = processTable.getProviders(flowId);
		for (long provider : providers) {
			LongPair prov = LongPair.of(provider, flowId);
			int row = techIndex.getIndex(prov);
			if (row < 0)
				continue;
			for (LongPair recipient : techIndex.getProviders(processId)) {
				int col = techIndex.getIndex(recipient);
				if (col < 0)
					continue;
				techMatrix.set(row, col, -amount);
			}
			break;
		}
	}

	private void setEnvi(long processId, long flowId, double amount) {
		for (LongPair p : techIndex.getProviders(processId)) {
			int row = flowIndex.getIndex(flowId);
			int col = techIndex.getIndex(p);
			if (row < 0 || col < 0)
				continue;
			enviMatrix.set(row, col, amount);
		}
	}

	private void initMatrices() {
		FlowTypeTable flowTypes = FlowTypeTable.create(db);
		processTable = ProcessTable.create(db, flowTypes);
		List<LongPair> products = processTable.getProviderFlows();
		techIndex = new TechIndex(products.get(0));
		flowIndex = new LongIndex();
		Set<Long> productIds = new HashSet<>();
		for (int i = 0; i < products.size(); i++) {
			LongPair product = products.get(i);
			techIndex.put(products.get(i));
			productIds.add(product.getSecond());
		}
		for (long flowId : flowTypes.getFlowIds()) {
			if (productIds.contains(flowId))
				continue;
			flowIndex.put(flowId);
		}
		int n = techIndex.size();
		int k = flowIndex.size();
		techMatrix = solver.matrix(n, n);
		enviMatrix = solver.matrix(k, n);
	}

	private void writeMatrices() throws Exception {
		writeMatrix(techMatrix, new File(dir, "A.bin"));
		writeMatrix(enviMatrix, new File(dir, "B.bin"));
		if (withResults) {
			IMatrix invA = solver.invert(techMatrix);
			writeMatrix(invA, new File(dir, "Ainv.bin"));
			IMatrix m = solver.multiply(enviMatrix, invA);
			writeMatrix(m, new File(dir, "M.bin"));
		}
	}

	private void writeMatrix(IMatrix m, File file) throws Exception {
		try (FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream buffer = new BufferedOutputStream(fos)) {

			// byte buffers for int and double
			ByteBuffer i32 = ByteBuffer.allocate(4);
			ByteBuffer f64 = ByteBuffer.allocate(8);
			i32.order(ByteOrder.LITTLE_ENDIAN);
			f64.order(ByteOrder.LITTLE_ENDIAN);

			// rows + columns
			i32.putInt(m.rows());
			buffer.write(i32.array());
			i32.clear();
			i32.putInt(m.columns());
			buffer.write(i32.array());

			// values
			for (int col = 0; col < m.columns(); col++) {
				for (int row = 0; row < m.rows(); row++) {
					f64.putDouble(m.get(row, col));
					buffer.write(f64.array());
					f64.clear();
				}
			}
		}
	}

	private void writeMetaData() throws Exception {
		Map<Long, FlowDescriptor> flows = getDescriptors(new FlowDao(db));
		writeTechIndex(flows);
		List<String> indexB = new ArrayList<>(flowIndex.size() + 1);
		indexB.add("index;flow");
		for (int i = 0; i < flowIndex.size(); i++) {
			StringBuilder builder = new StringBuilder();
			builder.append(i).append(";\"");
			FlowDescriptor flow = flows.get(flowIndex.getKeyAt(i));
			if (flow != null)
				builder.append(flow.getName());
			builder.append("\"");
			indexB.add(builder.toString());
		}
		writeIndex(indexB, new File(dir, "index_B.csv"));
	}

	private void writeTechIndex(Map<Long, FlowDescriptor> flows) throws Exception {
		Map<Long, ProcessDescriptor> procs = getDescriptors(new ProcessDao(db));
		Map<Long, LocationDescriptor> locs = getDescriptors(new LocationDao(db));
		List<String> indexA = new ArrayList<>(techIndex.size() + 1);
		indexA.add("index;process;location;flow");
		for (int i = 0; i < techIndex.size(); i++) {
			StringBuilder builder = new StringBuilder();
			builder.append(i).append(";\"");
			LongPair product = techIndex.getProviderAt(i);
			ProcessDescriptor proc = procs.get(product.getFirst());
			if (proc != null)
				builder.append(proc.getName());
			builder.append("\";\"");
			if (proc != null && proc.getLocation() != null) {
				LocationDescriptor loc = locs.get(proc.getLocation());
				if (loc != null)
					builder.append(loc.getName());
			}
			builder.append("\";\"");
			FlowDescriptor flow = flows.get(product.getSecond());
			if (flow != null)
				builder.append(flow.getName());
			builder.append("\"");
			indexA.add(builder.toString());
		}
		writeIndex(indexA, new File(dir, "index_A.csv"));
	}

	private void writeIndex(List<String> lines, File file) throws Exception {
		try (FileOutputStream fos = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
				BufferedWriter buffer = new BufferedWriter(writer)) {
			for (String line : lines) {
				buffer.write(line);
				buffer.newLine();
			}
		}
	}

	private <T extends BaseDescriptor> Map<Long, T> getDescriptors(
			RootEntityDao<?, T> dao) {
		Map<Long, T> map = new HashMap<>();
		for (T d : dao.getDescriptors()) {
			map.put(d.getId(), d);
		}
		return map;
	}
}
