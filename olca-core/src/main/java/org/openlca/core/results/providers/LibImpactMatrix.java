package org.openlca.core.results.providers;

import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.IndexedMatrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class LibImpactMatrix {

	private final ImpactIndex impactIndex;
	private final EnviIndex flowIndex;
	private Map<String, EnviIndex> libFlowIndices;

	private LibImpactMatrix(ImpactIndex impacts, EnviIndex flows) {
		this.impactIndex = impacts;
		this.flowIndex = flows;
	}

	public static LibImpactMatrix of(ImpactIndex impacts, EnviIndex flows) {
		return new LibImpactMatrix(impacts, flows);
	}

	public LibImpactMatrix withLibraryEnviIndices(
		Map<String, EnviIndex> indices) {
		this.libFlowIndices = indices;
		return this;
	}

	public MatrixReader build(IDatabase db, LibraryDir libDir) {

		// collect the used libraries
		var libs = new HashSet<String>();
		impactIndex.each((_i, impact) -> {
			if (impact.library != null) {
				libs.add(impact.library);
			}
		});

		// collect the factors from the database
		var dbFactors = fromDB(db);
		if (dbFactors != null && libs.isEmpty())
			return dbFactors.data();

		var builder = IndexedMatrix.build(
			impactIndex, flowIndex);
		if (dbFactors != null) {
			builder.put(dbFactors);
		}

		// collect and add the library factors
		for (var libID : libs) {
			var lib = libDir.getLibrary(libID).orElse(null);
			if (lib == null)
				continue;

			// load the matrix and impact index
			var libMatrix = lib.getMatrix(LibMatrix.C);
			if (libMatrix.isEmpty())
				continue;
			var libImpacts = lib.syncImpactIndex(db);
			if (libImpacts.isEmpty())
				continue;

			// load the library flows
			EnviIndex libFlows = libFlowIndices != null
				?  libFlowIndices.get(libID)
				: null;
			if (libFlows == null) {
				libFlows = lib.syncEnviIndex(db)
					.orElse(null);
			}
			if (libFlows == null)
				continue;

			// add the matrix
			builder.put(IndexedMatrix.of(
				libImpacts.get(), libFlows, libMatrix.get()));
		}

		return builder.finish().data();
	}

	private IndexedMatrix<ImpactDescriptor, EnviFlow> fromDB(IDatabase db) {
		var dbIdx = new ImpactIndex();
		impactIndex.each((i, impact) -> {
			if (!impact.isFromLibrary()) {
				dbIdx.add(impact);
			}
		});
		if (dbIdx.isEmpty())
			return null;
		var matrix = ImpactBuilder.of(db, flowIndex)
			.withImpacts(dbIdx)
			.build()
			.impactMatrix;
		return IndexedMatrix.of(dbIdx, flowIndex, matrix);
	}
}
