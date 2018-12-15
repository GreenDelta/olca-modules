package org.openlca.io.xls.systems;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.Provider;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.Category;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.CategoryPair;
import org.openlca.util.Strings;

/**
 * A class for showing the essential information of a flow to the user.
 */
class ProductInfo implements Comparable<ProductInfo> {

	Provider provider;
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
			Provider pair = index.getProviderAt(i);
			CategorizedDescriptor process = pair.entity;
			FlowDescriptor product = pair.flow;
			ProductInfo info = new ProductInfo();
			info.provider = pair;
			info.ref = pair.equals(index.getRefFlow());
			info.process = process.getName();
			info.processId = process.getRefId();
			info.product = product.getName();
			info.productId = product.getRefId();
			if (process.getCategory() != null) {
				Category cat = cache.get(Category.class, process.getCategory());
				CategoryPair catPair = new CategoryPair(cat);
				info.processCategory = catPair.getCategory();
				info.processSubCategory = catPair.getSubCategory();
			}
			if (process instanceof ProcessDescriptor) {
				ProcessDescriptor p = (ProcessDescriptor) process;
				if (p.getLocation() != null) {
					Location loc = cache.get(Location.class, p.getLocation());
					if (loc != null)
						info.processLocation = loc.getCode();
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
