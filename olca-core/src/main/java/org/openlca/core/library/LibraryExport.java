package org.openlca.core.library;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.MatrixConfig;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.io.MatrixExport;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.core.matrix.io.index.IxContext;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.matrix.io.index.IxImpactIndex;
import org.openlca.core.matrix.io.index.IxTechIndex;
import org.openlca.core.matrix.solvers.NativeSolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.nativelib.NativeLib;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	final IDatabase db;
	final File folder;
	final LibraryInfo info;

	private AllocationMethod allocation;
	private boolean withUncertainties;
	private MatrixData data;

	public LibraryExport(IDatabase db, File folder) {
		this.db = db;
		this.folder = folder;
		this.info = LibraryInfo.of(db.getName());
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

	public LibraryExport withData(MatrixData data) {
		if (data == null)
			return this;
		this.data = data;
		withUncertainties = data.techUncertainties != null
			|| data.enviUncertainties != null
			|| data.impactUncertainties != null;
		info.isRegionalized(
			data.enviIndex != null && data.enviIndex.isRegionalized());
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
		// create a thread pool and start writing the meta-data
		var exec = Executors.newFixedThreadPool(4);
		exec.execute(new MetaDataExport(this));
		createMatrices(lib, exec);
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

	private void createMatrices(Library lib, ExecutorService exec) {
		buildMatrices();
		if (data == null)
			return;
		normalizeInventory();
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
		exec.execute(this::preCalculate);
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
			data = MatrixConfig.of(db, techIdx)
				.withUncertainties(withUncertainties)
				.withRegionalization(info.isRegionalized())
				.withAllocation(allocation)
				.build();
		}

		// here we filter out LCIA categories from other libraries; this is fine
		var impacts = new ImpactCategoryDao(db).getDescriptors()
			.stream()
			.filter(d -> Strings.nullOrEmpty(d.library))
			.toList();
		if (impacts.isEmpty())
			return;

		var impactIdx = ImpactIndex.of(impacts);
		if (data == null) {
			data = new MatrixData();
		}
		data.impactIndex = impactIdx;
		if (data.enviIndex == null) {
			data.enviIndex = info.isRegionalized()
				? EnviIndex.createRegionalized(db, impactIdx)
				: EnviIndex.create(db, impactIdx);
		}
		ImpactBuilder.of(db, data.enviIndex)
			.withUncertainties(withUncertainties)
			.build()
			.addTo(data);
	}

	/**
	 * Normalize the columns of the technology matrix A and intervention matrix B to one
	 * unit of output or input for each product or waste flow.
	 */
	private void normalizeInventory() {
		if (data == null || data.techMatrix == null)
			return;
		log.info("normalize matrices to 1 | -1");
		var matrixA = data.techMatrix.asMutable();
		data.techMatrix = matrixA;
		var matrixB = data.enviMatrix != null
			? data.enviMatrix.asMutable()
			: null;
		data.enviMatrix = matrixB;
		int n = matrixA.columns();
		for (int j = 0; j < n; j++) {
			double f = Math.abs(matrixA.get(j, j));
			if (f == 1)
				continue;

			// normalize column j in matrix A
			for (int i = 0; i < n; i++) {
				double val = matrixA.get(i, j);
				if (val == 0)
					continue;
				matrixA.set(i, j, val / f);
			}

			// normalize column j in matrix B
			if (matrixB == null)
				continue;
			int m = matrixB.rows();
			for (int i = 0; i < m; i++) {
				double val = matrixB.get(i, j);
				if (val == 0)
					continue;
				matrixB.set(i, j, val / f);
			}
		}
	}

	private void preCalculate() {
		if (!NativeLib.isLoaded()) {
			log.info("no native libraries loaded; skip matrix inversion");
			return;
		}
		log.info("create matrix INV");
		var solver = new NativeSolver();
		var inv = solver.invert(data.techMatrix);
		NpyMatrix.write(folder, "INV", inv);
		if (data.enviMatrix == null)
			return;
		log.info("create matrix M");
		var m = solver.multiply(data.enviMatrix, inv);
		NpyMatrix.write(folder, "M", m);
	}
}
