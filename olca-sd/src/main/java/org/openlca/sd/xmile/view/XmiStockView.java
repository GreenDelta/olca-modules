package org.openlca.sd.xmile.view;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiStockView extends XmiVariableView {

	@XmlAttribute(name = "label_side")
	private String labelSide;

	public String labelSide() {
		return labelSide;
	}

	public void setLabelSide(String labelSide) {
		this.labelSide = labelSide;
	}
}
