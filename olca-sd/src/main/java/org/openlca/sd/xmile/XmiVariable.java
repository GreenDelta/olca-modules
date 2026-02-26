package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlAttribute;

public sealed abstract class XmiVariable
	permits XmiEvaluatable, XmiGf  {

	@XmlAttribute(name = "name")
	String name;

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
