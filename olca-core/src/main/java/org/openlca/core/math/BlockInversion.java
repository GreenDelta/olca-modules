package org.openlca.core.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BlockInversion {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final int MAX_BLOCK_SIZE;
	private final IMatrixFactory<?> factory;
	private final ISolver solver;

	public BlockInversion(int blockSize, ISolver solver) {
		this.MAX_BLOCK_SIZE = blockSize;
		this.solver = solver;
		this.factory = solver.getMatrixFactory();
	}

	public BlockInversion(ISolver solver) {
		this(2500, solver);
	}

	public IMatrix run(IMatrix a) {
		if (a.getColumnDimension() <= MAX_BLOCK_SIZE) {
			log.trace("Given matrix is smaller than max. block size "
					+ "-> calculate normal inverse");
			return solver.invert(a);
		}

		int dim = a.getRowDimension();
		int blocks = dim / MAX_BLOCK_SIZE + 1;
		int blockSize = dim / blocks;
		log.trace("Calculate inverse in blocks fow a {0}x{0} matrix; "
				+ "block size = {1}", dim, blockSize);

		IMatrix inverse = factory.create(dim, dim);

		int blockStart = 0;
		int blockEnd = blockSize;
		int blockCol = 0;
		IMatrix currentBlock = factory.create(dim, blockSize);
		for (int col = 0; col < dim; col++) {
			if (col == blockEnd) {
				solveBlock(a, currentBlock, blockStart, blockEnd, inverse);
				blockStart = col;
				blockCol = 0;
				if (col + blockSize >= dim)
					blockEnd = dim;
				else
					blockEnd = col + blockSize;
				currentBlock = factory.create(dim, blockEnd - col);
			}
			currentBlock.setEntry(col, blockCol, 1);
			blockCol++;
		}
		solveBlock(a, currentBlock, blockStart, blockEnd, inverse);
		return inverse;
	}

	private void solveBlock(IMatrix a, IMatrix block, int blockStart,
			int blockEnd, IMatrix inverse) {
		IMatrix inversePart = solver.solve(a, block);
		int partCol = 0;
		for (int col = blockStart; col < blockEnd; col++) {
			for (int row = 0; row < a.getRowDimension(); row++) {
				double val = inversePart.getEntry(row, partCol);
				inverse.setEntry(row, col, val);
			}
			partCol++;
		}
	}
}
