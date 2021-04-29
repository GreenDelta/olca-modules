package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.ModellingPrinciple;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "methodPrinciple", "approach" })
@XmlRootElement(name = "lciMethodInformation")
public class LciMethodInformation implements Serializable {

	private final static long serialVersionUID = 1L;

	public ModellingPrinciple methodPrinciple;
	public final List<ModellingApproach> approach = new ArrayList<>();

}
