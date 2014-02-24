package org.openlca.io.csv.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

import com.google.common.reflect.Parameter;

class CSVImportCache {
	/**
	 * key:
	 */
	Map<String, Source> sourceMap = new HashMap<>();

	/**
	 * key: flow name + value of {@link ElementaryFlowType}
	 */
	Map<String, SPSubstance> substanceMap = new HashMap<>();

	/**
	 * key: unit + quantity + conversion factor
	 */
	Map<String, Unit> unitMap = new HashMap<>();

	/**
	 * key: quantity name
	 */
	Map<String, UnitGroup> unitGroupMap = new HashMap<>();

	Map<String, Parameter> parameterMap = new HashMap<>();
}
