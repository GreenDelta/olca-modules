package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

/**
 * <p>
 * Java class for ILCDType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ILCDType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Process}processDataSet"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Flow}flowDataSet"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/FlowProperty}flowPropertyDataSet"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/UnitGroup}unitGroupDataSet"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Source}sourceDataSet"/>
 *         &lt;element ref="{http://lca.jrc.it/ILCD/Contact}contactDataSet"/>
 *       &lt;/choice>
 *       &lt;attribute name="version" use="required" type="{http://lca.jrc.it/ILCD/Common}SchemaVersion" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ILCDType", namespace = "http://lca.jrc.it/ILCD/Wrapper", propOrder = { "processDataSetOrFlowDataSetOrFlowPropertyDataSet" })
public class Wrapper implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElements({
			@XmlElement(name = "flowDataSet", namespace = "http://lca.jrc.it/ILCD/Flow", type = Flow.class),
			@XmlElement(name = "contactDataSet", namespace = "http://lca.jrc.it/ILCD/Contact", type = Contact.class),
			@XmlElement(name = "flowPropertyDataSet", namespace = "http://lca.jrc.it/ILCD/FlowProperty", type = FlowProperty.class),
			@XmlElement(name = "processDataSet", namespace = "http://lca.jrc.it/ILCD/Process", type = Process.class),
			@XmlElement(name = "sourceDataSet", namespace = "http://lca.jrc.it/ILCD/Source", type = Source.class),
			@XmlElement(name = "unitGroupDataSet", namespace = "http://lca.jrc.it/ILCD/UnitGroup", type = UnitGroup.class) })
	protected List<Serializable> processDataSetOrFlowDataSetOrFlowPropertyDataSet;
	@XmlAttribute(name = "version", required = true)
	protected String version;

	/**
	 * Gets the value of the processDataSetOrFlowDataSetOrFlowPropertyDataSet
	 * property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the
	 * processDataSetOrFlowDataSetOrFlowPropertyDataSet property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getProcessDataSetOrFlowDataSetOrFlowPropertyDataSet().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Flow }
	 * {@link Contact } {@link FlowProperty } {@link Process } {@link Source }
	 * {@link UnitGroup }
	 * 
	 * 
	 */
	public List<Serializable> getProcessDataSetOrFlowDataSetOrFlowPropertyDataSet() {
		if (processDataSetOrFlowDataSetOrFlowPropertyDataSet == null) {
			processDataSetOrFlowDataSetOrFlowPropertyDataSet = new ArrayList<>();
		}
		return this.processDataSetOrFlowDataSetOrFlowPropertyDataSet;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}

}
