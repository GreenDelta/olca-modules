package org.openlca.ilcd.models;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.annotations.Label;

import jakarta.xml.bind.annotation.XmlElement;

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
