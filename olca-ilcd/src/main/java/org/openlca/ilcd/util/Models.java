package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.models.AdminInfo;
import org.openlca.ilcd.models.ClassificationList;
import org.openlca.ilcd.models.DataEntry;
import org.openlca.ilcd.models.DataSetInfo;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ModelInfo;
import org.openlca.ilcd.models.ModelName;
import org.openlca.ilcd.models.Publication;

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

	public static AdminInfo getAdminInfo(Model m) {
		if (m == null)
			return null;
		return m.adminInfo;
	}

	public static AdminInfo adminInfo(Model m) {
		if (m.adminInfo == null)
			m.adminInfo = new AdminInfo();
		return m.adminInfo;
	}

	public static DataEntry getDataEntry(Model m) {
		AdminInfo ai = getAdminInfo(m);
		if (ai == null)
			return null;
		return ai.dataEntry;
	}

	public static DataEntry dataEntry(Model m) {
		AdminInfo ai = adminInfo(m);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static Publication getPublication(Model m) {
		AdminInfo ai = getAdminInfo(m);
		if (ai == null)
			return null;
		return ai.publication;
	}

	public static Publication publication(Model m) {
		AdminInfo ai = adminInfo(m);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

}
