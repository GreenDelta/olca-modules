package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

public class CalculationSetup {

	private ProductSystem productSystem;
	private ImpactMethodDescriptor impactMethod;
	private List<ImpactCategoryDescriptor> impactCategories = new ArrayList<>();

	public ProductSystem getProductSystem() {
		return productSystem;
	}

	public void setProductSystem(ProductSystem productSystem) {
		this.productSystem = productSystem;
	}

	public ImpactMethodDescriptor getImpactMethod() {
		return impactMethod;
	}

	public void setImpactMethod(ImpactMethodDescriptor impactMethod) {
		this.impactMethod = impactMethod;
	}

	public List<ImpactCategoryDescriptor> getImpactCategories() {
		return impactCategories;
	}

	public Process getReferenceProcess() {
		if (productSystem != null)
			return productSystem.getReferenceProcess();
		return null;
	}

	public Exchange getReferenceExchange() {
		if (productSystem != null)
			return productSystem.getReferenceExchange();
		return null;
	}

}
