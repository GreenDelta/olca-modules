
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeType", propOrder = {
		"referenceYear",
		"duration",
		"description"
})
public class Time implements Serializable {

	private final static long serialVersionUID = 1L;

	public Integer referenceYear;

	@Label
	public final List<LangString> duration = new ArrayList<>();

	@FreeText
	@XmlElement(name = "timeRepresentativenessDescription")
	public final List<LangString> description = new ArrayList<>();

}
