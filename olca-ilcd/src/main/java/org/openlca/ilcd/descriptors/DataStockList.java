package org.openlca.ilcd.descriptors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dataStockList", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
public class DataStockList {

	@XmlElement(name = "dataStock", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<DataStock> dataStocks = new ArrayList<>();

}
