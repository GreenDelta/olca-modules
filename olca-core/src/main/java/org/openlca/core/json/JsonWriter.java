package org.openlca.core.json;

import org.openlca.core.model.Category;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class JsonWriter {

	private final WriterConfig config;

	public JsonWriter() {
		this(WriterConfig.getDefault());
	}

	public JsonWriter(WriterConfig config) {
		this.config = config;
	}

	public String dump(Object obj) {
		GsonBuilder b = new GsonBuilder();
		if (config.isPrettyPrinting())
			b.setPrettyPrinting();
		b.registerTypeAdapter(Category.class, new CategoryWriter(this));
		registerDescriptorWriter(b);
		Gson gson = b.create();
		return gson.toJson(obj);
	}

	private void registerDescriptorWriter(GsonBuilder b) {
		DescriptorWriter dw = new DescriptorWriter(this);
		b.registerTypeAdapter(ActorDescriptor.class, dw);
		b.registerTypeAdapter(BaseDescriptor.class, dw);
		b.registerTypeAdapter(CategorizedDescriptor.class, dw);
		b.registerTypeAdapter(FlowDescriptor.class, dw);
		b.registerTypeAdapter(FlowPropertyDescriptor.class, dw);
		b.registerTypeAdapter(ImpactCategoryDescriptor.class, dw);
		b.registerTypeAdapter(ImpactMethodDescriptor.class, dw);
		b.registerTypeAdapter(NwSetDescriptor.class, dw);
		b.registerTypeAdapter(ProcessDescriptor.class, dw);
		b.registerTypeAdapter(ProductSystemDescriptor.class, dw);
		b.registerTypeAdapter(ProjectDescriptor.class, dw);
		b.registerTypeAdapter(SourceDescriptor.class, dw);
		b.registerTypeAdapter(UnitGroupDescriptor.class, dw);
	}

	void addContext(JsonObject object) {
		String url = "http://openlca.org/";
		JsonObject context = new JsonObject();
		context.addProperty("@vocab", url);
		JsonObject vocabType = new JsonObject();
		vocabType.addProperty("@type", "@vocab");
		context.add("modelType", vocabType);
		object.add("@context", context);
	}

	void addAttributes(RootEntity entity, JsonObject object) {
		String type = entity.getClass().getSimpleName();
		object.addProperty("@type", type);
		object.addProperty("@id", entity.getRefId());
		object.addProperty("name", entity.getName());
		object.addProperty("description", entity.getDescription());
	}

}
