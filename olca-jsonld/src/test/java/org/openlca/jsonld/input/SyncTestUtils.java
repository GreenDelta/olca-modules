package org.openlca.jsonld.input;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.IReferenceSearch;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ZipStore;

class SyncTestUtils {

	static File copyToTemp(String filename) throws IOException {
		Path tmp = Files.createTempFile("olca-sync-test", ".zip");
		Files.copy(SyncTestUtils.class.getResourceAsStream(filename), tmp, StandardCopyOption.REPLACE_EXISTING);
		return tmp.toFile();
	}

	static void doImport(File file, IDatabase db) throws IOException {
		ZipStore store = ZipStore.open(file);
		JsonImport json = new JsonImport(store, db);
		json.setUpdateMode(UpdateMode.ALWAYS);
		json.run();
	}

	static boolean validate(IDatabase db, ModelType[] modelTypes, Function<Reference, Boolean> isValid) {
		for (ModelType type : modelTypes) {
			if (!validate(db, type, isValid))
				return false;
		}
		return true;
	}

	private static boolean validate(IDatabase db, ModelType type, Function<Reference, Boolean> isValid) {
		Set<Long> ids = new HashSet<>();
		for (AbstractEntity entity : Daos.base(db, type.getModelClass()).getAll()) {
			ids.add(entity.getId());
		}
		List<Reference> references = findReferences(db, type, ids);
		for (Reference reference : references) {
			if (!isValid.apply(reference))
				return false;
		}
		return true;
	}

	private static List<Reference> findReferences(IDatabase db, ModelType type, Set<Long> ids) {
		return IReferenceSearch.FACTORY.createFor(type, db, true).findReferences(ids);
	}

	static void delete(File file) {
		if (file == null || !file.exists())
			return;
		file.delete();
	}

}
