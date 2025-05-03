package org.openlca.io.olca;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.model.Currency;
import org.openlca.core.model.ModelType;

import com.google.common.base.Objects;

class CurrencyImport {

	private final Config conf;

	private CurrencyImport(Config conf) {
		this.conf = conf;
	}

	static void run(Config conf) {
		new CurrencyImport(conf).run();
	}

	private void run() {
		try {
			var sourceDao = new CurrencyDao(conf.source());
			var ref = sourceDao.getReferenceCurrency();
			if (ref == null)
				return;

			// first copy the reference currency
			var targetRef = conf.isMapped(ModelType.CURRENCY, ref.id)
					? conf.target().get(Currency.class, ref.refId)
					: copy(ref, null);
			if (targetRef == null) {
				conf.log().error(
						"failed to copy reference currency; " + ref.refId);
				return;
			}

			// now copy all other currencies and link them to the
			// reference currency
			for (var src : sourceDao.getAll()) {
				if (Objects.equal(ref, src))
					continue;
				if (conf.isMapped(ModelType.CURRENCY, src.id))
					continue;
				copy(src, targetRef);
			}
		} catch (Exception e) {
			conf.log().error("Currency import failed", e);
		}
	}

	private Currency copy(Currency c, Currency ref) {
		return conf.copy(c, currency -> {
			var copy = currency.copy();
			copy.referenceCurrency = ref == null
					? copy
					: ref;
			return copy;
		});
	}
}
