package org.openlca.jsonld.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
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

public final class GsonUtil {

	private GsonUtil() {
	}

	public static Gson createGson() {
		GsonBuilder b = new GsonBuilder();
		registerTypeAdapters(b);
		registerDescriptorWriter(b);
		Gson gson = b.create();
		return gson;
	}

	private static void registerTypeAdapters(GsonBuilder b) {
		b.registerTypeAdapter(Category.class, new CategoryWriter());
		b.registerTypeAdapter(Actor.class, new ActorWriter());
		b.registerTypeAdapter(Source.class, new SourceWriter());
		b.registerTypeAdapter(Unit.class, new UnitWriter());
		b.registerTypeAdapter(UnitGroup.class, new UnitGroupWriter());
		b.registerTypeAdapter(FlowProperty.class, new FlowPropertyWriter());
		b.registerTypeAdapter(Flow.class, new FlowWriter());
		b.registerTypeAdapter(Uncertainty.class, new UncertaintyWriter());
		b.registerTypeAdapter(Parameter.class, new ParameterWriter());
		b.registerTypeAdapter(Location.class, new LocationWriter());
		b.registerTypeAdapter(Process.class, new ProcessWriter());
		b.registerTypeAdapter(Exchange.class, new ExchangeWriter());
		b.registerTypeAdapter(FlowPropertyFactor.class,
				new FlowPropertyFactorWriter());
		b.registerTypeAdapter(ImpactMethod.class, new ImpactMethodWriter());
		b.registerTypeAdapter(ImpactCategory.class, new ImpactCategoryWriter());
	}

	private static void registerDescriptorWriter(GsonBuilder b) {
		DescriptorWriter dw = new DescriptorWriter();
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
}
