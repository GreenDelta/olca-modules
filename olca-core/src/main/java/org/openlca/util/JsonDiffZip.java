package org.openlca.util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonDiffZip {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private IDatabase origin;

	private JsonDiffZip(IDatabase db) {
		this.db = Objects.requireNonNull(db);
	}

	public static JsonDiffZip of(IDatabase db) {
		return new JsonDiffZip(db);
	}

	public JsonDiffZip withOrigin(IDatabase origin) {
		this.origin = Objects.requireNonNull(origin);
		return this;
	}

	public void exportTo(File file) {
		Objects.requireNonNull(file);
		if (origin == null) {
			log.error("no original database provided; did nothing");
			return;
		}

		log.info("write diff of '{}' and '{}' to: {}", db, origin, file);
		try (var zip = ZipStore.open(file)) {

			var export = new JsonExport(db, zip)
					.withReferences(false)
					.withDefaultProviders(false)
					.skipLibraryData(true);

			int count = 0;
			for (var type : ModelType.values()) {
				var diff = getDiff(type);
				for (var d : diff) {
					var entity = db.get(type.getModelClass(), d.id);
					if (entity == null) {
						log.error("failed to load entity: {}", d);
						continue;
					}
					export.write(entity);
					count++;
				}
			}
			log.info("wrote Json diff with {} entities", count);

		} catch (Exception e) {
			throw new RuntimeException("failed to export diff-zip", e);
		}
	}

	private Set<RootDescriptor> getDiff(ModelType type) {
		var clazz = type.getModelClass();
		var origins = new HashMap<String, RootDescriptor>();
		for (var o : origin.getDescriptors(clazz)) {
			origins.put(o.refId, o);
		}
		var diff = new HashSet<RootDescriptor>();
		for (var d : db.getDescriptors(clazz)) {
			var o = origins.get(d.refId);
			if (o == null
					|| d.version > o.version
					|| (d.version == o.version && d.lastChange > o.lastChange)) {
				diff.add(d);
			}
		}
		return diff;
	}
}
