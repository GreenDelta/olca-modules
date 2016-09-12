package org.openlca.ilcd.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "methodPrinciple", "approach" })
@XmlRootElement(name = "lciMethodInformation")
public class LciMethodInformation implements Serializable {

	private final static long serialVersionUID = 1L;

	public LCIMethodPrincipleValues methodPrinciple;
	public final List<LCIMethodApproachesValues> approach = new ArrayList<>();

}
