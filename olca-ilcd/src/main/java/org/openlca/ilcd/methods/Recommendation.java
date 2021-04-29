package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.RecommendationLevel;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.annotations.FreeText;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecommendationType", propOrder = {
		"entities",
		"level",
		"meaning"
})
public class Recommendation implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToEntity")
	public final List<Ref> entities = new ArrayList<>();

	public RecommendationLevel level;

	@FreeText
	public final List<LangString> meaning = new ArrayList<>();

}
