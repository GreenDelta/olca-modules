package org.openlca.core.library.reader;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;

import java.util.Objects;

public class CachingLibReader implements LibReader {

	private final MatrixCache cache;
	private final LibReader reader;
	private TechIndex techIndex;
	private EnviIndex enviIndex;
	private ImpactIndex impactIndex;
	private double[] costs;

	private CachingLibReader(LibReader reader) {
		this.reader = Objects.requireNonNull(reader);
		this.cache = new MatrixCache();
	}

	public static CachingLibReader of(LibReader reader) {
		return new CachingLibReader(reader);
	}

	MatrixCache cache() {
		return cache;
	}

	@Override
	public Library library() {
		return reader.library();
	}

	@Override
	public TechIndex techIndex() {
		if (techIndex == null) {
			techIndex = reader.techIndex();
		}
		return techIndex;
	}

	@Override
	public EnviIndex enviIndex() {
		if (enviIndex == null) {
			enviIndex = reader.enviIndex();
		}
		return enviIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		if (impactIndex == null) {
			impactIndex = reader.impactIndex();
		}
		return impactIndex;
	}

	@Override
	public MatrixReader matrixOf(LibMatrix m) {
		var cached = cache.matrixOf(m);
		if (cached != null)
			return cached;
		var matrix = reader.matrixOf(m);
		if (matrix != null) {
			cache.put(m, matrix);
		}
		return matrix;
	}

	@Override
	public double[] costs() {
		if (costs == null) {
			costs = reader.costs();
		}
		return costs;
	}

	@Override
	public double[] diagonalOf(LibMatrix m) {
		var cached = cache.diagonalOf(m);
		if (cached != null)
			return cached;
		// read a full matrix if it is sparse
		var f = MatrixFile.of(library(), m);
		if (f.hasMatrix()) {
			cache.put(m, f.matrix());
			return f.matrix().diag();
		}
		var diag = reader.diagonalOf(m);
		cache.putDiagonal(m, diag);
		return diag;
	}

	@Override
	public double[] columnOf(LibMatrix m, int j) {
		var cached = cache.columnOf(m, j);
		if (cached != null)
			return cached;
		// read a full matrix if it is sparse
		var f = MatrixFile.of(library(), m);
		if (f.hasMatrix()) {
			cache.put(m, f.matrix());
			return f.matrix().getColumn(j);
		}
		var column = reader.columnOf(m, j);
		cache.putColumn(m, j, column);
		return column;
	}
}
