package org.openlca.sd.xmile.extensions;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.openlca.sd.xmile.Xmile;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Extensions", namespace = XmileExtensions.NS)
public class XmiExtensions {

	@XmlElement(name = "impactMethod", namespace = XmileExtensions.NS)
	public String impactMethod;

	@XmlElement(name = "systemBinding", namespace = XmileExtensions.NS)
	public final List<XmiSystemBinding> systemBindings = new ArrayList<>();

}
