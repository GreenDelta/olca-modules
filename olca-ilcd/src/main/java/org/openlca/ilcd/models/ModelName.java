package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.annotations.Label;

public class ModelName {

	@Label
	@XmlElement(name = "baseName", required = true)
	public final List<LangString> name = new ArrayList<>();

	@Label
	@XmlElement(name = "treatmentStandardsRoutes")
	public final List<LangString> technicalDetails = new ArrayList<>();

	@Label
	@XmlElement(name = "mixAndLocationTypes")
	public final List<LangString> mixAndLocation = new ArrayList<>();

	@Label
	@XmlElement(name = "functionalUnitFlowProperties")
	public final List<LangString> flowProperties = new ArrayList<>();

}
