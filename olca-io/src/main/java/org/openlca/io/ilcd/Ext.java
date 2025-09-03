package org.openlca.io.ilcd;

import java.util.Optional;

import javax.xml.namespace.QName;

import org.openlca.ilcd.processes.Parameter;
import org.openlca.util.Strings;

/// It would be better to move openLCA specific extensions of the ILCD format
/// into the olca-io module. This utility class contains extension handling
/// that is not available in the olca-ilcd module.
public class Ext {

	private static final String NAMESPACE = "http://openlca.org/ilcd-extensions";

	private Ext() {
	}

	public static void setUUID(Parameter param, String uuid) {
		if (param == null || uuid == null)
			return;
		param.withOtherAttributes().put(q("uuid"), uuid);
	}

	public static Optional<String> getUUID(Parameter param) {
		if (param == null)
			return Optional.empty();
		var props = param.getOtherAttributes();
		if (props == null)
			return Optional.empty();
		var uuid = props.get(q("uuid"));
		return Strings.notEmpty(uuid)
				? Optional.of(uuid)
				: Optional.empty();
	}

	private static QName q(String attribute) {
		return new QName(NAMESPACE, attribute, "olca");
	}

}
