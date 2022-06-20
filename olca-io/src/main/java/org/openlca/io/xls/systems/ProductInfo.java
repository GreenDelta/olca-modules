package org.openlca.io.xls.systems;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Category;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.CategoryPair;
import org.openlca.util.Strings;

/**
 * A class for showing the essential information of a flow to the user.
 */
class ProductInfo implements Comparable<ProductInfo> {

	TechFlow provider;
	boolean ref;
	boolean fromInfrastructureProcess;
	boolean fromMultiOutputProcess;
	String process;
	String processCategory;
	String processId;
	String processLocation;
	String processSubCategory;
	String product;
	String productId;
	String productUnit;

	public static List<ProductInfo> getAll(SystemExportConfig conf,
			TechIndex index) {
		EntityCache cache = conf.getEntityCache();
		List<ProductInfo> infos = new ArrayList<>(index.size() + 2);
		for (int i = 0; i < index.size(); i++) {
			TechFlow pair = index.at(i);
			RootDescriptor process = pair.provider();
			FlowDescriptor product = pair.flow();
			ProductInfo info = new ProductInfo();
			info.provider = pair;
			// info.ref = pair.equals(index.getRefFlow()); TODO
			info.process = process.name;
			info.processId = process.refId;
			info.product = product.name;
			info.productId = product.refId;
			if (process.category != null) {
				Category cat = cache.get(Category.class, process.category);
				CategoryPair catPair = new CategoryPair(cat);
				info.processCategory = catPair.getCategory();
				info.processSubCategory = catPair.getSubCategory();
			}
			if (process instanceof ProcessDescriptor) {
				ProcessDescriptor p = (ProcessDescriptor) process;
				if (p.location != null) {
					Location loc = cache.get(Location.class, p.location);
					if (loc != null)
						info.processLocation = loc.code;
				}
			}
			infos.add(info);
		}
		return infos;
	}

	@Override
	public int compareTo(ProductInfo other) {
		// the reference product will be always placed before any other product
		if (this.ref)
			return -1;
		if (other == null)
			return 1;
		if (other.ref)
			return 1;
		int c = Strings.compare(this.process, other.process);
		if (c != 0)
			return c;
		c = Strings.compare(this.processCategory, other.processCategory);
		if (c != 0)
			return c;
		c = Strings.compare(this.processSubCategory, other.processSubCategory);
		if (c != 0)
			return c;
		c = Strings.compare(this.product, other.product);
		return c;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductInfo other = (ProductInfo) obj;
		return provider.equals(other.provider);
	}

	@Override
	public int hashCode() {
		return provider.hashCode();
	}

}
