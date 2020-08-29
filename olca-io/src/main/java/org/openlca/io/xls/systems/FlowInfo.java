package org.openlca.io.xls.systems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

	long realId;
	String id;
	String name;
	String unit;
	String category;
	String subCategory;
	String location;

	public static List<FlowInfo> getAll(SystemExportConfig conf,
			FlowIndex index) {
		EntityCache cache = conf.getEntityCache();
		Set<FlowDescriptor> flows = getFlowDescriptors(index);
		List<FlowInfo> infos = new ArrayList<>();
		for (FlowDescriptor flow : flows) {
			CategoryPair catPair = CategoryPair.create(flow, cache);
			FlowInfo info = new FlowInfo();
			info.realId = flow.id;
			info.id = flow.refId;
			info.name = flow.name;
			info.category = catPair.getCategory();
			info.subCategory = catPair.getSubCategory();
			if (flow.location != null) {
				Location location = cache.get(Location.class,
						flow.location);
				if (location != null)
					info.location = location.code;
			}
			String unit = DisplayValues.referenceUnit(flow, cache);
			info.unit = unit;
			infos.add(info);
		}
		return infos;
	}

	private static Set<FlowDescriptor> getFlowDescriptors(FlowIndex index) {
		if (index == null)
			return Collections.emptySet();
		HashSet<FlowDescriptor> descriptors = new HashSet<>();
		index.each((i, f) -> {
			if (f.flow != null) {
				descriptors.add(f.flow);
			}
		});
		return descriptors;
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
		return Objects.hashCode(id);
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
		return Objects.equals(id, other.id);
	}

}
