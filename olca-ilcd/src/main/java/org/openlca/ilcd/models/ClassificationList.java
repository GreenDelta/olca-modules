package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Classification;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
		"classifications"
})
public class ClassificationList {

	@XmlElement(name = "classification", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Classification> classifications = new ArrayList<>();

}
