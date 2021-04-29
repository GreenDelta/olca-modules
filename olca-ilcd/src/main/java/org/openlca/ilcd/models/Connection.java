package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "downstreamLinks" })
public class Connection {

	@XmlAttribute(name = "flowUUID")
	public String outputFlow;

	@XmlAttribute(name = "dominant")
	public Boolean isDominant;

	@XmlElement(name = "downstreamProcess")
	public List<DownstreamLink> downstreamLinks = new ArrayList<>();

}
