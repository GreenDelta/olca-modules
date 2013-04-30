package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}methodPrinciple" minOccurs="0"/>
 *         &lt;element ref="{http://www.ilcd-network.org/ILCD/ServiceAPI/Process}approach" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "methodPrinciple", "approach" })
@XmlRootElement(name = "lciMethodInformation")
public class LciMethodInformation implements Serializable {

	private final static long serialVersionUID = 1L;
	protected LCIMethodPrincipleValues methodPrinciple;
	protected List<LCIMethodApproachesValues> approach;

	/**
	 * Gets the value of the methodPrinciple property.
	 * 
	 * @return possible object is {@link LCIMethodPrincipleValues }
	 * 
	 */
	public LCIMethodPrincipleValues getMethodPrinciple() {
		return methodPrinciple;
	}

	/**
	 * Sets the value of the methodPrinciple property.
	 * 
	 * @param value
	 *            allowed object is {@link LCIMethodPrincipleValues }
	 * 
	 */
	public void setMethodPrinciple(LCIMethodPrincipleValues value) {
		this.methodPrinciple = value;
	}

	/**
	 * Gets the value of the approach property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the approach property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getApproach().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link LCIMethodApproachesValues }
	 * 
	 * 
	 */
	public List<LCIMethodApproachesValues> getApproach() {
		if (approach == null) {
			approach = new ArrayList<>();
		}
		return this.approach;
	}

}
