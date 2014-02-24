package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a SimaPro process
 */
public class SPProcess extends SPDataSet {

	private SPProduct referenceProduct = null;
	private List<SPProduct> byProducts = new ArrayList<SPProduct>();

	public SPProcess(SPProduct referenceProduct) {
		this.referenceProduct = referenceProduct;
	}

	public SPProcess(SPProduct referenceProduct, String subCategory,
			SPProcessDocumentation documentation) {
		this.referenceProduct = referenceProduct;
		setSubCategory(subCategory);
		setDocumentation(documentation);
	}

	public SPProduct getReferenceProduct() {
		return referenceProduct;
	}

	public void setReferenceProduct(SPProduct referenceProduct) {
		this.referenceProduct = referenceProduct;
	}

	public void add(SPProduct byProduct) {
		byProducts.add(byProduct);
	}

	public SPProduct[] getByProducts() {
		return byProducts.toArray(new SPProduct[byProducts.size()]);
	}

	public SPFlow[] getFlows() {
		List<SPFlow> flows = new ArrayList<SPFlow>();
		for (SPFlow flow : getElementaryFlows()) {
			flows.add(flow);
		}
		for (SPFlow flow : getProductFlows()) {
			flows.add(flow);
		}
		for (SPFlow flow : getByProducts()) {
			flows.add(flow);
		}
		return flows.toArray(new SPFlow[flows.size()]);
	}

}
