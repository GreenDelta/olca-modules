
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.Ref;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataGeneratorType", propOrder = {
		"dataGenerators"
})
public class DataGenerator implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToPersonOrEntityGeneratingTheDataSet", namespace = "http://lca.jrc.it/ILCD/Common", required = true)
	public final List<Ref> dataGenerators = new ArrayList<>();

}
