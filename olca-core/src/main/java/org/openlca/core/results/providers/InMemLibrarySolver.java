package org.openlca.core.results.providers;

import java.util.ArrayDeque;
import java.util.HashSet;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.TechIndex;

/// A solver that loads all library data in to memory first and then uses a
/// standard solver to calculate the results.
public class InMemLibrarySolver {

	public static ResultProvider create(SolverContext ctx) {
		if (!ctx.hasLibraryLinks())
			return ResultProviders.solve(ctx);

		var dataF = ctx.data();
		var fullTechIdx = new TechIndex();

		var libQueue = new ArrayDeque<String>();
		for (var techFlow : dataF.techIndex) {
			fullTechIdx.add(techFlow);
			if (techFlow.isFromLibrary()) {
				libQueue.add(techFlow.library());
			}
		}
	}

	private final class Builder {

		private final SolverContext ctx;
		private final MatrixData origin;
		private final MatrixData full;

		Builder(SolverContext ctx) {
			this.ctx = ctx;
			this.origin = ctx.data();
			this.full = new MatrixData();
		}

		void build() {

			var procLibs = new HashSet<String>();
			for (var techFlow : origin.techIndex) {
				if (techFlow.isFromLibrary()) {
					procLibs.add(techFlow.library());
				}
			}
			if (!procLibs.isEmpty()) {
				fillInventory(procLibs);
			}

			// TODO: costs & LCIA factors

			if (full.techIndex == null) {
				full.techIndex = origin.techIndex;
			}

		}

		private void fillInventory(HashSet<String> libs) {
			var techBuffer = new MatrixBuilder();
			var enviBuffer = new MatrixBuilder();

			mapTechMatrix(origin.techIndex, origin.techMatrix, techBuffer);
			mapEnviMatrix(
					origin.enviIndex, origin.techIndex, origin.enviMatrix, enviBuffer);

			var queue = new ArrayDeque<>(libs);
			while (!queue.isEmpty()) {
				var libId = queue.poll();
				var lib = ctx.libraries().get(libId);
				var libTechIdx = lib.techIndex();
				var libA = lib.matrixOf(LibMatrix.A);
				mapTechMatrix(libTechIdx, libA, techBuffer);
				if (lib.hasEnviData()) {
					var libB = lib.matrixOf(LibMatrix.B);
					mapEnviMatrix(lib.enviIndex(), libTechIdx, libB, enviBuffer);
				}

				for (var techFlow : libTechIdx) {
					if (!libs.contains(techFlow.library())) {
						libs.add(techFlow.library());
						queue.add(techFlow.library());
					}
				}
			}
		}

		private void mapTechMatrix(
				TechIndex idx, MatrixReader m, MatrixBuilder buffer
		) {
			if (full.techIndex == null) {
				full.techIndex = new TechIndex();
			}
			var fullIdx = full.techIndex;
			fullIdx.addAll(idx);
			buffer.minSize(fullIdx.size(), fullIdx.size());

			m.iterate((row, col, val) -> {
				var rowFlow = idx.at(row);
				var colFlow = idx.at(col);
				var targetRow = fullIdx.of(rowFlow);
				var targetCol = fullIdx.of(colFlow);
				buffer.set(targetRow, targetCol, val);
			});
		}

		private void mapEnviMatrix(
				EnviIndex enviIdx,
				TechIndex techIndex,
				MatrixReader m,
				MatrixBuilder buffer
		) {
			if (enviIdx == null)
				return;
			if (full.enviIndex == null) {
				full.enviIndex = enviIdx.isRegionalized()
						? EnviIndex.createRegionalized()
						: EnviIndex.create();
			}
			var fullEnviIdx = full.enviIndex;
			fullEnviIdx.addAll(enviIdx);
			var fullTechIdx = full.techIndex;
			buffer.minSize(fullEnviIdx.size(), fullTechIdx.size());

			m.iterate((row, col, val) -> {
				var rowFlow = enviIdx.at(row);
				var colFlow = techIndex.at(col);
				var targetRow = fullEnviIdx.of(rowFlow);
				var targetCol = fullTechIdx.of(colFlow);
				buffer.set(targetRow, targetCol, val);
			});
		}
	}
}
