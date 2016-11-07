
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;

/**
 * <p>
 * Java class for LCIAMethodInformationType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="LCIAMethodInformationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataSetInformation" type="{http://lca.jrc.it/ILCD/LCIAMethod}DataSetInformationType"/>
 *         &lt;element name="quantitativeReference" type="{http://lca.jrc.it/ILCD/LCIAMethod}QuantitativeReferenceType" minOccurs="0"/>
 *         &lt;element name="time" type="{http://lca.jrc.it/ILCD/LCIAMethod}TimeType" minOccurs="0"/>
 *         &lt;element name="geography" type="{http://lca.jrc.it/ILCD/LCIAMethod}GeographyType"/>
 *         &lt;element name="impactModel" type="{http://lca.jrc.it/ILCD/LCIAMethod}ImpactModelType" minOccurs="0"/>
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
@XmlType(name = "LCIAMethodInformationType", propOrder = {
		"dataSetInfo",
		"quantitativeReference",
		"time",
		"geography",
		"impactModel",
		"other"
})
public class MethodInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "dataSetInformation", required = true)
	public DataSetInfo dataSetInfo;

	public QuantitativeReference quantitativeReference;

	public Time time;

	@XmlElement(required = true)
	public Geography geography;

	public ImpactModel impactModel;

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
