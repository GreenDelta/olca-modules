package org.openlca.sd.xmile.extensions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VarBinding", namespace = XmileExtensions.NS)
public class XmiVarBinding {

	@XmlAttribute(name = "var")
	public String var;

	@XmlElement(name = "parameter", namespace = XmileExtensions.NS)
	public XmiParameter parameter;

}
