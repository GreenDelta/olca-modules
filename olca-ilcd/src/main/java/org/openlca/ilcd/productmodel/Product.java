package org.openlca.ilcd.productmodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Product {

	@XmlAttribute
	protected String uuid;

	@XmlAttribute
	protected String uri;

	@XmlAttribute
	protected String name;

	protected ConsumedBy consumedBy;

	public Product() {

	}

	public Product(String uuid, String uri, String name, ConsumedBy consumedBy) {
		this.uuid = uuid;
		this.uri = uri;
		this.name = name;
		this.consumedBy = consumedBy;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the consumedBy
	 */
	public ConsumedBy getConsumedBy() {
		return consumedBy;
	}

	/**
	 * @param consumedBy
	 *            the consumedBy to set
	 */
	public void setConsumedBy(ConsumedBy consumedBy) {
		this.consumedBy = consumedBy;
	}

}
