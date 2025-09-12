package org.openlca.core.results.providers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.list.array.TDoubleArrayList;

/// A solver that loads all library data in to memory first and then uses a
/// standard solver to calculate the results.
public class InMemLibrarySolver {

	public static ResultProvider solve(SolverContext ctx) {
		if (!ctx.hasLibraryLinks())
			return ResultProviders.solve(ctx);

		var fullData = new Builder(ctx).build();
		var fullCtx = SolverContext.of(ctx.db(), fullData)
				.withSolver(ctx.solver());
		var n = fullData.techIndex.size();
		var isSmall = ctx.solver().isNative()
				? n < 1000
				: n < 500;
		return isSmall || !fullData.isSparse()
				? InversionResult.of(fullCtx).calculate().provider()
				: FactorizationSolver.solve(fullCtx);
	}

	private static final class Builder {

		private final Logger log = LoggerFactory.getLogger(getClass());

		private final SolverContext ctx;
		private final MatrixData origin;
		private final MatrixData full;

		Builder(SolverContext ctx) {
			this.ctx = ctx;
			this.origin = ctx.data();
			full = new MatrixData();
			full.demand = origin.demand;
		}

		MatrixData build() {
			log.info("load all library data into memory");
			fillInventory();
			fillImpactModel();

			if (full.techIndex == null) {
				full.techIndex = origin.techIndex;
			}
			if (full.techMatrix == null) {
				full.techMatrix = origin.techMatrix;
			}

			if (full.enviIndex == null) {
				full.enviIndex = origin.enviIndex;
			}
			if (full.enviMatrix == null) {
				full.enviMatrix = origin.enviMatrix;
			}

			if (full.costVector == null) {
				full.costVector = origin.costVector;
			}

			if (full.impactIndex == null) {
				full.impactIndex = origin.impactIndex;
			}
			if (full.impactMatrix == null) {
				full.impactMatrix = origin.impactMatrix;
			}
			return full;
		}

		private void fillImpactModel() {
			if (MatrixIndex.isAbsent(origin.impactIndex)
					|| MatrixIndex.isAbsent(full.enviIndex))
				return;
			var impIdx = full.impactIndex = origin.impactIndex;
			var enviIdx = full.enviIndex;
			var impBuffer = new MatrixBuilder();
			impBuffer.minSize(impIdx.size(), enviIdx.size());

			// first, load impact factors for non-library impact categories
			// into the matrix
			var nonLibIndicators = new ArrayList<ImpactDescriptor>();
			for (var imp : impIdx) {
				if (!imp.isFromLibrary()) {
					nonLibIndicators.add(imp);
				}
			}

			if (!nonLibIndicators.isEmpty()) {
				var nonLibIdx = ImpactIndex.of(nonLibIndicators);
				var contexts = new HashSet<Long>();
				nonLibIdx.each((_idx, impact) -> contexts.add(impact.id));
				var interpreter = ParameterTable.interpreter(
						ctx.db(), contexts, Collections.emptyList());
				var nonLibMatrix = ImpactBuilder.of(ctx.db(), enviIdx)
						.withImpacts(nonLibIdx)
						.withInterpreter(interpreter)
						.build().impactMatrix;
				mapImpactMatrix(nonLibIdx,enviIdx, nonLibMatrix, impBuffer);
			}

			// collect impact factors for library impact categories
			var libs = new HashSet<String>();
			for (var imp : impIdx) {
				if (!imp.isFromLibrary() || libs.contains(imp.library))
					continue;
				libs.add(imp.library);
				var lib = ctx.libraries().get(imp.library);
				if (lib == null) {
					log.error("could not find library {}", imp.library);
					continue;
				}
				if (!lib.hasImpactData())
					continue;
				var libImpIdx = lib.impactIndex();
				var libEnviIdx = lib.enviIndex();
				var matrix = lib.matrixOf(LibMatrix.C);
				if (MatrixIndex.isAbsent(libImpIdx)
						|| MatrixIndex.isAbsent(libEnviIdx)
						|| matrix == null)
					continue;
				log.info("map impact model of library {}", imp.library);
				mapImpactMatrix(libImpIdx, libEnviIdx, matrix, impBuffer);
			}

			if (!impBuffer.isEmpty()) {
				full.impactMatrix = impBuffer.finish();
			}
		}

