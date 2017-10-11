package org.openlca.io.olca;

import java.util.Set;
import java.util.TreeSet;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports the global parameters (identified by name).
 *
 * TODO: change this when we have reference IDs for parameters.
 */
class ParameterImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ParameterDao sourceDao;
	private ParameterDao destDao;

	ParameterImport(IDatabase source, IDatabase dest) {
		this.sourceDao = new ParameterDao(source);
		this.destDao = new ParameterDao(dest);
	}

	public void run() {
		log.trace("import global parameters");
		try {
			Set<String> existing = getExisting();
			for (Parameter param : sourceDao.getGlobalParameters()) {
				if (param.getName() == null)
					continue;
				if (existing.contains(param.getName()))
					continue;
				Parameter c = param.clone();
				destDao.insert(c);
			}
		} catch (Exception e) {
			log.error("Global parameter import failed", e);
		}
	}

	private Set<String> getExisting() {
		Set<String> existing = new TreeSet<>();
		for (Parameter param : destDao.getGlobalParameters()) {
			if (param.getName() != null)
				existing.add(param.getName());
		}
		return existing;
	}
}
