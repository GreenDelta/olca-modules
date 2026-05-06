package org.openlca.core.database;

import java.io.File;
import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Dirs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides structured access to files that are stored outside of a database.
 * Such files can be shapefiles of LCIA methods, PDF documents of sources etc.
 */
public record FileStore(File root) {

	public FileStore {
		Objects.requireNonNull(root);
	}

	public static Res<FileStore> of(IDatabase db) {
		if (db == null)
			return Res.error("No database provided");
		var dir = db.getFileStorageLocation();
		if (dir == null)
			return Res.error("Database has no file storage linked");
		try {
			Dirs.createIfAbsent(dir);
			return Res.ok(new FileStore(dir));
		} catch (Exception e) {
			return Res.error("Failed to create file storage location of database", e);
		}
	}

	public File getFolder(Descriptor d) {
		return d == null
			? new File(root, "null")
			: getFolder(d.type, d.refId);
	}

	public File getFolder(RootEntity e) {
		if (e == null)
			return new File(root, "null");
		ModelType type = ModelType.of(e.getClass());
		return getFolder(type, e.refId);
	}

	public File getFolder(ModelType type, String id) {
		if (type == null || id == null)
			return new File(root, "null");
		File dir = new File(root, getPath(type));
		return new File(dir, id);
	}

	public void copyFolder(RootEntity from, RootEntity to) {
		if (from == null || to == null)
			return;
		ModelType type = ModelType.of(from.getClass());
		copyFolder(type, from.refId, to.refId);
	}

	public void copyFolder(Descriptor from, Descriptor to) {
		if (from == null || to == null)
			return;
		copyFolder(from.type, from.refId, to.refId);
	}

	public void copyFolder(ModelType type, String fromId, String toId) {
		if (type == null || fromId == null || toId == null)
			return;
		File fromDir = getFolder(type, fromId);
		if (!fromDir.exists())
			return;
		File toDir = getFolder(type, toId);
		try {
			Dirs.copy(fromDir.toPath(), toDir.toPath());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to copy directory {} to {}", fromDir, toDir, e);
		}
	}

	public void deleteFolder(Descriptor d) {
		if (d == null)
			return;
		deleteFolder(d.type, d.refId);
	}

	public void deleteFolder(RootEntity e) {
		if (e == null)
			return;
		ModelType type = ModelType.of(e.getClass());
		deleteFolder(type, e.refId);
	}

	public void deleteFolder(ModelType type, String id) {
		if (type == null || id == null)
			return;
		File dir = getFolder(type, id);
		if (!dir.exists())
			return;
		try {
			Dirs.delete(dir.toPath());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to delete directory {}", dir, e);
		}
	}

	/**
	 * Returns the path/folder name for the given model type (e.g. 'processes',
	 * 'flow_properties' etc.).
	 */
	public static String getPath(ModelType type) {
		if (type == null)
			return "null";
		return switch (type) {
			case CATEGORY -> "categories";
			case PROCESS -> "processes";
			case FLOW -> "flows";
			case FLOW_PROPERTY -> "flow_properties";
			case ACTOR -> "actors";
			case IMPACT_CATEGORY -> "lcia_categories";
			case IMPACT_METHOD -> "lcia_methods";
			case LOCATION -> "locations";
			case PRODUCT_SYSTEM -> "product_systems";
			case PROJECT -> "projects";
			case SOCIAL_INDICATOR -> "social_indicators";
			case SOURCE -> "sources";
			case UNIT_GROUP -> "unit_groups";
			case EPD -> "epds";
			case RESULT -> "results";
			case CURRENCY -> "currencies";
			case DQ_SYSTEM -> "dq_systems";
			case PARAMETER -> "parameters";
		};
	}

	public static String getPath(ModelType type, String refId) {
		return getPath(type) + File.separator + refId;
	}

}
