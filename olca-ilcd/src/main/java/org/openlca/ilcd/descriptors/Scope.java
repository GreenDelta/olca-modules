package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.ReviewScope;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "method" })
@XmlRootElement(name = "scope")
public class Scope implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<Method> method = new ArrayList<>();

	@XmlAttribute(name = "name", required = true)
	public ReviewScope name;

}
