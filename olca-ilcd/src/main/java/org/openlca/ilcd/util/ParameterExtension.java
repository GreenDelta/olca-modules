package org.openlca.ilcd.util;

import javax.xml.namespace.QName;

import org.openlca.ilcd.processes.Parameter;

public class ParameterExtension {

	private final String SCOPE = "scope";
	private Parameter parameter;

	public ParameterExtension(Parameter parameter) {
		this.parameter = parameter;
	}

	public void setScope(String scope) {
		if (parameter == null || scope == null)
			return;
		QName qName = Extensions.getQName(SCOPE);
		parameter.otherAttributes.put(qName, scope);
	}

	public String getScope() {
		if (parameter == null)
			return null;
		QName qName = Extensions.getQName(SCOPE);
		return parameter.otherAttributes.get(qName);
	}

}
