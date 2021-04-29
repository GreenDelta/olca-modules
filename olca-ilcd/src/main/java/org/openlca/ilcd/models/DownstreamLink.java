package org.openlca.ilcd.models;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

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

	/**
	 * This is an extension attribute to support linking of exchanges that have
	 * the same flow. In the eILCD format this is only possible when these
	 * exchanges have different locations. However, in openLCA you can for
	 * example have multiple inputs of the same product that come from different
	 * providers (without location attributes). To solve this case we add the
	 * internal exchange ID of the linked exchange to the connection.
	 */
	@XmlAttribute(name = "linkedExchange", namespace = "http://openlca.org/ilcd-extensions")
	public Integer linkedExchange;

}
