package org.openlca.core.results.providers.libinv;

import org.openlca.core.DataDir;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.providers.LibraryCache;
import org.openlca.core.results.providers.SolverContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class only works with a foreground system that is connected to one or
 * more process libraries. It represents then the combined technosphere as a
 * sequence of index blocks. The first block (the index front) contains only
 * the foreground processes. It is then followed by a sequence of index blocks
 * of the respective libraries in the system.
 */
public class BlockTechIndex {

	private final SolverContext context;

	private final TechIndex index;
	private final TechIndex front;
	private final List<Block> blocks;
	private final boolean isSparse;

	public static BlockTechIndex createFrom(SolverContext context) {
		return new BlockTechIndex(context);
	}

	private BlockTechIndex(SolverContext context) {
		this.context = context;

		// the index front with foreground processes
		index = new TechIndex();
		front = new TechIndex();
		var f = context.data();
		for (var techFlow : f.techIndex) {
			if (!techFlow.isFromLibrary()) {
				front.add(techFlow);
			}
		}
		index.addAll(front);

		// the library blocks
		blocks = new ArrayList<>();
		int offset = front.size();
		var techIndices = context.libraries().techIndicesOf(f.techIndex);
		for (var e : techIndices.entrySet()) {
			var block = new Block(e.getKey(), e.getValue(), offset);
			blocks.add(block);
			offset += block.size();
		}

		isSparse = areSparse(blocks);
	}

	private boolean areSparse(List<Block> blocks) {
		var libs = context.libraries();
		if (blocks.size() == 1)
			return blocks.get(0).isSparse(libs);
		double entries = 0;
		double total = 0;
		for (var block : blocks) {
			var libDir = libs.dir()
				.getLibrary(block.library)
				.map(Library::folder)
				.orElse(null);
			if (libDir == null)
				continue;
			total += block.size();
			var f = block.isSparse(libs) ? 0.25 : 0.75;
			entries += f * Math.pow(index.size(), 2.0);
		}
		return entries == 0
			|| total == 0
			|| (entries / Math.pow(total, 2.0)) < 0.4;
	}

	record Block(String library, TechIndex index, int offset) {

		int size() {
			return index.size();
		}

		private boolean isSparse(LibraryCache libs) {
			var lib = libs.dir().getLibrary(library).orElse(null);
			if (lib == null)
				return false;
			var npz = new File(lib.folder(), "A.npz");
			return npz.exists();
		}

		@Override
		public String toString() {
			return "Block { library: " + library + ", size: " + size() + "}";
		}
	}

	public static void main(String[] args) {
		var dir = DataDir.get();
		try (var db = dir.openDatabase("eiblock")) {
			var system = db.get(
				ProductSystem.class, "cce59bf1-09d5-405d-9b25-d709d914d6f6");
			var setup = CalculationSetup.fullAnalysis(system);
			var techIdx = TechIndex.of(db, setup);
			var data = MatrixData.of(db, techIdx)
				.withSetup(setup)
				.build();
			var context = SolverContext.of(db, data)
				.libraryDir(dir.getLibraryDir());
			var blockIdx = BlockTechIndex.createFrom(context);
			System.out.printf("front size: %d%n", blockIdx.front.size());
			System.out.printf("block 0: lib=%s%n", blockIdx.blocks.get(0));
			System.out.printf("block 0: sparse=%s%n", blockIdx.isSparse);
		}
	}

}
