package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.commons.annotations.FreeText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReviewType", propOrder = {
		"scopes",
		"details",
		"reviewers",
		"otherDetails",
		"report"
})
public class Review implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "scope")
	public final List<Review.Scope> scopes = new ArrayList<>();

	@FreeText
	@XmlElement(name = "reviewDetails", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> details = new ArrayList<>();

	@XmlElement(name = "referenceToNameOfReviewerAndInstitution", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Ref> reviewers = new ArrayList<>();

	@FreeText
	@XmlElement(name = "otherReviewDetails", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<LangString> otherDetails = new ArrayList<>();

	@XmlElement(name = "referenceToCompleteReviewReport", namespace = "http://lca.jrc.it/ILCD/Common")
	public Ref report;

	@XmlAttribute(name = "type", required = true)
	public ReviewType type;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "methods" })
	public static class Scope implements Serializable {

		private final static long serialVersionUID = 1L;

		@XmlElement(name = "method")
		public final List<Review.Scope.Method> methods = new ArrayList<>();

		@XmlAttribute(name = "name", required = true)
		public ReviewScope name;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "")
		public static class Method implements Serializable {

			private final static long serialVersionUID = 1L;

			@XmlAttribute(name = "name", required = true)
			public ReviewMethod name;

		}

	}

}
