package org.openlca.ilcd.epd.conversion;

import java.util.Optional;

import javax.xml.namespace.QName;

public class Vocab {

	static final String NS_EPD = "http://www.iai.kit.edu/EPD/2013";
	static final String NS_OLCA = "http://openlca.org/epd_ilcd";
	static final String NS_XML = "http://www.w3.org/XML/1998/namespace";
	public static final String NS_EPDv2 = "http://www.indata.network/EPD/2019";
	public static final String SBE_ILCD = "http://sbeilcd.se/EPD/2017";

	private Vocab() {
	}

	public static final QName PROFILE_ATTR = new QName(
		"http://www.okworx.com/ILCD/Extensions/2018/Profile", "profile");

	/**
	 * Returns a comment prefix for the given namespace if such a prefix exists.
	 */
	public static Optional<String> prefixOf(String namespace) {
		if (namespace == null)
			return Optional.empty();
		return switch (namespace) {
			case NS_EPD -> Optional.of("epd");
			case NS_EPDv2 -> Optional.of("epd2");
			case NS_OLCA -> Optional.of("olca");
			case SBE_ILCD -> Optional.of("sbe");
			case NS_XML -> Optional.of("xml");
			default -> Optional.empty();
		};
	}

}
