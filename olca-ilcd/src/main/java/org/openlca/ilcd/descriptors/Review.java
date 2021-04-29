package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ReviewType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "scope", "dataQualityIndicators",
		"reviewDetails", "otherReviewDetails", "reviewer" })
@XmlRootElement(name = "review")
public class Review implements Serializable {

	private final static long serialVersionUID = 1L;

	public final List<Scope> scope = new ArrayList<>();

	public DataQualityIndicators dataQualityIndicators;

	public LangString reviewDetails;

	public LangString otherReviewDetails;

	public DataSetReference reviewer;

	@XmlAttribute(name = "type", required = true)
	public ReviewType type;

}
