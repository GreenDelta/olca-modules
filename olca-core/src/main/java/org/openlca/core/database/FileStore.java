package org.openlca.core.database;

import java.io.File;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Dirs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides structured access to files that are stored outside of a database.
 * Such files can be shapefiles of LCIA methods, PDF documents of sources etc.
 */
public class FileStore {

	private final File root;

	public FileStore(File rootFolder) {
		this.root = rootFolder;
	}

	public FileStore(IDatabase db) {
		this(db.getFileStorageLocation());
	}

	public File getRoot() {
		return root;
	}

	public File getFolder(Descriptor d) {
		if (d == null)
			return new File(root, "null");
		else
			return getFolder(d.type, d.refId);
	}

	public File getFolder(RefEntity e) {
		if (e == null)
			return new File(root, "null");
		ModelType type = ModelType.forModelClass(e.getClass());
		return getFolder(type, e.refId);
	}

	public File getFolder(ModelType type, String id) {
		if (type == null || id == null)
			return new File(root, "null");
		File dir = new File(root, getPath(type));
		return new File(dir, id);
	}

	public void copyFolder(RefEntity from, RefEntity to) {
		if (from == null || to == null)
			return;
		ModelType type = ModelType.forModelClass(from.getClass());
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
			log.error("Failed to copy directory " + fromDir + " to " + toDir, e);
		}
	}

	public void deleteFolder(Descriptor d) {
		if (d == null)
			return;
		deleteFolder(d.type, d.refId);
	}

	public void deleteFolder(RefEntity e) {
		if (e == null)
			return;
		ModelType type = ModelType.forModelClass(e.getClass());
		deleteFolder(type, e.refId);
	}

	public void deleteFolder(ModelType type, String id) {
		if (type == null || id == null)
			return;
		File dir = getFolder(type, id);
		if (dir == null || !dir.exists())
			return;
		try {
			Dirs.delete(dir.toPath());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to delete directory " + dir, e);
		}
	}

	/**
	 * Returns the path/folder name for the given model type (e.g. 'processes',
	 * 'flow_properties' etc).
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
			case NW_SET -> "nw_sets";
			case PRODUCT_SYSTEM -> "product_systems";
			case PROJECT -> "projects";
			case SOCIAL_INDICATOR -> "social_indicators";
			case SOURCE -> "sources";
			case UNIT -> "units";
			case UNIT_GROUP -> "unit_groups";
			case EPD -> "epds";
			case RESULT -> "results";
			case CURRENCY -> "currencies";
			case DQ_SYSTEM -> "dq_systems";
			case PARAMETER -> "parameters";
			default -> "unknown";
		};
	}

	public static String getPath(ModelType type, String refId) {
		return getPath(type) + File.separator + refId;
	}

}
