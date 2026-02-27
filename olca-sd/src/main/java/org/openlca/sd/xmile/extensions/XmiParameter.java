package org.openlca.sd.xmile.extensions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.openlca.core.model.ModelType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Parameter", namespace = XmileExtensions.NS)
public class XmiParameter {

	@XmlAttribute(name = "name")
	public String name;

	@XmlAttribute(name = "value")
	public double value;

	@XmlAttribute(name = "description")
	public String description;

	@XmlAttribute(name = "contextId")
	public String contextId;

	@XmlAttribute(name = "contextType")
	public ModelType contextType;

}
