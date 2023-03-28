package org.openlca.io.olca;

import java.util.stream.Collectors;

import org.openlca.core.database.ParameterDao;

/**
 * Imports the global parameters (identified by name).
 */
class ParameterImport {

	private final Config conf;

	ParameterImport(Config conf) {
		this.conf = conf;
	}

	public void run() {
		var existing = new ParameterDao(conf.target())
				.getGlobalParameters()
				.stream()
				.map(p -> p.name)
				.collect(Collectors.toSet());

		new ParameterDao(conf.source())
				.getGlobalParameters()
				.stream()
				.filter(p -> !existing.contains(p.name))
				.forEach(p -> {
					var copy = p.copy();
					if (!conf.seq().contains(Seq.CATEGORY, p.refId)) {
						copy.refId = p.refId;
					}
					copy.category = conf.swap(p.category);
					conf.target().insert(copy);
				});
	}
}
