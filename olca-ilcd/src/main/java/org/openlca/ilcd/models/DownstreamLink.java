package org.openlca.ilcd.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class DownstreamLink {

	@XmlAttribute(name = "id")
	public int process;

	@XmlAttribute(name = "flowUUID")
	public String inputFlow;

	@XmlAttribute(name = "location")
	public String location;

	@XmlAttribute(name = "dominant")
	public Boolean isDominant;

}
