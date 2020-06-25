package org.openlca.core.database;

import java.io.File;

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

	public File getFolder(RootEntity e) {
		if (e == null)
			return new File(root, "null");
		ModelType type = ModelType.forModelClass(e.getClass());
		return getFolder(type, e.refId);
	}

	public File getFolder(ModelType type, String id) {
		if (type == null || id == null)
			return new File(root, "null");
		File dir = new File(root, getPath(type));
		String subPath = id != null ? id : "null";
		return new File(dir, subPath);
	}

	public void copyFolder(RootEntity from, RootEntity to) {
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

	public void deleteFolder(RootEntity e) {
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
		switch (type) {
		case CATEGORY:
			return "categories";
		case PROCESS:
			return "processes";
		case FLOW:
			return "flows";
		case FLOW_PROPERTY:
			return "flow_properties";
		case ACTOR:
			return "actors";
		case IMPACT_CATEGORY:
			return "lcia_categories";
		case IMPACT_METHOD:
			return "lcia_methods";
		case LOCATION:
			return "locations";
		case NW_SET:
			return "nw_sets";
		case PRODUCT_SYSTEM:
			return "product_systems";
		case PROJECT:
			return "projects";
		case SOCIAL_INDICATOR:
			return "social_indicators";
		case SOURCE:
			return "sources";
		case UNIT:
			return "units";
		case UNIT_GROUP:
			return "unit_groups";
		default:
			return "unknown";
		}
	}
	
	public static String getPath(ModelType type, String refId) {
		return getPath(type) + File.separator + refId;
	}

}
