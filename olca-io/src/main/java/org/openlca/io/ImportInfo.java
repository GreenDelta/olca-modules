package org.openlca.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

public class ImportInfo {

	public static enum Status {

		/** The entity was imported successfully. */
		IMPORTED,

		/** The entity already exists in the database and was not imported. */
		IGNORED,

		/** An existing entity was replaced with another version. */
		REPLACED,

		/** Some error occurred during the import of the respective entity. */
		ERROR

	}

	public final Descriptor entity;
	public final Status status;
	public String message;

	public ImportInfo(Descriptor entity, Status status) {
		this.entity = entity;
		this.status = status;
	}

	public static ImportInfo imported(RootEntity entity) {
		Descriptor d = Descriptor.of(entity);
		return new ImportInfo(d, Status.IMPORTED);
	}

	public static ImportInfo ignored(RootEntity entity) {
		Descriptor d = Descriptor.of(entity);
		return new ImportInfo(d, Status.IGNORED);
	}

	public static ImportInfo error(RootEntity entity) {
		Descriptor d = Descriptor.of(entity);
		return new ImportInfo(d, Status.ERROR);
	}

	/**
	 * Collects the import information during an import. For each entity only
	 * one information is collected.
	 */
	public static class Collector {

		private Map<String, ImportInfo> infos = new HashMap<>();

		public void imported(RootEntity e) {
			put(e, Status.IMPORTED);
		}

		public void ignored(RootEntity e) {
			put(e, Status.IGNORED);
		}

		public void error(RootEntity e) {
			put(e, Status.ERROR);
		}

		private void put(RootEntity e, Status s) {
			if (e == null || e.refId == null)
				return;
			ImportInfo info = infos.get(e.refId);
			if (info != null)
				return;
			Descriptor d = Descriptor.of(e);
			infos.put(e.refId, new ImportInfo(d, s));
		}

		public List<ImportInfo> get() {
			return new ArrayList<>(infos.values());
		}
	}
}
