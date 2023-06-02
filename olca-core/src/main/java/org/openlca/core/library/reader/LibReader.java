package org.openlca.core.library.reader;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;

import javax.crypto.Cipher;
import java.io.File;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface LibReader {

	static Builder of(Library lib, IDatabase db) {
		return new Builder(lib, db);
	}

	Library library();

	default String libraryName() {
		return library().name();
	}

	TechIndex techIndex();

	default boolean hasTechData() {
		return techIndex() != null;
	}

	EnviIndex enviIndex();

	default boolean hasEnviData() {
		return techIndex() != null && enviIndex() != null;
	}

	ImpactIndex impactIndex();

	default boolean hasImpactData() {
		return impactIndex() != null;
	}

	default boolean hasCostData() {
		var file = new File(library().folder(), "costs.npy");
		return file.exists();
	}

	MatrixReader matrixOf(LibMatrix matrix);

	double[] costs();

	double[] diagonalOf(LibMatrix matrix);

	double[] columnOf(LibMatrix matrix, int col);

	default void dispose() {
	}

	class Builder {
		private final Library lib;
		private final IDatabase db;
		private MatrixSolver solver;
		private Supplier<Cipher> cipher;

		private Builder(Library lib, IDatabase db) {
			this.lib = Objects.requireNonNull(lib);
			this.db = Objects.requireNonNull(db);
		}

		public Builder withSolver(MatrixSolver solver) {
			this.solver = solver;
			return this;
		}

		public Builder withDecryption(Supplier<Cipher> cipher) {
			this.cipher = cipher;
			return this;
		}

		public LibReader create() {
			var direct = DirectLibReader.of(lib, db);
			var caching = CachingLibReader.of(wrapDecryption(direct));
			if (lib.hasMatrix(LibMatrix.A)
				&& !lib.hasMatrix(LibMatrix.INV)) {
				return solver != null
					? SolvingLibReader.of(caching, solver)
					: SolvingLibReader.of(caching);
			}
			return caching;
		}

		private LibReader wrapDecryption(LibReader reader) {
			boolean encrypted = Stream.of(
					"index_A.enc", "index_B.enc", "index_C.enc")
				.map(name -> new File(lib.folder(), name))
				.anyMatch(File::exists);
			if (!encrypted)
				return reader;
			if (cipher == null) {
				throw new IllegalStateException(
					"library '" + lib.name() + "' is encrypted but no "
						+ "decrypting cipher was provided to read that library");
			}
			return DecryptingLibReader.of(cipher, reader, db);
		}
	}
}
