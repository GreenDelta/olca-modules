package org.openlca.core.library;

import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Categories;
import org.openlca.util.Strings;

public class WriterContext {

	private final Library library;
	private final IDatabase db;



	private WriterContext(IDatabase db, Library library) {
		this.db = db;
		this.library = library;
	}

	public static WriterContext create(IDatabase db, Library library) {
		return new WriterContext(db, library);
	}

	public Library library() {
		return library;
	}

	public IDatabase db() {
		return db;
	}

	Proto.Flow toProtoFlow(FlowDescriptor d) {
		var proto = Proto.Flow.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		var category = categories().pathOf(d.category);
		proto.setCategory(Strings.orEmpty(category));
		if (d.flowType != null) {
			proto.setType(d.flowType.name());
		}
		var property = quantities().get(d.refFlowPropertyId);
		if (property != null && property.unitGroup != null) {
			var unit = property.unitGroup.referenceUnit;
			if (unit != null) {
				proto.setUnit(Strings.orEmpty(unit.name));
			}
		}
		return proto.build();
	}

	Proto.Process toProtoProcess(RootDescriptor d) {
		var proto = Proto.Process.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		var category = categories().pathOf(d.category);
		proto.setCategory(Strings.orEmpty(category));
		if (d instanceof ProcessDescriptor) {
			var loc = ((ProcessDescriptor) d).location;
			if (loc != null) {
				var code = locationCodes().get(loc);
				proto.setLocationCode(Strings.orEmpty(code));
			}
		}
		return proto.build();
	}

	Proto.Impact toProtoImpact(ImpactDescriptor d) {
		var proto = Proto.Impact.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		proto.setUnit(Strings.orEmpty(d.referenceUnit));
		return proto.build();
	}

	Proto.Location toProtoLocation(LocationDescriptor d) {
		var proto = Proto.Location.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		proto.setCode(Strings.orEmpty(d.code));
		return proto.build();
	}

}
