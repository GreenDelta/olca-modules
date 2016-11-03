
package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClassType", propOrder = {
		"value"
})
public class Category implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlValue
	public String value;

	/**
	 * If more than one class is specified in a hierarchical classification
	 * system, the hierarchy level (1,2,...) could be specified with this
	 * attribute of class.
	 */
	@XmlAttribute(name = "level", required = true)
	public int level;

	/**
	 * Unique identifier for the class. [ If such identifiers are also defined
	 * in the referenced category file, they should be identical. Identifiers
	 * can be UUID's, but also other forms are allowed.]
	 */
	@XmlAttribute(name = "classId")
	public String classId;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Category clone() {
		Category clone = new Category();
		clone.value = value;
		clone.level = level;
		clone.classId = classId;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
