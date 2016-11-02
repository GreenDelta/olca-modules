package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.DataQualityIndicator;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "dataQualityIndicator" })
@XmlRootElement(name = "dataQualityIndicators")
public class DataQualityIndicators implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(required = true)
	public final List<DataQualityIndicator> dataQualityIndicator = new ArrayList<>();

}
