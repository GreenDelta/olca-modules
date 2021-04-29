package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.ReviewScope;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "method" })
@XmlRootElement(name = "scope")
public class Scope implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<Method> method = new ArrayList<>();

	@XmlAttribute(name = "name", required = true)
	public ReviewScope name;

}
