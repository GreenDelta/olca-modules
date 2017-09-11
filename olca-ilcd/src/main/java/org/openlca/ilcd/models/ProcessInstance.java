package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.Ref;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "process", "groupRefs" })
public class ProcessInstance {

	@XmlAttribute(name = "dataSetInternalID")
	public int id;

	@XmlElement(name = "referenceToProcess")
	public Ref process;

	@XmlElementWrapper(name = "groups")
	@XmlElement(name = "memberOf")
	public List<GroupRef> groupRefs = new ArrayList<>();

}
