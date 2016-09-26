package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClassificationInformationType", propOrder = { "classifications" })
public class ClassificationInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "classification")
	public final List<Classification> classifications = new ArrayList<>();

}
