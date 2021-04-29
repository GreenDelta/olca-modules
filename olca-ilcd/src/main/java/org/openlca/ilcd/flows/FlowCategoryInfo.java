package org.openlca.ilcd.flows;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Classification;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlowCategoryInformationType", propOrder = {
		"compartmentLists", "classifications" })
public class FlowCategoryInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "elementaryFlowCategorization", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<CompartmentList> compartmentLists = new ArrayList<>();

	@XmlElement(name = "classification", namespace = "http://lca.jrc.it/ILCD/Common")
	public final List<Classification> classifications = new ArrayList<>();

}
