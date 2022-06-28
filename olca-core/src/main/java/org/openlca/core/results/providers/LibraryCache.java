package org.openlca.core.results.providers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.npy.Array2d;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.util.Strings;

public class LibraryCache {

	private final IDatabase db;
	private final LibraryDir dir;

	private final HashMap<String, TechIndex> techIndices = new HashMap<>();
	private final HashMap<String, EnviIndex> enviIndices = new HashMap<>();
	private final HashMap<String, ImpactIndex> impactIndices = new HashMap<>();

	private final HashMap<String, MatrixReader> matrices = new HashMap<>();
	private final HashMap<String, TIntObjectHashMap<double[]>> columns = new HashMap<>();
	private final HashMap<String, double[]> diagonals = new HashMap<>();
	private final HashMap<String, Optional<double[]>> costVectors = new HashMap<>();

	public LibraryCache(LibraryDir dir, IDatabase db) {
		this.dir = dir;
		this.db = db;
	}

	public LibraryDir dir() {
		return dir;
	}

	/**
	 * Collects and synchronizes the linked technosphere indices of the given
	 * foreground index.
	 *
	 * @param idx the foreground index with possible links to library processes
	 * @return a map of the linked library indices
	 */
	public Map<String, TechIndex> techIndicesOf(TechIndex idx) {
		var map = new HashMap<String, TechIndex>();
		for (var techFlow : idx) {
			var libId = techFlow.library();
			if (Strings.nullOrEmpty(libId))
				continue;
			map.computeIfAbsent(libId, this::techIndexOf);
		}
		return map;
	}

	public TechIndex techIndexOf(String libId) {
		return techIndices.computeIfAbsent(libId, id -> {
			var lib = dir.getLibrary(id);
			if (lib.isEmpty())
				return null;
			var index = lib.get().syncTechIndex(db);
			return index.orElse(null);
		});
	}

	public EnviIndex enviIndexOf(String libId) {
		return enviIndices.computeIfAbsent(libId, id -> {
			var lib = dir.getLibrary(id);
			if (lib.isEmpty())
				return null;
			var index = lib.get().syncEnviIndex(db);
			return index.orElse(null);
		});
	}

	public ImpactIndex impactIndexOf(String libId) {
		return impactIndices.computeIfAbsent(libId, id -> {
			var lib = dir.getLibrary(id);
			if (lib.isEmpty())
				return null;
			var index = lib.get().syncImpactIndex(db);
			return index.orElse(null);
		});
	}

	public MatrixReader matrixOf(String libId, LibMatrix matrix) {
		return matrices.computeIfAbsent(keyOf(libId, matrix), key -> {
			var lib = dir.getLibrary(libId).orElse(null);
			if (lib == null)
				return null;
			return lib.getMatrix(matrix).orElse(null);
		});
	}

	public Optional<double[]> costsOf(String libId) {
		return costVectors.computeIfAbsent(libId, key -> {
			var lib = dir.getLibrary(libId).orElse(null);
			return lib == null
				? Optional.empty()
				: lib.getCosts();
		});
	}

	public double[] diagonalOf(String libId, LibMatrix matrix) {
		var key = keyOf(libId, matrix);
		var diag = diagonals.get(key);
		if (diag != null)
			return diag;

		var fullMatrix = matrices.get(key);
		if (fullMatrix != null) {
			diag = fullMatrix.diag();
			diagonals.put(key, diag);
			return diag;
		}

		var file = MatrixFile.of(dir, libId, matrix);
		if (file.isEmpty())
			return new double[0];

		if (file.isSparse()) {
			fullMatrix = file.readFull();
			if (fullMatrix == null)
				return new double[0];
			matrices.put(key, fullMatrix);
			diag = fullMatrix.diag();
			diagonals.put(key, diag);
			return diag;
		}

		diag = Array2d.readDiag(file.file)
			.asDoubleArray()
			.data();
		diagonals.put(key, diag);
		return diag;
	}

	public double[] columnOf(String libId, LibMatrix matrix, int j) {
		var key = keyOf(libId, matrix);

		var fullMatrix = matrices.get(key);
		if (fullMatrix != null) {
			return fullMatrix.getColumn(j);
		}

		var colCache = columns.get(key);
		var col = colCache != null
			? colCache.get(j)
			: null;
		if (col != null)
			return col;

		var file = MatrixFile.of(dir, libId, matrix);
		if (file.isEmpty())
			return new double[0];

		if (file.isSparse()) {
			fullMatrix = file.readFull();
			if (fullMatrix == null)
				return new double[0];
			matrices.put(key, fullMatrix);
			return fullMatrix.getColumn(j);
		}

		col = Array2d.readColumn(file.file, j)
			.asDoubleArray()
			.data();
		if (colCache == null) {
			colCache = new TIntObjectHashMap<>(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1);
			columns.put(key, colCache);
		}
		colCache.put(j, col);
		return col;
	}


	private String keyOf(String libId, LibMatrix matrix) {
		return libId + "::" + matrix.name();
	}

	private record MatrixFile(File file, boolean isSparse) {

		static MatrixFile of(
			LibraryDir dir, String libId, LibMatrix matrix) {

			var lib = dir.getLibrary(libId).orElse(null);
			if (lib == null)
				return new MatrixFile(null, false);

			var npy = new File(lib.folder(), matrix.name() + ".npy");
			if (npy.exists()) {
				return new MatrixFile(npy, false);
			}

			var npz = new File(lib.folder(), matrix.name() + ".npz");
			return npz.exists()
				? new MatrixFile(npz, true)
				: new MatrixFile(null, false);
		}

		boolean isEmpty() {
			return file == null || !file.exists();
		}

		public MatrixReader readFull() {
			return isEmpty()
				? null
				: NpyMatrix.read(file);
		}
	}

}
