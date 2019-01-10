package org.openlca.core.matrix.io.olcamat;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Categories;

class Indexer {

	private Map<Long, ProcessDescriptor> processes;
	private Map<Long, FlowDescriptor> flows;
	private Map<Long, Location> locations;
	private Map<Long, FlowProperty> flowProperties;
	private Map<Long, Category> categories;

	Indexer(IDatabase db) {
		processes = descriptors(new ProcessDao(db));
		flows = descriptors(new FlowDao(db));
		locations = all(new LocationDao(db));
		flowProperties = all(new FlowPropertyDao(db));
		categories = all(new CategoryDao(db));
	}

	private <T extends BaseDescriptor> Map<Long, T> descriptors(
			RootEntityDao<?, T> dao) {
		Map<Long, T> map = new HashMap<>();
		for (T d : dao.getDescriptors()) {
			map.put(d.id, d);
		}
		return map;
	}

	private <T extends RootEntity> Map<Long, T> all(RootEntityDao<T, ?> dao) {
		Map<Long, T> map = new HashMap<>();
		for (T d : dao.getAll()) {
			map.put(d.getId(), d);
		}
		return map;
	}

	EnviIndexEntry getEnviEntry(long flowID) {
		EnviIndexEntry e = new EnviIndexEntry();
		fillFlowInfo(e, flowID);
		return e;
	}

	private void fillFlowInfo(EnviIndexEntry e, long flowID) {
		FlowDescriptor f = flows.get(flowID);
		if (f == null)
			return;
		e.flowID = f.refId;
		e.flowName = f.name;
		e.flowType = f.flowType;
		Location location = locations.get(f.location);
		if (location != null) {
			e.flowLocation = location.getCode();
		}
		e.flowCategory = category(f.category);
		FlowProperty fp = flowProperties.get(f.refFlowPropertyId);
		if (fp == null)
			return;
		e.flowPropertyID = fp.getRefId();
		e.flowPropertyName = fp.getName();
		UnitGroup ug = fp.getUnitGroup();
		if (ug == null || ug.getReferenceUnit() == null)
			return;
		Unit u = ug.getReferenceUnit();
		e.unitID = u.getRefId();
		e.unitName = u.getName();
	}

	TechIndexEntry getTechEntry(ProcessProduct product) {
		TechIndexEntry e = new TechIndexEntry();
		if (product == null)
			return e;
		// TODO: we could have product systems here
		ProcessDescriptor p = processes.get(product.id());
		if (p == null)
			return e;
		e.processID = p.refId;
		e.processName = p.name;
		e.processType = p.processType;
		Location l = locations.get(p.location);
		if (l != null) {
			e.processLocation = l.getCode();
		}
		e.processCategory = category(p.category);

		// TODO: we could directly take the flow descriptor here
		fillFlowInfo(e, product.flowId());
		return e;
	}

	private String category(Long id) {
		if (id == null)
			return "";
		Category cat = categories.get(id);
		return Categories.path(cat).stream()
				.reduce((c1, c2) -> c1 + "/" + c2)
				.orElse("");
	}
}
