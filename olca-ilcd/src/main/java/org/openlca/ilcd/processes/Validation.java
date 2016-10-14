package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidationType", propOrder = { "reviews", "other" })
public class Validation implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "review")
	public final List<Review> reviews = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Validation clone() {
		Validation clone = new Validation();
		for (Review r : reviews) {
			if (r == null)
				continue;
			clone.reviews.add(r.clone());
		}
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}