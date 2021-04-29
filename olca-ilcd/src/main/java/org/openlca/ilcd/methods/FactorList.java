package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CharacterisationFactorsType", propOrder = { "factors" })
public class FactorList implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "factor", required = true)
	public final List<Factor> factors = new ArrayList<>();

}
