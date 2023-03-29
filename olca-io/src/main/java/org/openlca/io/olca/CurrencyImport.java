package org.openlca.io.olca;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.model.Currency;

import com.google.common.base.Objects;

class CurrencyImport {

	private final Config conf;

	CurrencyImport(Config conf) {
		this.conf = conf;
	}

	public void run() {
		try {
			var sourceDao = new CurrencyDao(conf.source());
			var ref = sourceDao.getReferenceCurrency();
			if (ref == null)
				return;
			var targetRef = !conf.contains(Seq.CURRENCY, ref.refId)
					? copy(ref, null)
					: conf.target().get(Currency.class, ref.refId);
			for (var c : sourceDao.getAll()) {
				if (Objects.equal(ref, c))
					continue;
				if (conf.contains(Seq.CURRENCY, c.refId))
					continue;
				copy(c, targetRef);
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
