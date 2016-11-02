package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ReviewType;

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
