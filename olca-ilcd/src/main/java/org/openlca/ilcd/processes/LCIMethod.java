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
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.LCIMethodApproach;
import org.openlca.ilcd.commons.LCIMethodPrinciple;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ProcessType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIMethodAndAllocationType", propOrder = { "processType",
		"lciMethodPrinciple", "deviationsFromLCIMethodPrinciple",
		"lciMethodApproaches", "deviationsFromLCIMethodApproaches",
		"modellingConstants", "deviationsFromModellingConstants",
		"referenceToLCAMethodDetails", "other" })
public class LCIMethod implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "typeOfDataSet")
	public ProcessType processType;

	@XmlElement(name = "LCIMethodPrinciple")
	public LCIMethodPrinciple lciMethodPrinciple;

	public final List<FreeText> deviationsFromLCIMethodPrinciple = new ArrayList<>();

	@XmlElement(name = "LCIMethodApproaches")
	public final List<LCIMethodApproach> lciMethodApproaches = new ArrayList<>();

	public final List<FreeText> deviationsFromLCIMethodApproaches = new ArrayList<>();

	public final List<FreeText> modellingConstants = new ArrayList<>();

	public final List<FreeText> deviationsFromModellingConstants = new ArrayList<>();

	public final List<DataSetReference> referenceToLCAMethodDetails = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
