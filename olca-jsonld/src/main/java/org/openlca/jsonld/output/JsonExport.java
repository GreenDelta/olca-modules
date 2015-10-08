package org.openlca.jsonld.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Callback;
import org.openlca.core.model.Callback.Message;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.EntityStore;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Writes entities to an entity store (e.g. a document or zip file). It also
 * writes the referenced entities to this store if they are not yet contained.
 */
public class JsonExport {

	private final EntityStore store;

	public JsonExport(IDatabase database, EntityStore store) {
		this.store = store;
	}

	public <T extends RootEntity> void write(T entity, Callback cb) {
		if (entity == null)
			return;
		ModelType type = ModelType.forModelClass(entity.getClass());
		if (type == null || entity.getRefId() == null) {
			cb.apply(Message.error("no refId, or type is unknown"), entity);
			return;
		}
		if (store.contains(type, entity.getRefId()))
			return;
		Writer<T> writer = getWriter(entity);
		if (writer == null) {
			cb.apply(Message.error("no writer found for type " + type), entity);
			return;
		}
		try {
			JsonObject obj = writer.write(entity, ref -> {
				write(ref, cb);
			});
			store.put(type, obj);
			cb.apply(Message.info("data set exported"), entity);
		} catch (Exception e) {
			cb.apply(Message.error("failed to export data set", e), entity);
		}
	}

	public static <T extends RootEntity> String toJson(T entity) {
		if (entity == null)
			return "{}";
		Writer<T> writer = getWriter(entity);
		JsonObject json = writer.write(entity, ref -> {
		});
		Gson gson = new Gson();
		return gson.toJson(json);
	}

	@SuppressWarnings("unchecked")
	private static <T extends RootEntity> Writer<T> getWriter(T entity) {
		if (entity == null)
			return null;
		if (entity instanceof Actor)
			return Writer.class.cast(new ActorWriter());
		if (entity instanceof Category)
			return Writer.class.cast(new CategoryWriter());
		if (entity instanceof FlowProperty)
			return Writer.class.cast(new FlowPropertyWriter());
		if (entity instanceof Flow)
			return Writer.class.cast(new FlowWriter());
		if (entity instanceof ImpactCategory)
			return Writer.class.cast(new ImpactCategoryWriter());
		if (entity instanceof ImpactMethod)
			return Writer.class.cast(new ImpactMethodWriter());
		if (entity instanceof Location)
			return Writer.class.cast(new LocationWriter());
		if (entity instanceof Process)
			return Writer.class.cast(new ProcessWriter());
		if (entity instanceof Source)
			return Writer.class.cast(new SourceWriter());
		if (entity instanceof UnitGroup)
			return Writer.class.cast(new UnitGroupWriter());
		if (entity instanceof SocialIndicator)
			return Writer.class.cast(new SocialIndicatorWriter());
		else
			return null;
	}
}
