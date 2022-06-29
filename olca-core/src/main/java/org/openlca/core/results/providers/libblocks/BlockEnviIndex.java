package org.openlca.core.results.providers.libblocks;

import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.results.providers.SolverContext;
import org.openlca.core.results.providers.libblocks.BlockTechIndex.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The combined envi-index of a library block model. When there are libraries
 * with environmental interactions present, the first segment of this index is
 * identical to the index of the library with the largest size of the
 * intervention matrix so that the matrices {@code B} and {@code M} can be
 * directly copied as blocks to the corresponding result matrices in this case.
 */
class BlockEnviIndex {

	final EnviIndex index;
	final String frontLib;
	final Map<String, EnviIndex> libIndices;

	static BlockEnviIndex of(SolverContext context, BlockTechIndex techIdx) {
		return new BlockEnviIndex(context, techIdx);
	}

	private BlockEnviIndex(SolverContext context, BlockTechIndex techIdx) {

		var libs = context.libraries();
		var enviBlocks = new ArrayList<EnviBlock>();
		EnviBlock front = null;
		libIndices = new HashMap<>();
		for (var block : techIdx.blocks) {
			var enviIdx = libs.enviIndexOf(block.library());
			if (enviIdx == null)
				continue;
			var enviBlock = new EnviBlock(block, enviIdx);
			enviBlocks.add(enviBlock);
			libIndices.put(block.library(), enviIdx);
			if (front == null || enviBlock.size() > front.size()) {
				front = enviBlock;
			}
		}

		var f = context.data();
		index = front != null
			? front.index.copy()
			: f.enviIndex != null
			? f.enviIndex.copy()
			: null;

		for (var block : enviBlocks) {
			if (!Objects.equals(front, block)) {
				index.addAll(block.index);
			}
		}

		if (front == null) {
			frontLib = null;
		} else {
			frontLib = front.block.library();
			if (f.enviIndex != null) {
				index.addAll(f.enviIndex);
			}
		}
	}

	int size() {
		return index.size();
	}

	boolean isEmpty() {
		return index == null || index.isEmpty();
	}

	boolean isFront(String library) {
		return Objects.equals(library, frontLib);
	}

	boolean contains(String library) {
		return libIndices.containsKey(library);
	}

	int[] map(String library) {
		var libIdx = libIndices.get(library);
		return libIdx.mapTo(index);
	}

	private record EnviBlock(Block block, EnviIndex index) {

		int size() {
			return block.size() * index.size();
		}
	}
}
