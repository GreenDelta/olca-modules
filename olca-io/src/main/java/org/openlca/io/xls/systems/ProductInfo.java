package org.openlca.io.xls.systems;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.Category;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.CategoryPair;
import org.openlca.util.Strings;

/**
 * A class for showing the essential information of a flow to the user.
 */
class ProductInfo implements Comparable<ProductInfo> {

	private LongPair longPair;
	private boolean ref;
	private boolean fromInfrastructureProcess;
	private boolean fromMultiOutputProcess;
	private String process;
	private String processCategory;
	private String processId;
	private String processLocation;
	private String processSubCategory;
	private String product;
	private String productId;
	private String productUnit;

	public static List<ProductInfo> getAll(SystemExportConfig conf,
			TechIndex index) {
		EntityCache cache = conf.getEntityCache();
		List<ProductInfo> infos = new ArrayList<>(index.size() + 2);
		for (int i = 0; i < index.size(); i++) {
			LongPair pair = index.getProviderAt(i);
			ProcessDescriptor process = cache.get(ProcessDescriptor.class,
					pair.getFirst());
			FlowDescriptor product = cache.get(FlowDescriptor.class,
					pair.getSecond());
			ProductInfo info = new ProductInfo();
			info.longPair = pair;
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
			if (process.getLocation() != null) {
				Location loc = cache.get(Location.class, process.getLocation());
				if (loc != null)
					info.processLocation = loc.getCode();
			}
			infos.add(info);
		}
		return infos;
	}

	public LongPair getLongPair() {
		return longPair;
	}

	public boolean isRef() {
		return ref;
	}

	public String getProcess() {
		return process;
	}

	public String getProcessCategory() {
		return processCategory;
	}

	public String getProcessId() {
		return processId;
	}

	public String getProcessLocation() {
		return processLocation;
	}

	public String getProcessSubCategory() {
		return processSubCategory;
	}

	public String getProduct() {
		return product;
	}

	public String getProductId() {
		return productId;
	}

	public String getProductUnit() {
		return productUnit;
	}

	public boolean isFromInfrastructureProcess() {
		return fromInfrastructureProcess;
	}

	public boolean isFromMultiOutputProcess() {
		return fromMultiOutputProcess;
	}

	public void setFromInfrastructureProcess(boolean fromInfrastructureProcess) {
		this.fromInfrastructureProcess = fromInfrastructureProcess;
	}

	public void setFromMultiOutputProcess(boolean fromMultiOutputProcess) {
		this.fromMultiOutputProcess = fromMultiOutputProcess;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public void setProcessCategory(String processCategory) {
		this.processCategory = processCategory;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public void setProcessLocation(String processLocation) {
		this.processLocation = processLocation;
	}

	public void setProcessSubCategory(String processSubCategory) {
		this.processSubCategory = processSubCategory;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public void setProductUnit(String productUnit) {
		this.productUnit = productUnit;
	}

	@Override
	public int compareTo(ProductInfo other) {
		// the reference product will be always placed before any other product
		if (this.isRef())
			return -1;
		if (other == null)
			return 1;
		if (other.isRef())
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
		return longPair.equals(other.longPair);
	}

	@Override
	public int hashCode() {
		return longPair.hashCode();
	}

}
