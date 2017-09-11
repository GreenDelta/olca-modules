package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
