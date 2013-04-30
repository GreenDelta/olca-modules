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
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ReviewMethod;
import org.openlca.ilcd.commons.ReviewScope;
import org.openlca.ilcd.commons.ReviewType;

/**
 * <p>
 * Java class for ReviewType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ReviewType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://lca.jrc.it/ILCD/Common}ValidationGroup1"/>
 *         &lt;group ref="{http://lca.jrc.it/ILCD/Common}ValidationGroup3"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://lca.jrc.it/ILCD/Common}TypeOfReviewValues" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReviewType", propOrder = { "scope", "dataQualityIndicators",
		"reviewDetails", "referenceToNameOfReviewerAndInstitution",
		"otherReviewDetails", "referenceToCompleteReviewReport", "other" })
public class Review implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<Review.Scope> scope;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected DataQualityIndicatorList dataQualityIndicators;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<FreeText> reviewDetails;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<DataSetReference> referenceToNameOfReviewerAndInstitution;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected List<FreeText> otherReviewDetails;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected DataSetReference referenceToCompleteReviewReport;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAttribute(name = "type")
	protected ReviewType type;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the scope property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the scope property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getScope().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Review.Scope }
	 * 
	 * 
	 */
	public List<Review.Scope> getScope() {
		if (scope == null) {
			scope = new ArrayList<>();
		}
		return this.scope;
	}

	/**
	 * Data quality indicators serve to provide the reviewed key information on
	 * the data set in a defined, computer-readable (and hence searchable) form.
	 * This serves to support LCA practitioners to identify/select the highest
	 * quality and most appropriate data sets.
	 * 
	 * @return possible object is {@link DataQualityIndicatorList }
	 * 
	 */
	public DataQualityIndicatorList getDataQualityIndicators() {
		return dataQualityIndicators;
	}

	/**
	 * Sets the value of the dataQualityIndicators property.
	 * 
	 * @param value
	 *            allowed object is {@link DataQualityIndicatorList }
	 * 
	 */
	public void setDataQualityIndicators(DataQualityIndicatorList value) {
		this.dataQualityIndicators = value;
	}

	/**
	 * Summary of the review. All the following items should be explicitly
	 * addressed: Representativeness, completeness, and precision of Inputs and
	 * Outputs for the process in its documented location, technology and time
	 * i.e. both completeness of technical model (product, waste, and elementary
	 * flows) and completeness of coverage of the relevant problem fields
	 * (environmental, human health, resource use) for this specific good,
	 * service, or process. Plausibility of data. Correctness and
	 * appropriateness of the data set documentation. Appropriateness of system
	 * boundaries, cut-off rules, LCI modelling choices such as e.g. allocation,
	 * consistency of included processes and of LCI methodology. If the data set
	 * comprises pre-calculated LCIA results, the correspondence of the Input
	 * and Output elementary flows (including their geographical validity) with
	 * the applied LCIA method(s) should be addressed by the reviewer. An
	 * overall quality statement on the data set may be included here.Gets the
	 * value of the reviewDetails property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the reviewDetails property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReviewDetails().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getReviewDetails() {
		if (reviewDetails == null) {
			reviewDetails = new ArrayList<>();
		}
		return this.reviewDetails;
	}

	/**
	 * Gets the value of the referenceToNameOfReviewerAndInstitution property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToNameOfReviewerAndInstitution
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToNameOfReviewerAndInstitution().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getReferenceToNameOfReviewerAndInstitution() {
		if (referenceToNameOfReviewerAndInstitution == null) {
			referenceToNameOfReviewerAndInstitution = new ArrayList<>();
		}
		return this.referenceToNameOfReviewerAndInstitution;
	}

	/**
	 * Gets the value of the otherReviewDetails property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the otherReviewDetails property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getOtherReviewDetails().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getOtherReviewDetails() {
		if (otherReviewDetails == null) {
			otherReviewDetails = new ArrayList<>();
		}
		return this.otherReviewDetails;
	}

	/**
	 * Gets the value of the referenceToCompleteReviewReport property.
	 * 
	 * @return possible object is {@link DataSetReference }
	 * 
	 */
	public DataSetReference getReferenceToCompleteReviewReport() {
		return referenceToCompleteReviewReport;
	}

	/**
	 * Sets the value of the referenceToCompleteReviewReport property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetReference }
	 * 
	 */
	public void setReferenceToCompleteReviewReport(DataSetReference value) {
		this.referenceToCompleteReviewReport = value;
	}

	/**
	 * Gets the value of the other property.
	 * 
	 * @return possible object is {@link Other }
	 * 
	 */
	public Other getOther() {
		return other;
	}

	/**
	 * Sets the value of the other property.
	 * 
	 * @param value
	 *            allowed object is {@link Other }
	 * 
	 */
	public void setOther(Other value) {
		this.other = value;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link ReviewType }
	 * 
	 */
	public ReviewType getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link ReviewType }
	 * 
	 */
	public void setType(ReviewType value) {
		this.type = value;
	}

	/**
	 * Gets a map that contains attributes that aren't bound to any typed
	 * property on this class.
	 * 
	 * <p>
	 * the map is keyed by the name of the attribute and the value is the string
	 * value of the attribute.
	 * 
	 * the map returned by this method is live, and you can add new attribute by
	 * updating the map directly. Because of this design, there's no setter.
	 * 
	 * 
	 * @return always non-null
	 */
	public Map<QName, String> getOtherAttributes() {
		return otherAttributes;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained
	 * within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="method" maxOccurs="unbounded" minOccurs="0">
	 *           &lt;complexType>
	 *             &lt;complexContent>
	 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *                 &lt;attribute name="name" use="required" type="{http://lca.jrc.it/ILCD/Common}MethodOfReviewValues" />
	 *                 &lt;anyAttribute processContents='lax' namespace='##other'/>
	 *               &lt;/restriction>
	 *             &lt;/complexContent>
	 *           &lt;/complexType>
	 *         &lt;/element>
	 *       &lt;/sequence>
	 *       &lt;attribute name="name" use="required" type="{http://lca.jrc.it/ILCD/Common}ScopeOfReviewValues" />
	 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "method" })
	public static class Scope implements Serializable {

		private final static long serialVersionUID = 1L;
		@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
		protected List<Review.Scope.Method> method;
		@XmlAttribute(name = "name", required = true)
		protected ReviewScope name;
		@XmlAnyAttribute
		private Map<QName, String> otherAttributes = new HashMap<>();

		/**
		 * Gets the value of the method property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list
		 * will be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the method property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getMethod().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link Review.Scope.Method }
		 * 
		 * 
		 */
		public List<Review.Scope.Method> getMethod() {
			if (method == null) {
				method = new ArrayList<>();
			}
			return this.method;
		}

		/**
		 * Gets the value of the name property.
		 * 
		 * @return possible object is {@link ReviewScope }
		 * 
		 */
		public ReviewScope getName() {
			return name;
		}

		/**
		 * Sets the value of the name property.
		 * 
		 * @param value
		 *            allowed object is {@link ReviewScope }
		 * 
		 */
		public void setName(ReviewScope value) {
			this.name = value;
		}

		/**
		 * Gets a map that contains attributes that aren't bound to any typed
		 * property on this class.
		 * 
		 * <p>
		 * the map is keyed by the name of the attribute and the value is the
		 * string value of the attribute.
		 * 
		 * the map returned by this method is live, and you can add new
		 * attribute by updating the map directly. Because of this design,
		 * there's no setter.
		 * 
		 * 
		 * @return always non-null
		 */
		public Map<QName, String> getOtherAttributes() {
			return otherAttributes;
		}

		/**
		 * <p>
		 * Java class for anonymous complex type.
		 * 
		 * <p>
		 * The following schema fragment specifies the expected content
		 * contained within this class.
		 * 
		 * <pre>
		 * &lt;complexType>
		 *   &lt;complexContent>
		 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
		 *       &lt;attribute name="name" use="required" type="{http://lca.jrc.it/ILCD/Common}MethodOfReviewValues" />
		 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
		 *     &lt;/restriction>
		 *   &lt;/complexContent>
		 * &lt;/complexType>
		 * </pre>
		 * 
		 * 
		 */
		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "")
		public static class Method implements Serializable {

			private final static long serialVersionUID = 1L;
			@XmlAttribute(name = "name", required = true)
			protected ReviewMethod name;
			@XmlAnyAttribute
			private Map<QName, String> otherAttributes = new HashMap<>();

			/**
			 * Gets the value of the name property.
			 * 
			 * @return possible object is {@link ReviewMethod }
			 * 
			 */
			public ReviewMethod getName() {
				return name;
			}

			/**
			 * Sets the value of the name property.
			 * 
			 * @param value
			 *            allowed object is {@link ReviewMethod }
			 * 
			 */
			public void setName(ReviewMethod value) {
				this.name = value;
			}

			/**
			 * Gets a map that contains attributes that aren't bound to any
			 * typed property on this class.
			 * 
			 * <p>
			 * the map is keyed by the name of the attribute and the value is
			 * the string value of the attribute.
			 * 
			 * the map returned by this method is live, and you can add new
			 * attribute by updating the map directly. Because of this design,
			 * there's no setter.
			 * 
			 * 
			 * @return always non-null
			 */
			public Map<QName, String> getOtherAttributes() {
				return otherAttributes;
			}

		}

	}

}