		private void mapImpactMatrix(
				ImpactIndex impIdx,
				EnviIndex enviIdx,
				MatrixReader m,
				MatrixBuilder buffer
		) {
			if (impIdx == null || enviIdx == null || m == null)
				return;

			var fullImpIdx = full.impactIndex;
			var fullEnvIdx = full.enviIndex;
			m.iterate((row, col, val) -> {
				var rowImp = impIdx.at(row);
				var colFlow = enviIdx.at(col);
				var targetRow = fullImpIdx.of(rowImp);
				var targetCol = fullEnvIdx.of(colFlow);
				buffer.set(targetRow, targetCol, val);
			});
		}

		private void fillInventory() {
			var libs = new HashSet<String>();
			for (var techFlow : origin.techIndex) {
				if (techFlow.isFromLibrary()) {
					libs.add(techFlow.library());
				}
			}
			if (libs.isEmpty())
				return;

			var techBuffer = new MatrixBuilder();
			var enviBuffer = new MatrixBuilder();
			var costBuffer = origin.costVector != null
					? new TDoubleArrayList(origin.techIndex.size())
					: null;

			mapTechMatrix(origin.techIndex, origin.techMatrix, techBuffer);
			mapEnviMatrix(
					origin.enviIndex, origin.techIndex, origin.enviMatrix, enviBuffer);
			if (costBuffer != null) {
				mapCosts(origin.techIndex, origin.costVector, costBuffer);
			}

			var queue = new ArrayDeque<>(libs);
			while (!queue.isEmpty()) {

				var libId = queue.poll();
				var lib = ctx.libraries().get(libId);
				if (lib == null) {
					log.error("could not find library {}", libId);
					continue;
				}
				var libTechIdx = lib.techIndex();
				if (MatrixIndex.isAbsent(libTechIdx))
					continue;

				log.info("map inventory of library {}", libId);
				var libA = lib.matrixOf(LibMatrix.A);
				mapTechMatrix(libTechIdx, libA, techBuffer);

				if (lib.hasEnviData()) {
					var libB = lib.matrixOf(LibMatrix.B);
					mapEnviMatrix(lib.enviIndex(), libTechIdx, libB, enviBuffer);
				}

				if (lib.hasCostData() && costBuffer != null) {
					mapCosts(libTechIdx, lib.costs(), costBuffer);
				}

				for (var techFlow : libTechIdx) {
					if (!libs.contains(techFlow.library())) {
						libs.add(techFlow.library());
						queue.add(techFlow.library());
					}
				}
			}

			if (!techBuffer.isEmpty()) {
				full.techMatrix = techBuffer.finish();
			}
			if (!enviBuffer.isEmpty()) {
				full.enviMatrix = enviBuffer.finish();
			}
			if (costBuffer != null && !costBuffer.isEmpty()) {
				full.costVector = costBuffer.toArray();
			}
		}

		private void mapTechMatrix(
				TechIndex idx, MatrixReader m, MatrixBuilder buffer
		) {
			if (idx == null || m == null)
				return;
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

			if (m == null || techIndex == null)
				return;
			m.iterate((row, col, val) -> {
				var rowFlow = enviIdx.at(row);
				var colFlow = techIndex.at(col);
				var targetRow = fullEnviIdx.of(rowFlow);
				var targetCol = fullTechIdx.of(colFlow);
				buffer.set(targetRow, targetCol, val);
			});
		}

		private void mapCosts(
				TechIndex idx, double[] costs, TDoubleArrayList buffer
		) {
			var fullIdx = full.techIndex;
			if (buffer.size() < fullIdx.size()) {
				buffer.ensureCapacity(fullIdx.size());
				for (int i = buffer.size(); i < fullIdx.size(); i++) {
					buffer.add(0);
				}
			}
			if (costs == null)
				return;
			for (var col = 0; col < costs.length; col++) {
				var techFlow = idx.at(col);
				var targetCol = fullIdx.of(techFlow);
				buffer.set(targetCol, costs[col]);
			}
		}
	}
}
