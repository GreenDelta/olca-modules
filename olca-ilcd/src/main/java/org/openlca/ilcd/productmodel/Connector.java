package org.openlca.ilcd.productmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Connector {

	@XmlAttribute
	protected String id;

	@XmlAttribute
	protected String origin;

	@XmlElement(name = "product", namespace = "http://iai.kit.edu/ILCD/ProductModel", type = Product.class)
	protected List<Product> products = null;

	public Connector() {
	}

	public Connector(String id) {
		this.id = id;
	}

	public Connector(String id, String origin) {
		this(id);
		this.origin = origin;
	}

	public Connector(String id, String origin, Product product) {
		this(id, origin);
		this.getProducts().add(product);
	}

	/**
	 * @return the products
	 */
	public List<Product> getProducts() {
		if (products == null)
			products = new ArrayList<>();
		return products;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

}
