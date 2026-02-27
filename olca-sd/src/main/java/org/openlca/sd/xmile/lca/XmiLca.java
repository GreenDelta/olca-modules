package org.openlca.sd.xmile.lca;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiLca {

	public static final String NS = "https://openlca.org/xmile-extensions";
	public static final String PREFIX = "olca";

	@XmlElement(name = "impactMethod", namespace = XmiLca.NS)
	private XmiEntityRef impactMethod;

	@XmlElement(name = "systemBinding", namespace = XmiLca.NS)
	private List<XmiSystemBinding> systemBindings;

	public XmiEntityRef impactMethod() {
		return impactMethod;
	}

	public void setImpactMethod(XmiEntityRef impactMethod) {
		this.impactMethod = impactMethod;
	}

	public List<XmiSystemBinding> systemBindings() {
		if (systemBindings == null) {
			systemBindings = new ArrayList<>();
		}
		return systemBindings;
	}
}
