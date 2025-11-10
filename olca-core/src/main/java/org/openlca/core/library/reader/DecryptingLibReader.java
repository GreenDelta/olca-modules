package org.openlca.core.library.reader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Supplier;

import javax.crypto.Cipher;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.matrix.io.index.IxImpactIndex;
import org.openlca.core.matrix.io.index.IxTechIndex;

public class DecryptingLibReader implements LibReader {

	private final IDatabase db;
	private final Supplier<Cipher> cipher;
	private final LibReader reader;

	private DecryptingLibReader(
		Supplier<Cipher> cipher, LibReader reader, IDatabase db
	) {
		this.db = Objects.requireNonNull(db);
		this.cipher = Objects.requireNonNull(cipher);
		this.reader = Objects.requireNonNull(reader);
	}

	public static LibReader of(
		Supplier<Cipher> cipher, LibReader reader, IDatabase db
	) {
		return new DecryptingLibReader(cipher, reader, db);
	}

	@Override
	public Library library() {
		return reader.library();
	}

	@Override
	public TechIndex techIndex() {
		var data = decryptFile("index_A.enc");
		if (data == null)
			return null;
		try (data) {
			return IxTechIndex.readProto(data)
				.syncWith(db)
				.orElse(null);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to parse tech-index", e);
		}
	}

	@Override
	public EnviIndex enviIndex() {
		var data = decryptFile("index_B.enc");
		if (data == null)
			return null;
		try (data) {
			return IxEnviIndex.readProto(data)
				.syncWith(db)
				.orElse(null);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to parse envi-index", e);
		}
	}

	@Override
	public ImpactIndex impactIndex() {
		var data = decryptFile("index_C.enc");
		if (data == null)
			return null;
		try (data) {
			return IxImpactIndex.readProto(data)
				.syncWith(db)
				.orElse(null);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to parse impact-index", e);
		}
	}

	private ByteArrayInputStream decryptFile(String name) {
		var file = new File(reader.library().folder(), name);
		if (!file.exists())
			return null;
		try {
			var data = Files.readAllBytes(file.toPath());
			var decoder = cipher.get();
			var decoded = decoder.doFinal(data);
			return new ByteArrayInputStream(decoded);
		} catch (Exception e) {
			throw new RuntimeException(
				"failed to decrypt file: " + name, e);
		}
	}

	@Override
	public MatrixReader matrixOf(LibMatrix matrix) {
		return reader.matrixOf(matrix);
	}

	@Override
	public double[] costs() {
		return reader.costs();
	}

	@Override
	public double[] diagonalOf(LibMatrix matrix) {
		return reader.diagonalOf(matrix);
	}

	@Override
	public double[] columnOf(LibMatrix matrix, int col) {
		return reader.columnOf(matrix, col);
	}

	@Override
	public void dispose() {
		reader.dispose();
	}
}
