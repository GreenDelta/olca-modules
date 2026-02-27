package org.openlca.sd.xmile;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public final class XmiStock extends XmiEvaluatable {

	@XmlElement(name = "inflow", namespace = Xmile.NS)
	private List<String> inflows;

	@XmlElement(name = "outflow", namespace = Xmile.NS)
	private List<String> outflows;

	public List<String> inflows() {
		if (inflows == null) {
			inflows = new ArrayList<>();
		}
		return inflows;
	}

	public List<String> outflows() {
		if (outflows == null) {
			outflows = new ArrayList<>();
		}
		return outflows;
	}
}
