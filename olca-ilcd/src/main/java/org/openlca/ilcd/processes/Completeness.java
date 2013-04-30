package org.openlca.ilcd.processes;

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

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FlowCompleteness;
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Other;

/**
 * <p>
 * Java class for CompletenessType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CompletenessType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="completenessProductModel" type="{http://lca.jrc.it/ILCD/Common}CompletenessValues" minOccurs="0"/>
 *         &lt;element name="referenceToSupportedImpactAssessmentMethods" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="completenessElementaryFlows" type="{http://lca.jrc.it/ILCD/Process}CompletenessElementaryFlowsType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="completenessOtherProblemField" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Common}other" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletenessType", propOrder = { "completenessProductModel",
		"supportedLciaMethods", "completenessElementaryFlows",
		"completenessOtherProblemField", "other" })
public class Completeness implements Serializable {

	private final static long serialVersionUID = 1L;
	protected FlowCompleteness completenessProductModel;
	@XmlElement(name = "referenceToSupportedImpactAssessmentMethods")
	protected List<DataSetReference> supportedLciaMethods;
	protected List<ElementaryFlowCompleteness> completenessElementaryFlows;
	protected List<FreeText> completenessOtherProblemField;
	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	protected Other other;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

	/**
	 * Gets the value of the completenessProductModel property.
	 * 
	 * @return possible object is {@link FlowCompleteness }
	 * 
	 */
	public FlowCompleteness getCompletenessProductModel() {
		return completenessProductModel;
	}

	/**
	 * Sets the value of the completenessProductModel property.
	 * 
	 * @param value
	 *            allowed object is {@link FlowCompleteness }
	 * 
	 */
	public void setCompletenessProductModel(FlowCompleteness value) {
		this.completenessProductModel = value;
	}

	/**
	 * Gets the value of the supportedLciaMethods property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the supportedLciaMethods property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSupportedLciaMethods().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getSupportedLciaMethods() {
		if (supportedLciaMethods == null) {
			supportedLciaMethods = new ArrayList<>();
		}
		return this.supportedLciaMethods;
	}

	/**
	 * Gets the value of the completenessElementaryFlows property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the completenessElementaryFlows property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCompletenessElementaryFlows().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ElementaryFlowCompleteness }
	 * 
	 * 
	 */
	public List<ElementaryFlowCompleteness> getCompletenessElementaryFlows() {
		if (completenessElementaryFlows == null) {
			completenessElementaryFlows = new ArrayList<>();
		}
		return this.completenessElementaryFlows;
	}

	/**
	 * Gets the value of the completenessOtherProblemField property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the completenessOtherProblemField property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCompletenessOtherProblemField().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getCompletenessOtherProblemField() {
		if (completenessOtherProblemField == null) {
			completenessOtherProblemField = new ArrayList<>();
		}
		return this.completenessOtherProblemField;
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
