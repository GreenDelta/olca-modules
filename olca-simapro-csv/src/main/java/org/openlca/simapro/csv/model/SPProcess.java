package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.process.SPProductOutput;

/**
 * This class represents a SimaPro process
 */
public class SPProcess extends SPDataSet {

	private SPProductOutput referenceProduct = null;
	private List<SPProductOutput> byProducts = new ArrayList<SPProductOutput>();

	public SPProcess(SPProductOutput referenceProduct) {
		this.referenceProduct = referenceProduct;
	}

	public SPProcess(SPProductOutput referenceProduct, String subCategory,
			SPProcessDocumentation documentation) {
		this.referenceProduct = referenceProduct;
		setSubCategory(subCategory);
		setDocumentation(documentation);
	}

	public SPProductOutput getReferenceProduct() {
		return referenceProduct;
	}

	public void setReferenceProduct(SPProductOutput referenceProduct) {
		this.referenceProduct = referenceProduct;
	}

	public void add(SPProductOutput byProduct) {
		byProducts.add(byProduct);
	}

	public SPProductOutput[] getByProducts() {
		return byProducts.toArray(new SPProductOutput[byProducts.size()]);
	}

	public SPExchange[] getFlows() {
		List<SPExchange> flows = new ArrayList<SPExchange>();
		for (SPExchange flow : getElementaryFlows()) {
			flows.add(flow);
		}
		for (SPExchange flow : getProductFlows()) {
			flows.add(flow);
		}
		for (SPExchange flow : getByProducts()) {
			flows.add(flow);
		}
		return flows.toArray(new SPExchange[flows.size()]);
	}

}
