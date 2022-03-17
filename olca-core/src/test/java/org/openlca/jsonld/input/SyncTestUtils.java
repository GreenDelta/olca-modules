package org.openlca.jsonld.input;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.IReferenceSearch;
import org.openlca.core.database.references.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;

class SyncTestUtils {

	static File copyToTemp(String filename) {
		try {
			Path tmp = Files.createTempFile("olca-sync-test", ".zip");
			Files.copy(
					SyncTestUtils.class.getResourceAsStream(filename),
					tmp,
					StandardCopyOption.REPLACE_EXISTING);
			return tmp.toFile();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static void doImport(File file, IDatabase db) {
		try {
			ZipStore store = ZipStore.open(file);
			JsonImport json = new JsonImport(store, db);
			json.setUpdateMode(UpdateMode.ALWAYS);
			json.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static boolean validate(ModelType[] modelTypes, Predicate<Reference> isValid) {
		var db = Tests.getDb();
		for (ModelType type : modelTypes) {
			var ids = db.getAll(type.getModelClass())
					.stream()
					.map(e -> e.id)
					.collect(Collectors.toSet());
			var refs = IReferenceSearch.FACTORY
					.createFor(type, db, true)
					.findReferences(ids);
			for (var ref : refs) {
				if (!isValid.test(ref))
					return false;
			}
		}
		return true;
	}

	static void delete(File file) {
		if (file == null || !file.exists())
			return;
		try {
			Files.delete(file.toPath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
