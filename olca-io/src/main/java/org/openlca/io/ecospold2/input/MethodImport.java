package org.openlca.io.ecospold2.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spold2.ImpactIndicator;

import java.io.File;
import java.util.Date;

public class MethodImport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File[] files;
	private final IDatabase db;

	public MethodImport(File[] files, IDatabase db) {
		this.files = files;
		this.db = db;
	}

	@Override
	public void run() {
		ImpactMethodDao dao = new ImpactMethodDao(db);
		Spold2Files.parse(files, es2 -> {
			if (es2.impactMethod == null)
				return;
			ImpactMethod m = dao.getForRefId(es2.impactMethod.id);
			if (m != null) {
				log.warn("an LCIA method with id={} " +
						"already exisis; not imported", m.refId);
				return;
			}
			m = map(es2.impactMethod);
			if (m != null) {
				dao.insert(m);
				log.info("saved new LCIA method {}", m);
			}
		});
	}

	private ImpactMethod map(spold2.ImpactMethod eMethod) {
		if (eMethod.id == null) {
			log.info("method {} has no id", eMethod.name);
			return null;
		}
		ImpactMethod method = new ImpactMethod();
		method.refId = eMethod.id;
		method.name = eMethod.name;
		method.lastChange = new Date().getTime();
		eMethod.categories.forEach(eCategory -> {
			eCategory.indicators.forEach(eIndicator -> {
				ImpactCategory impact = map(eCategory, eIndicator);
				if (impact != null) {
					method.impactCategories.add(impact);
				}
			});
		});
		return method;
	}

	private ImpactCategory map(spold2.ImpactCategory eCategory,
	                           ImpactIndicator eIndicator) {
		if (eCategory == null || eIndicator == null)
			return null;
		ImpactCategory impact = new ImpactCategory();
		impact.refId = KeyGen.get(eCategory.id, eIndicator.id);
		impact.name = eIndicator.name + " - " + eCategory.name;
		impact.lastChange = new Date().getTime();
		impact.referenceUnit = eIndicator.unit;

		return impact;
	}

}
