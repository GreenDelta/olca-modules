package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidationType", propOrder = { "reviews" })
public class Validation implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "review", required = true)
	public final List<Review> reviews = new ArrayList<>();

}
