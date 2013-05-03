package org.openlca.ecospold.internal.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.IProcessInformation;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.ITechnology;
import org.openlca.ecospold.ITimePeriod;
import org.w3c.dom.Element;

/**
 * Contains content-related metainformation for the unit process.
 * 
 * <p>
 * Java class for TProcessInformation complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TProcessInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="referenceFunction" type="{http://www.EcoInvent.org/EcoSpold01}TReferenceFunction"/>
 *         &lt;element name="geography" type="{http://www.EcoInvent.org/EcoSpold01}TGeography"/>
 *         &lt;element name="technology" type="{http://www.EcoInvent.org/EcoSpold01}TTechnology"/>
 *         &lt;element name="timePeriod" type="{http://www.EcoInvent.org/EcoSpold01}TTimePeriod"/>
 *         &lt;element name="dataSetInformation" type="{http://www.EcoInvent.org/EcoSpold01}TDataSetInformation"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TProcessInformation", propOrder = { "referenceFunction",
		"geography", "technology", "timePeriod", "dataSetInformation", "any" })
class ProcessInformation implements Serializable, IProcessInformation {

	private final static long serialVersionUID = 1L;
	@XmlElement(required = true, type = ReferenceFunction.class)
	protected IReferenceFunction referenceFunction;
	@XmlElement(required = true, type = Geography.class)
	protected IGeography geography;
	@XmlElement(required = true, type = Technology.class)
	protected ITechnology technology;
	@XmlElement(required = true, type = TimePeriod.class)
	protected ITimePeriod timePeriod;
	@XmlElement(required = true, type = DataSetInformation.class)
	protected IDataSetInformation dataSetInformation;
	@XmlAnyElement(lax = true)
	protected List<Object> any;

	/**
	 * Gets the value of the referenceFunction property.
	 * 
	 * @return possible object is {@link ReferenceFunction }
	 * 
	 */
	@Override
	public IReferenceFunction getReferenceFunction() {
		return referenceFunction;
	}

	/**
	 * Sets the value of the referenceFunction property.
	 * 
	 * @param value
	 *            allowed object is {@link ReferenceFunction }
	 * 
	 */
	@Override
	public void setReferenceFunction(IReferenceFunction value) {
		this.referenceFunction = value;
	}

	/**
	 * Gets the value of the geography property.
	 * 
	 * @return possible object is {@link Geography }
	 * 
	 */
	@Override
	public IGeography getGeography() {
		return geography;
	}

	/**
	 * Sets the value of the geography property.
	 * 
	 * @param value
	 *            allowed object is {@link Geography }
	 * 
	 */
	@Override
	public void setGeography(IGeography value) {
		this.geography = value;
	}

	/**
	 * Gets the value of the technology property.
	 * 
	 * @return possible object is {@link Technology }
	 * 
	 */
	@Override
	public ITechnology getTechnology() {
		return technology;
	}

	/**
	 * Sets the value of the technology property.
	 * 
	 * @param value
	 *            allowed object is {@link Technology }
	 * 
	 */
	@Override
	public void setTechnology(ITechnology value) {
		this.technology = value;
	}

	/**
	 * Gets the value of the timePeriod property.
	 * 
	 * @return possible object is {@link TimePeriod }
	 * 
	 */
	@Override
	public ITimePeriod getTimePeriod() {
		return timePeriod;
	}

	/**
	 * Sets the value of the timePeriod property.
	 * 
	 * @param value
	 *            allowed object is {@link TimePeriod }
	 * 
	 */
	@Override
	public void setTimePeriod(ITimePeriod value) {
		this.timePeriod = value;
	}

	/**
	 * Gets the value of the dataSetInformation property.
	 * 
	 * @return possible object is {@link DataSetInformation }
	 * 
	 */
	@Override
	public IDataSetInformation getDataSetInformation() {
		return dataSetInformation;
	}

	/**
	 * Sets the value of the dataSetInformation property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetInformation }
	 * 
	 */
	@Override
	public void setDataSetInformation(IDataSetInformation value) {
		this.dataSetInformation = value;
	}

	/**
	 * Gets the value of the any property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the any property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAny().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Object }
	 * {@link Element }
	 * 
	 * 
	 */
	@Override
	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<>();
		}
		return this.any;
	}

}
