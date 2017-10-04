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
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

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

	private <T extends BaseDescriptor> Map<Long, T> descriptors(RootEntityDao<?, T> dao) {
		Map<Long, T> map = new HashMap<>();
		for (T d : dao.getDescriptors()) {
			map.put(d.getId(), d);
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
		e.flowID = f.getRefId();
		e.flowName = f.getName();
		e.flowType = f.getFlowType();
		Location location = locations.get(f.getLocation());
		if (location != null) {
			e.flowLocation = location.getCode();
		}
		String[] cat = category(f.getCategory());
		e.flowCategory = cat[0];
		e.flowSubCategory = cat[1];
		FlowProperty fp = flowProperties.get(f.getRefFlowPropertyId());
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

	TechIndexEntry getTechEntry(LongPair product) {
		TechIndexEntry e = new TechIndexEntry();
		if (product == null)
			return e;
		ProcessDescriptor p = processes.get(product.getFirst());
		if (p == null)
			return e;
		e.processID = p.getRefId();
		e.processName = p.getName();
		e.processType = p.getProcessType();
		Location l = locations.get(p.getLocation());
		if (l != null) {
			e.processLocation = l.getCode();
		}
		String[] cat = category(p.getCategory());
		e.processCategory = cat[0];
		e.processSubCategory = cat[1];
		fillFlowInfo(e, product.getSecond());
		return e;
	}

	private String[] category(Long id) {
		String[] c = new String[2];
		if (id == null)
			return c;
		Category cat = categories.get(id);
		if (cat == null)
			return c;
		if (cat.getCategory() != null) {
			c[0] = cat.getCategory().getName();
			c[1] = cat.getName();
		} else {
			c[0] = cat.getName();
		}
		return c;
	}
}
