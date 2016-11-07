
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.ShortText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModellingAndValidationType", propOrder = {
		"useAdviceForDataSet",
		"lciaMethodNormalisationAndWeighting",
		"dataSources",
		"completeness",
		"validation",
		"complianceDeclarations",
		"other"
})
public class Modelling implements Serializable {

	private final static long serialVersionUID = 1L;

	@ShortText
	protected List<LangString> useAdviceForDataSet;

	@XmlElement(name = "LCIAMethodNormalisationAndWeighting")
	protected NormalisationAndWeighting lciaMethodNormalisationAndWeighting;

	protected DataSourceList dataSources;

	protected Completeness completeness;

	protected Validation validation;

	protected ComplianceDeclarationList complianceDeclarations;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;

	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the useAdviceForDataSet property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the useAdviceForDataSet property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getUseAdviceForDataSet().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link ShortText
	 * }
	 * 
	 * 
	 */
	public List<LangString> getUseAdviceForDataSet() {
		if (useAdviceForDataSet == null) {
			useAdviceForDataSet = new ArrayList<>();
		}
		return this.useAdviceForDataSet;
	}

	/**
	 * Gets the value of the lciaMethodNormalisationAndWeighting property.
	 * 
	 * @return possible object is {@link NormalisationAndWeighting }
	 * 
	 */
	public NormalisationAndWeighting getLCIAMethodNormalisationAndWeighting() {
		return lciaMethodNormalisationAndWeighting;
	}

	/**
	 * Sets the value of the lciaMethodNormalisationAndWeighting property.
	 * 
	 * @param value
	 *            allowed object is {@link NormalisationAndWeighting }
	 * 
	 */
	public void setLCIAMethodNormalisationAndWeighting(NormalisationAndWeighting value) {
		this.lciaMethodNormalisationAndWeighting = value;
	}

	/**
	 * Gets the value of the dataSources property.
	 * 
	 * @return possible object is {@link DataSourceList }
	 * 
	 */
	public DataSourceList getDataSources() {
		return dataSources;
	}

	/**
	 * Sets the value of the dataSources property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSourceList }
	 * 
	 */
	public void setDataSources(DataSourceList value) {
		this.dataSources = value;
	}

	/**
	 * Gets the value of the completeness property.
	 * 
	 * @return possible object is {@link Completeness }
	 * 
	 */
	public Completeness getCompleteness() {
		return completeness;
	}

	/**
	 * Sets the value of the completeness property.
	 * 
	 * @param value
	 *            allowed object is {@link Completeness }
	 * 
	 */
	public void setCompleteness(Completeness value) {
		this.completeness = value;
	}

	/**
	 * Gets the value of the validation property.
	 * 
	 * @return possible object is {@link Validation }
	 * 
	 */
	public Validation getValidation() {
		return validation;
	}

	/**
	 * Sets the value of the validation property.
	 * 
	 * @param value
	 *            allowed object is {@link Validation }
	 * 
	 */
	public void setValidation(Validation value) {
		this.validation = value;
	}

	/**
	 * Gets the value of the complianceDeclarations property.
	 * 
	 * @return possible object is {@link ComplianceDeclarationList }
	 * 
	 */
	public ComplianceDeclarationList getComplianceDeclarations() {
		return complianceDeclarations;
	}

	/**
	 * Sets the value of the complianceDeclarations property.
	 * 
	 * @param value
	 *            allowed object is {@link ComplianceDeclarationList }
	 * 
	 */
	public void setComplianceDeclarations(ComplianceDeclarationList value) {
		this.complianceDeclarations = value;
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

}
