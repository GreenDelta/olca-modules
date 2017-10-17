package org.openlca.io.xls.systems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.io.CategoryPair;
import org.openlca.io.DisplayValues;
import org.openlca.util.Strings;

/**
 * A class for showing the essential information of a flow to the user.
 */
class FlowInfo implements Comparable<FlowInfo> {

	private long realId;
	private String id;
	private String name;
	private String unit;
	private String category;
	private String subCategory;
	private String location;

	public static List<FlowInfo> getAll(SystemExportConfig conf, FlowIndex index) {
		EntityCache cache = conf.getEntityCache();
		Set<FlowDescriptor> flows = getFlowDescriptors(cache, index);
		List<FlowInfo> infos = new ArrayList<>();
		for (FlowDescriptor flow : flows) {
			CategoryPair catPair = CategoryPair.create(flow, cache);
			FlowInfo info = new FlowInfo();
			info.realId = flow.getId();
			info.setId(flow.getRefId());
			info.setName(flow.getName());
			info.setCategory(catPair.getCategory());
			info.setSubCategory(catPair.getSubCategory());
			if (flow.getLocation() != null) {
				Location location = cache.get(Location.class,
						flow.getLocation());
				if (location != null)
					info.setLocation(location.getCode());
			}
			String unit = DisplayValues.referenceUnit(flow, cache);
			info.setUnit(unit);
			infos.add(info);
		}
		return infos;
	}

	private static Set<FlowDescriptor> getFlowDescriptors(EntityCache cache,
	                                                      FlowIndex index) {
		if (index == null)
			return Collections.emptySet();
		List<Long> ids = new ArrayList<>(index.size());
		for (long id : index.getFlowIds())
			ids.add(id);
		Map<Long, FlowDescriptor> values = cache.getAll(FlowDescriptor.class, ids);
		HashSet<FlowDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	public long getRealId() {
		return realId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	@Override
	public int compareTo(FlowInfo other) {
		if (other == null)
			return 1;
		int c = Strings.compare(this.name, other.name);
		if (c != 0)
			return c;
		c = Strings.compare(this.category, other.category);
		if (c != 0)
			return c;
		c = Strings.compare(this.subCategory, other.subCategory);
		return c;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlowInfo other = (FlowInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
