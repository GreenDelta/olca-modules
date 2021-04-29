package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "clazz" })
@XmlRootElement(name = "classification", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
public class Classification implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "class", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", required = true)
	public final List<ClassType> clazz = new ArrayList<>();

}
