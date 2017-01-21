package org.openlca.ilcd.util;

import javax.xml.namespace.QName;

class Extensions {

	public static final String NAMESPACE = "http://openlca.org/ilcd-extensions";

	private Extensions() {
	}

	public static QName getQName(String attribute) {
		return new QName(NAMESPACE, attribute, "olca");
	}

}
