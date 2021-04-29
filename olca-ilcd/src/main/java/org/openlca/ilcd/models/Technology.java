package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "groups", "processes" })
public class Technology {

	@XmlElementWrapper(name = "groupDeclarations")
	@XmlElement(name = "group")
	public List<Group> groups = new ArrayList<>();

	@XmlElementWrapper(name = "processes")
	@XmlElement(name = "processInstance")
	public List<ProcessInstance> processes = new ArrayList<>();

}
