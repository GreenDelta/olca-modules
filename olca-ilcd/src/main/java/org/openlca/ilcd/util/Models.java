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
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.models.Publication;
import org.openlca.ilcd.models.QuantitativeReference;
import org.openlca.ilcd.models.Technology;

public class Models {

	public static String getOrigin(Model model) {
		return model != null
			? Extensions.getString(model.otherAttributes, "origin")
			: null;
	}

	public static void setOrigin(Model model, String value) {
		if (model == null)
			return;
		Extensions.setString(model.otherAttributes, "origin", value);
	}

	public static ModelInfo forceModelInfo(Model model) {
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

	public static DataSetInfo forceDataSetInfo(Model model) {
		ModelInfo mi = forceModelInfo(model);
		if (mi.dataSetInfo == null) {
			mi.dataSetInfo = new DataSetInfo();
		}
		return mi.dataSetInfo;
	}

	public static DataSetInfo getDataSetInfo(Model model) {
		var mi = getModelInfo(model);
		return mi != null
			? mi.dataSetInfo
			: null;
	}

	public static ModelName forceModelName(Model model) {
		DataSetInfo di = forceDataSetInfo(model);
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

	public static List<Classification> forceClassifications(Model model) {
		DataSetInfo di = forceDataSetInfo(model);
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

	public static QuantitativeReference getQuantitativeReference(Model m) {
		ModelInfo mi = getModelInfo(m);
		if (mi == null)
			return null;
		return mi.quantitativeReference;
	}

	public static QuantitativeReference forceQuantitativeReference(Model m) {
		ModelInfo mi = forceModelInfo(m);
		if (mi.quantitativeReference == null)
			mi.quantitativeReference = new QuantitativeReference();
		return mi.quantitativeReference;
	}

	public static Technology getTechnology(Model m) {
		ModelInfo mi = getModelInfo(m);
		if (mi == null)
			return null;
		return mi.technology;
	}

	public static Technology forceTechnology(Model m) {
		ModelInfo mi = forceModelInfo(m);
		if (mi.technology == null)
			mi.technology = new Technology();
		return mi.technology;
	}

	public static AdminInfo getAdminInfo(Model m) {
		if (m == null)
			return null;
		return m.adminInfo;
	}

	public static AdminInfo forceAdminInfo(Model m) {
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

	public static DataEntry forceDataEntry(Model m) {
		AdminInfo ai = forceAdminInfo(m);
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

	public static Publication forcePublication(Model m) {
		AdminInfo ai = forceAdminInfo(m);
		if (ai.publication == null)
			ai.publication = new Publication();
		return ai.publication;
	}

	public static List<ProcessInstance> getProcesses(Model m) {
		var tech = getTechnology(m);
		return tech == null
			? Collections.emptyList()
			: tech.processes;
	}

	public static List<ProcessInstance> forceProcesses(Model m) {
		var tech = forceTechnology(m);
		return tech.processes;
	}
}
