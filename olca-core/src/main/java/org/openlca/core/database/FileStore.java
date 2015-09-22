package org.openlca.core.database;

import java.io.File;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;

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

	public File getFolder(BaseDescriptor d) {
		if (d == null)
			return new File(root, "null");
		else
			return getFolder(d.getModelType(), d.getRefId());
	}

	public File getFolder(RootEntity e) {
		if (e == null)
			return new File(root, "null");
		ModelType type = ModelType.forModelClass(e.getClass());
		return getFolder(type, e.getRefId());
	}

	public File getFolder(ModelType type, String id) {
		File dir = new File(root, getPath(type));
		String subPath = id != null ? id : "null";
		return new File(dir, subPath);
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

}
