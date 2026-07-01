package org.openlca.io.ecospold1.output;

import java.util.Collections;
import java.util.Map;

import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.ecospold.EcoSpold;
import org.openlca.ecospold.model.IEcoSpold;
import org.openlca.ecospold.model.DataSet;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;

class MethodConverter {

	private final ImpactMethod method;
	private final EcoSpold1Config config;
	private final Map<String, FlowMapEntry> flowMap;

	static IEcoSpold convert(ImpactMethod method, EcoSpold1Config config) {
		return new MethodConverter(method, config).doIt();
	}

	private MethodConverter(ImpactMethod method, EcoSpold1Config config) {
		this.method = method;
		this.config = config;
		this.flowMap = config.flowMap != null
			? config.flowMap.index()
			: Collections.emptyMap();
	}

	private IEcoSpold doIt() {
		var spold = EcoSpold.newImpactMethod();
		for (var indicator : method.impactCategories) {
			var ds = spold.newDataSet();
			Util.setDataSetAttributes(ds, method);
			mapLCIACategory(indicator, ds);
			var refFun = ds.withReferenceFunction();
			refFun.setCategory(method.name);
			refFun.setGeneralComment(Util.comment(indicator, config));
			if (config.withDefaults) {
				SchemaDefaults.write(ds);
			}
		}
		return spold;
	}

	private void mapLCIACategory(ImpactCategory category, DataSet ds) {
		var refFun = ds.withReferenceFunction();
		String subCategory = category.name;
		String name = null;
		if (subCategory.contains("-")) {
			String[] parts = subCategory.split("-", 2);
			subCategory = parts[0].trim();
			name = parts[1].trim();
		}
		refFun.setSubCategory(subCategory);
		refFun.setName(name);
		refFun.setUnit(category.referenceUnit);
		for (var f : category.impactFactors) {
			mapLCIAFactor(f, ds);
		}
	}

	private void mapLCIAFactor(ImpactFactor factor, DataSet ds) {
		var e = ExportFlow.of(factor, ds, flowMap).orElse(null);
		if (e == null)
			return;


		Util.mapFlowInformation(e, factor.flow);

		// unit
		if (mapping != null && mapping.targetFlow() != null
			&& mapping.targetFlow().unit != null
			&& mapping.targetFlow().unit.name != null) {
			e.setUnit(mapping.targetFlow().unit.name);
		} else {
			e.setUnit(factor.unit.name);
		}

		e.setMeanValue(factor.value * f);
	}




}
