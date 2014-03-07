package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.process.ProductOutputRow;

/**
 * This class represents a SimaPro process
 */
public class SPProcess extends SPDataSet {

	private ProductOutputRow referenceProduct = null;
	private List<ProductOutputRow> byProducts = new ArrayList<ProductOutputRow>();

	public SPProcess(ProductOutputRow referenceProduct) {
		this.referenceProduct = referenceProduct;
	}

	public SPProcess(ProductOutputRow referenceProduct, String subCategory,
			SPProcessDocumentation documentation) {
		this.referenceProduct = referenceProduct;
		setSubCategory(subCategory);
		setDocumentation(documentation);
	}

	public ProductOutputRow getReferenceProduct() {
		return referenceProduct;
	}

	public void setReferenceProduct(ProductOutputRow referenceProduct) {
		this.referenceProduct = referenceProduct;
	}

	public void add(ProductOutputRow byProduct) {
		byProducts.add(byProduct);
	}

	public ProductOutputRow[] getByProducts() {
		return byProducts.toArray(new ProductOutputRow[byProducts.size()]);
	}

	public AbstractExchangeRow[] getFlows() {
		List<AbstractExchangeRow> flows = new ArrayList<AbstractExchangeRow>();
		for (AbstractExchangeRow flow : getElementaryFlows()) {
			flows.add(flow);
		}
		for (AbstractExchangeRow flow : getProductFlows()) {
			flows.add(flow);
		}
		for (AbstractExchangeRow flow : getByProducts()) {
			flows.add(flow);
		}
		return flows.toArray(new AbstractExchangeRow[flows.size()]);
	}

}
