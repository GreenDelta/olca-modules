package org.openlca.core.database;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openlca.core.model.MappingFile;

public class MappingFileDao extends BaseDao<MappingFile> {

	public MappingFileDao(IDatabase db) {
		super(MappingFile.class, db);
	}

	/**
	 * Get the mapping file with the given name from the database.
	 */
	public static MappingFile get(IDatabase db, String name) {
		if (db == null || name == null)
			return null;
		return new MappingFileDao(db).getForName(name);
	}

	public MappingFile getForName(String name) {
		var jpql = "select m from MappingFile m where m.name = :name";
		return getFirst(jpql, Collections.singletonMap("name", name));
	}

	/**
	 * Returns the names of the mapping files that are stored in the database.
	 * In contrast to other entities a name of a mapping file should be unique.
	 * So you should check this list before you store a new mapping file under
	 * a given name.
	 */
	public Set<String> getNames() {
		var set = new HashSet<String>();
		var sql = "select file_name from tbl_mapping_files";
		NativeSql.on(db).query(sql, r -> {
			set.add(r.getString(1));
			return true;
		});
		return set;
	}

}
