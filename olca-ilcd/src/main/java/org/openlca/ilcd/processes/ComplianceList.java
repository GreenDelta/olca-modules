package org.openlca.ilcd.processes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ComplianceList {

	@XmlElement(name = "compliance", required = true)
	public final List<ComplianceDeclaration> entries = new ArrayList<>();

	@Override
	public ComplianceList clone() {
		ComplianceList clone = new ComplianceList();
		for (ComplianceDeclaration e : entries) {
			if (e != null)
				clone.entries.add(e.clone());
		}
		return clone;
	}

}
