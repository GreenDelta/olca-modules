package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.models.ClassificationList;
import org.openlca.ilcd.models.DataSetInfo;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ModelInfo;
import org.openlca.ilcd.models.ModelName;

public class Models {

	public static ModelInfo modelInfo(Model model) {
		if (model.info == null) {
			model.info = new ModelInfo();
		}
		return model.info;
	}

	public static ModelInfo getModelInfo(Model model) {
		if (model == null)
			return null;
		return model.info;
	}

	public static DataSetInfo dataSetInfo(Model model) {
		ModelInfo mi = modelInfo(model);
		if (mi.dataSetInfo == null) {
			mi.dataSetInfo = new DataSetInfo();
		}
		return mi.dataSetInfo;
	}

	public static DataSetInfo getDataSetInfo(Model model) {
		ModelInfo mi = getModelInfo(model);
		if (mi == null)
			return null;
		return mi.dataSetInfo;
	}

	public static ModelName modelName(Model model) {
		DataSetInfo di = dataSetInfo(model);
		if (di.name == null) {
			di.name = new ModelName();
		}
		return di.name;
	}

	public static ModelName getModelName(Model model) {
		DataSetInfo di = getDataSetInfo(model);
		if (di == null)
			return null;
		return di.name;
	}

	public static List<Classification> classifications(Model model) {
		DataSetInfo di = dataSetInfo(model);
		if (di.classifications == null) {
			di.classifications = new ClassificationList();
		}
		return di.classifications.classifications;
	}

	public static List<Classification> getClassifications(Model model) {
		DataSetInfo di = getDataSetInfo(model);
		if (di == null || di.classifications == null)
			return Collections.emptyList();
		return di.classifications.classifications;
	}

}
