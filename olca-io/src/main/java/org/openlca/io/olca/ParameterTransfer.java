package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

final class ParameterTransfer implements EntityTransfer<Parameter> {

	private final TransferConfig conf;

	ParameterTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var origin : new ParameterDao(conf.source()).getGlobalParameters()) {
			sync(origin);
		}
	}

	@Override
	public Parameter sync(Parameter origin) {
		if (origin == null)
			return null;
		var mapped = conf.getMapped(origin);
		if (mapped != null)
			return mapped;
		if (origin.scope != ParameterScope.GLOBAL)
			return conf.sync(origin, origin::copy);

		for (var existing : new ParameterDao(conf.target()).getGlobalParameters()) {
			if (!Objects.equals(existing.name, origin.name))
				continue;
			conf.seq().put(ModelType.PARAMETER, origin.id, existing.id);
			conf.log().skipped(origin);
			return existing;
		}

		return conf.sync(origin, origin::copy);
	}
}