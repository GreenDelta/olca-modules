package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataQualityIndicator;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ReviewMethod;
import org.openlca.ilcd.commons.ReviewScope;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReviewType", propOrder = { "scopes", "indicators",
		"details", "reviewers",
		"otherDetails", "report", "other" })
public class Review implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "scope")
	public final List<Scope> scopes = new ArrayList<>();

	@XmlElementWrapper(namespace = "http://lca.jrc.it/ILCD/Common", name = "dataQualityIndicators")
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "dataQualityIndicator")
	public DataQualityIndicator[] indicators;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "reviewDetails")
	public final List<LangString> details = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToNameOfReviewerAndInstitution")
	public final List<Ref> reviewers = new ArrayList<>();

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "otherReviewDetails")
	public final List<LangString> otherDetails = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToCompleteReviewReport")
	public Ref report;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "type")
	public ReviewType type;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "methods" })
	public static class Scope implements Serializable {

		private final static long serialVersionUID = 1L;

		@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common", name = "method")
		public final List<Method> methods = new ArrayList<>();

		@XmlAttribute(name = "name", required = true)
		public ReviewScope name;

		@Override
		public Scope clone() {
			Scope clone = new Scope();
			for (Method m : methods) {
				if (m == null)
					continue;
				clone.methods.add(m.clone());
			}
			clone.name = name;
			return clone;
		}

	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Method implements Serializable {

		private final static long serialVersionUID = 1L;

		@XmlAttribute(name = "name", required = true)
		public ReviewMethod name;

		@Override
		public Method clone() {
			Method clone = new Method();
			clone.name = name;
			return clone;
		}
	}

	@Override
	public Review clone() {
		Review clone = new Review();
		for (Scope s : scopes)
			clone.scopes.add(s.clone());
		if (indicators != null) {
			clone.indicators = new DataQualityIndicator[indicators.length];
			for (int i = 0; i < indicators.length; i++) {
				if (indicators[i] == null)
					continue;
				clone.indicators[i] = indicators[i].clone();
			}
		}
		LangString.copy(details, clone.details);
		Ref.copy(reviewers, clone.reviewers);
		LangString.copy(otherDetails, clone.otherDetails);
		if (report != null)
			clone.report = report.clone();
		if (other != null)
			clone.other = other.clone();
		clone.type = type;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
