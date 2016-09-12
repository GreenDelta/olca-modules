package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ILCDType", namespace = "http://lca.jrc.it/ILCD/Wrapper", propOrder = {
		"dataSets" })
public class Wrapper implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElements({
			@XmlElement(name = "flowDataSet", namespace = "http://lca.jrc.it/ILCD/Flow", type = Flow.class),
			@XmlElement(name = "contactDataSet", namespace = "http://lca.jrc.it/ILCD/Contact", type = Contact.class),
			@XmlElement(name = "flowPropertyDataSet", namespace = "http://lca.jrc.it/ILCD/FlowProperty", type = FlowProperty.class),
			@XmlElement(name = "processDataSet", namespace = "http://lca.jrc.it/ILCD/Process", type = Process.class),
			@XmlElement(name = "sourceDataSet", namespace = "http://lca.jrc.it/ILCD/Source", type = Source.class),
			@XmlElement(name = "unitGroupDataSet", namespace = "http://lca.jrc.it/ILCD/UnitGroup", type = UnitGroup.class) })
	public final List<Serializable> dataSets = new ArrayList<>();

	@XmlAttribute(name = "version", required = true)
	public String version;

}
