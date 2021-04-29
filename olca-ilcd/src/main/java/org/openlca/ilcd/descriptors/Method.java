
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import org.openlca.ilcd.commons.ReviewMethod;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "method")
public class Method implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlAttribute(name = "name", required = true)
	public ReviewMethod name;

}
