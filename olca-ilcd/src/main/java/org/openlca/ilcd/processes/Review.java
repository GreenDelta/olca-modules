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
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataQualityIndicatorList;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ReviewMethod;
import org.openlca.ilcd.commons.ReviewScope;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReviewType", propOrder = { "scope", "dataQualityIndicators",
		"reviewDetails", "referenceToNameOfReviewerAndInstitution",
		"otherReviewDetails", "referenceToCompleteReviewReport", "other" })
public class Review implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Review.Scope> scope = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public DataQualityIndicatorList dataQualityIndicators;

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> reviewDetails = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<DataSetReference> referenceToNameOfReviewerAndInstitution = new ArrayList<>();

	@FreeText
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> otherReviewDetails = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public DataSetReference referenceToCompleteReviewReport;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAttribute(name = "type")
	public ReviewType type;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "method" })
	public static class Scope implements Serializable {

		private final static long serialVersionUID = 1L;

		@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
		public final List<Review.Scope.Method> method = new ArrayList<>();

		@XmlAttribute(name = "name", required = true)
		public ReviewScope name;

		@XmlAnyAttribute
		public final Map<QName, String> otherAttributes = new HashMap<>();

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "")
		public static class Method implements Serializable {

			private final static long serialVersionUID = 1L;

			@XmlAttribute(name = "name", required = true)
			public ReviewMethod name;

			@XmlAnyAttribute
			public final Map<QName, String> otherAttributes = new HashMap<>();

		}
	}
}
