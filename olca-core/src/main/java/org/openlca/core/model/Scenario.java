package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

public class Scenario {

	public String name;
	public String description;
	public final List<ParameterRedef> parameterRedefs = new ArrayList<>();

}
