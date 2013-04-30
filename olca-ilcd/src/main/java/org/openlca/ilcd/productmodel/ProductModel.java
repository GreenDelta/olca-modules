package org.openlca.ilcd.productmodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.processes.Process;

@XmlRootElement(name = "productModel", namespace = "http://iai.kit.edu/ILCD/ProductModel")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "parameters", "nodes", "connections" })
@XmlSeeAlso(Process.class)
public class ProductModel {

	@XmlAttribute
	protected String name;

	@XmlElementWrapper(name = "parameters")
	@XmlElement(name = "parameter", namespace = "http://iai.kit.edu/ILCD/ProductModel", type = Parameter.class)
	protected List<Parameter> parameters = null;

	@XmlElementWrapper(name = "nodes")
	@XmlElement(name = "process", namespace = "http://iai.kit.edu/ILCD/ProductModel", type = ProcessNode.class)
	protected List<ProcessNode> nodes = null;

	@XmlElementWrapper(name = "connections")
	@XmlElement(name = "connector", namespace = "http://iai.kit.edu/ILCD/ProductModel", type = Connector.class)
	protected List<Connector> connections = null;

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
	 * @return the nodes
	 */
	public List<ProcessNode> getNodes() {
		if (nodes == null)
			nodes = new ArrayList<>();
		return nodes;
	}

	/**
	 * @return the connections
	 */
	public List<Connector> getConnections() {
		if (connections == null)
			connections = new ArrayList<>();
		return connections;
	}

	/**
	 * @return the parameters
	 */
	public List<Parameter> getParameters() {
		if (parameters == null)
			parameters = new ArrayList<>();
		return parameters;
	}

}
