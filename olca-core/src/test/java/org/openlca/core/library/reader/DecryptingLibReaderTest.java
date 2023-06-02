package org.openlca.core.library.reader;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryExport;
import org.openlca.core.library.Mounter;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Dirs;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class DecryptingLibReaderTest {

	private final char[] PASSWORD = "abc123".toCharArray();
	private final byte[] SALT = "123abc".getBytes();

	private SecretKey key;
	private IDatabase sdb;
	private IDatabase tdb;
	private File tempDir;

	@Before
	public void setup() throws Exception {
		sdb = Derby.createInMemory();
		tdb = Derby.createInMemory();
		tempDir = Files.createTempDirectory("_olca_lib").toFile();

		var keySpec = new PBEKeySpec(PASSWORD, SALT, 4096, 128);
		var keyData = SecretKeyFactory
			.getInstance("PBKDF2WithHmacSHA1")
			.generateSecret(keySpec)
			.getEncoded();
		key = new SecretKeySpec(keyData, "AES");
	}

	@After
	public void cleanup() throws Exception {
		sdb.close();
		tdb.close();
		Dirs.delete(tempDir);
	}

	@Test
	public void testAll() throws Exception {

		// create the simple model
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var e = Flow.elementary("e", mass);
		var p = Flow.product("p", mass);
		var process = Process.of("P", p);
		process.output(e, 21);
		var i = ImpactCategory.of("I", "kgEq");
		i.factor(e, 2);
		sdb.insert(units, mass, e, p, process, i);

		// write it as library
		var libDir = new File(tempDir, "lib");
		new LibraryExport(sdb, libDir).run();
		var lib = Library.of(libDir);
		encrypt(lib);

		// mount the library & create the reader
		Mounter.of(tdb, lib).run();
		var reader = LibReader.of(lib, tdb)
			.withDecryption(() -> cipherOf(Cipher.DECRYPT_MODE))
			.create();

		// run tests
		checkTechIndex(reader);
		checkEnviIndex(reader);
		checkImpactIndex(reader);
		checkMatrices(reader);
	}

	private void encrypt(Library lib) throws Exception {
		var names = List.of("index_A", "index_B", "index_C");
		for (var name : names) {
			var input = new File(lib.folder(), name + ".bin");
			if (!input.exists())
				continue;
			var inData = Files.readAllBytes(input.toPath());
			var cipher = cipherOf(Cipher.ENCRYPT_MODE);
			var outData = cipher.doFinal(inData);
			var output = new File(lib.folder(), name + ".enc");
			Files.write(output.toPath(), outData);
			Files.delete(input.toPath());
		}
	}

	private Cipher cipherOf(int mode) {
		try {
			var cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(mode, key);
			return cipher;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void checkTechIndex(LibReader reader) {
		var techIdx = reader.techIndex();
		assertEquals(1, techIdx.size());
		var techFlow = techIdx.at(0);
		assertEquals("P", techFlow.provider().name);
		assertEquals("p", techFlow.flow().name);
	}

	private void checkEnviIndex(LibReader reader) {
		var enviIdx = reader.enviIndex();
		assertEquals(1, enviIdx.size());
		var enviFlow = enviIdx.at(0);
		assertEquals("e", enviFlow.flow().name);
	}

	private void checkImpactIndex(LibReader reader) {
		var impactIdx = reader.impactIndex();
		assertEquals(1, impactIdx.size());
		var impact = impactIdx.at(0);
		assertEquals("I", impact.name);
	}

	private void checkMatrices(LibReader reader) {
		var A = reader.matrixOf(LibMatrix.A);
		var B = reader.matrixOf(LibMatrix.B);
		var C = reader.matrixOf(LibMatrix.C);
		for (var m : List.of(A, B, C)) {
			assertEquals(1, m.rows());
			assertEquals(1, m.columns());
		}

		assertEquals(1, A.get(0, 0), 1e-10);
		assertEquals(21, B.get(0, 0), 1e-10);
		assertEquals(2, C.get(0, 0), 1e-10);
	}
}
