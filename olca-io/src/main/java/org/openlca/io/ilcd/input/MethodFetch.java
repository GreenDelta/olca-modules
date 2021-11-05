package org.openlca.io.ilcd.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactMethod;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.MethodInfo;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get possible LCIA methods for the given ILCD data set. In an ILCD LCIA
 * data set the LCIA methods of an indicator are stored as plain strings in
 * the data set. This function tries to find corresponding LCIA methods in
 * the openLCA database or creates them when no method can be found for a
 * name.
 */
final class MethodFetch {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ImpactMethodDao dao;
	private final LCIAMethod ilcdMethod;

	private MethodFetch(ImportConfig config, LCIAMethod ilcdMethod) {
		this.dao = new ImpactMethodDao(config.db);
		this.ilcdMethod = ilcdMethod;
	}

	public static List<ImpactMethod> get(LCIAMethod ilcdMethod, ImportConfig config) {
		if (ilcdMethod == null || config.db == null)
			return Collections.emptyList();
		else
			return new MethodFetch(config, ilcdMethod).run();
	}

	private List<ImpactMethod> run() {
		try {
			List<String> names = getMethodNames();
			if (names.isEmpty())
				return Collections.emptyList();
			List<ImpactMethod> methods = new ArrayList<>();
			for (String name : names) {
				ImpactMethod method = getFromName(name);
				methods.add(method);
			}
			return methods;
		} catch (Exception e) {
			log.error("failed to search / create LCIA method in database", e);
			return Collections.emptyList();
		}
	}

	private List<String> getMethodNames() {
		MethodInfo info = ilcdMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return Collections.emptyList();
		return info.dataSetInfo.methods;
	}

	private ImpactMethod getFromName(String name) {
		String refId = KeyGen.get(name);
		ImpactMethod method = dao.getForRefId(refId);
		if (method == null) {
			method = new org.openlca.core.model.ImpactMethod();
			method.refId = refId;
			method.name = name;
			method.lastChange = new Date().getTime();
			method = dao.insert(method);
		}
		return method;
	}
}
