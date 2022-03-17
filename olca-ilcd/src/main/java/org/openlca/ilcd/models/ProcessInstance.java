package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Ref;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "process", "scalingFactor", "groupRefs", "parameters", "connections" })
public class ProcessInstance {

	@XmlAttribute(name = "dataSetInternalID")
	public int id;

	@XmlAttribute(name = "multiplicationFactor")
	public double multiplicationFactor;

	@XmlElement(name = "referenceToProcess")
	public Ref process;

	@XmlElement(name = "scalingFactor")
	public Double scalingFactor;

	@XmlElementWrapper(name = "groups")
	@XmlElement(name = "memberOf")
	public List<GroupRef> groupRefs = new ArrayList<>();

	@XmlElementWrapper(name = "parameters")
	@XmlElement(name = "parameter")
	public List<Parameter> parameters = new ArrayList<>();

	@XmlElementWrapper(name = "connections")
	@XmlElement(name = "outputExchange")
	public List<Connection> connections = new ArrayList<>();

}
