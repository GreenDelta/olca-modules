package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.DataQualityIndicator;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "dataQualityIndicator" })
@XmlRootElement(name = "dataQualityIndicators")
public class DataQualityIndicators implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true)
	public final List<DataQualityIndicator> dataQualityIndicator = new ArrayList<>();

}
