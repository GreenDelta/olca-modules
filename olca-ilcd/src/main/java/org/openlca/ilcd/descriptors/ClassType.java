
package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class ClassType implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlValue
	public String content;

	@XmlAttribute(name = "level", required = true)
	public int level;

}
