package org.openlca.io.ilcd.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactMethod;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.LCIAMethodType;
import org.openlca.ilcd.methods.MethodInfo;
import org.openlca.ilcd.methods.Modelling;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MethodFetch {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final ImpactMethodDao dao;
	private final LCIAMethod ilcdMethod;

	private MethodFetch(ImportConfig config, LCIAMethod ilcdMethod) {
		this.dao = new ImpactMethodDao(config.db);
		this.ilcdMethod = ilcdMethod;
	}

	/**
	 * Gets the LCIA method(s) for the given ILCD data set from the openLCA
	 * database. If no method can be found a new one is created. Note that a
	 * LCIA-Method data set in ILCD is the same as an LCIA-Category data set in
	 * openLCA.
	 */
	public static List<ImpactMethod> getOrCreate(LCIAMethod ilcdMethod,
			ImportConfig config) {
		if (ilcdMethod == null || config.db == null)
			return Collections.emptyList();
		else
			return new MethodFetch(config, ilcdMethod).run();
	}

	private List<ImpactMethod> run() {
		try {
			ImpactMethod method = getFromExtension();
			if (method != null)
				return Arrays.asList(method);
			else
				return getFromNames();
		} catch (Exception e) {
			log.error("failed to search / create LCIA method in database", e);
			return Collections.emptyList();
		}
	}

	public ImpactMethod getFromExtension() {
		QName refIdAtt = new QName("http://openlca.org/ilcd-extensions",
				"olca_method_uuid");
		String refId = ilcdMethod.otherAttributes.get(refIdAtt);
		if (refId == null)
			return null;
		ImpactMethod method = dao.getForRefId(refId);
		return method != null ? method : createFromExtension(refId);
	}

	private ImpactMethod createFromExtension(String refId) {
		ImpactMethod method = new ImpactMethod();
		method.setRefId(refId);
		List<String> names = getMethodNames();
		String name = names.isEmpty() ? "LCIA Method" : names.get(0);
		method.setName(name);
		dao.insert(method);
		return method;
	}

	private List<ImpactMethod> getFromNames() {
		List<ImpactMethod> methods = new ArrayList<>();
		List<String> names = getMethodNames();
		String type = getType();
		for (String methodName : names) {
			String name = methodName;
			if (type != null)
				name += " (" + type + ")";
			ImpactMethod method = getFromName(name);
			if (method != null)
				methods.add(method);
		}
		return methods;
	}

	private ImpactMethod getFromName(String name) {
		String refId = KeyGen.get(name);
		org.openlca.core.model.ImpactMethod method = dao.getForRefId(refId);
		if (method == null) {
			method = new org.openlca.core.model.ImpactMethod();
			method.setRefId(refId);
			method.setName(name);
			dao.insert(method);
		}
		return method;
	}

	private List<String> getMethodNames() {
		MethodInfo info = ilcdMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return Collections.emptyList();
		return info.dataSetInfo.methods;
	}

	private String getType() {
		Modelling mav = ilcdMethod.modelling;
		if (mav == null || mav.normalisationAndWeighting == null)
			return null;
		LCIAMethodType type = mav.normalisationAndWeighting.type;
		return type == null ? null : type.value();
	}
}
