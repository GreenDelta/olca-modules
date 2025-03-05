package org.openlca.core.results.providers.libblocks;

import org.openlca.core.library.reader.LibReader;
import org.openlca.core.matrix.index.TechIndex;
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
			if (!context.libraries().dataPackages.isLibrary(techFlow.dataPackage())) {
				front.add(techFlow);
			}
		}
		index.addAll(front);

		// the library blocks
		blocks = new ArrayList<>();
		int offset = front.size();
		for (var reader : context.libraries().readers()) {
			var techIdx = reader.techIndex();
			if (techIdx == null)
				continue;
			var block = new Block(reader, techIdx, offset);
			blocks.add(block);
			index.addAll(block.index);
			offset += block.size();
		}

		isSparse = areSparse(blocks);
	}

	private boolean areSparse(List<Block> blocks) {
		if (blocks.size() == 1)
			return blocks.get(0).isSparse();
		double entries = 0;
		double total = 0;
		for (var block : blocks) {
			var libDir = block.reader.library().folder();
			if (libDir == null)
				continue;
			total += block.size();
			var f = block.isSparse() ? 0.25 : 0.75;
			entries += f * Math.pow(index.size(), 2.0);
		}
		return entries == 0
				|| total == 0
				|| (entries / Math.pow(total, 2.0)) < 0.4;
	}

	int size() {
		return index.size();
	}

	record Block(LibReader reader, TechIndex index, int offset) {

		String id() {
			return reader.libraryName();
		}

		int size() {
			return index.size();
		}

		private boolean isSparse() {
			var libDir = reader.library().folder();
			if (libDir == null)
				return false;
			var npz = new File(libDir, "A.npz");
			return npz.exists();
		}

		@Override
		public String toString() {
			return "Block { library: " + reader.libraryName()
					+ ", size: " + size() + "}";
		}
	}
}
