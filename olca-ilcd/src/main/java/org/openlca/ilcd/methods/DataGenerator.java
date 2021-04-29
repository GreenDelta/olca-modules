package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Ref;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataGeneratorType", propOrder = {
		"dataGenerators"
})
public class DataGenerator implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToPersonOrEntityGeneratingTheDataSet", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public final List<Ref> dataGenerators = new ArrayList<>();

}
