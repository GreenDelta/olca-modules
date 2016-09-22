package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AllocationsType", propOrder = { "factors" })
public class Allocation implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "allocation", required = true)
	public final List<AllocationFactor> factors = new ArrayList<>();

}
