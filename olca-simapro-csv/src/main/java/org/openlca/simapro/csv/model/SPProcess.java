package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a SimaPro process
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPProcess extends SPDataEntry {

	/**
	 * The reference products of the process
	 */
	private List<SPReferenceProduct> referenceProducts = new ArrayList<SPReferenceProduct>();

	/**
	 * Creates a new process
	 * 
	 * @param referenceProduct
	 *            The reference product of the process
	 */
	public SPProcess(SPReferenceProduct referenceProduct) {
		this.referenceProducts.add(referenceProduct);
	}

	/**
	 * Creates a new process
	 * 
	 * @param referenceProduct
	 *            The reference product of the process
	 * @param subCategory
	 *            The sub category of the process
	 * @param documentation
	 *            The documentation of the process
	 */
	public SPProcess(SPReferenceProduct referenceProduct, String subCategory,
			SPDocumentation documentation) {
		this.referenceProducts.add(referenceProduct);
		setSubCategory(subCategory);
		setDocumentation(documentation);
	}

	/**
	 * Adds a reference product to the process
	 * 
	 * @param product
	 *            The product to add
	 */
	public void add(SPReferenceProduct product) {
		referenceProducts.add(product);
	}

	/**
	 * Getter of the reference products
	 * 
	 * @see SPReferenceProduct
	 * @return The product outputs of the process
	 */
	public SPReferenceProduct[] getReferenceProducts() {
		return referenceProducts
				.toArray(new SPReferenceProduct[referenceProducts.size()]);
	}

	public SPFlow[] getFlows() {
		List<SPFlow> flows = new ArrayList<SPFlow>();
		for (SPFlow flow : getElementaryFlows()) {
			flows.add(flow);
		}
		for (SPFlow flow : getProductFlows()) {
			flows.add(flow);
		}
		for (SPFlow flow : getReferenceProducts()) {
			flows.add(flow);
		}
		return flows.toArray(new SPFlow[flows.size()]);
	}

}
