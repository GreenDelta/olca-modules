package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "clazz" })
@XmlRootElement(name = "classification", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
public class Classification implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "class", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", required = true)
	public final List<ClassType> clazz = new ArrayList<>();

}
