package org.openlca.sd.xmile.extensions;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.openlca.core.model.AllocationMethod;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SystemBinding", namespace = XmileExtensions.NS)
public class XmiSystemBinding {

	@XmlAttribute(name = "system")
	public String system;

	@XmlAttribute(name = "allocation")
	public AllocationMethod allocation;

	@XmlAttribute(name = "amount")
	public Double amount;

	@XmlElement(name = "varBinding", namespace = XmileExtensions.NS)
	public final List<XmiVarBinding> varBindings = new ArrayList<>();

}
