package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAttribute;

abstract class XmiVariableView extends XmiBasicView {

	@XmlAttribute(name = "name")
	String name;

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
