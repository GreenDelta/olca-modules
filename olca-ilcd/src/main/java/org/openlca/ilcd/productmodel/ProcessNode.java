package org.openlca.ilcd.productmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessNode {

	@XmlAttribute
	protected String id;

	@XmlAttribute
	protected String uuid;

	@XmlAttribute
	protected String uri;

	@XmlAttribute
	protected String name;

	@XmlElement(name = "parameter", namespace = "http://iai.kit.edu/ILCD/ProductModel", type = Parameter.class)
	protected List<Parameter> parameters = null;

	public ProcessNode() {
	}

	public ProcessNode(String id, String uuid, String uri) {
		this(id, uuid, uri, null);
	}

	public ProcessNode(String id, String uuid, String uri, String name) {
		this.id = id;
		this.uuid = uuid;
		this.uri = uri;
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param nodeId
	 *            the nodeId to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the references
	 */
	public List<Parameter> getParameters() {
		if (parameters == null)
			parameters = new ArrayList<>();
		return parameters;
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

}
