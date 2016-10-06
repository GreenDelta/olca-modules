package org.openlca.ilcd.commons;

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

import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommissionerAndGoalType", propOrder = { "commissioners",
		"project", "intendedApplications", "other" })
public class CommissionerAndGoal implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToCommissioner")
	public final List<DataSetReference> commissioners = new ArrayList<>();

	@Label
	public final List<LangString> project = new ArrayList<>();

	@FreeText
	public final List<LangString> intendedApplications = new ArrayList<>();

	public Other other;

	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<>();

}
