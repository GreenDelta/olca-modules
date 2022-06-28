package org.openlca.core.results.providers.libblocks;

import org.openlca.core.library.Library;
import org.openlca.core.matrix.index.TechIndex;
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

	final TechIndex index;
	final TechIndex front;
	final List<Block> blocks;
	final boolean isSparse;

	public static BlockTechIndex of(SolverContext context) {
		return new BlockTechIndex(context);
	}

	private BlockTechIndex(SolverContext context) {
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
			index.addAll(block.index);
			offset += block.size();
		}

		isSparse = areSparse(context, blocks);
	}

	private boolean areSparse(SolverContext context, List<Block> blocks) {
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

	int size() {
		return index.size();
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
}
