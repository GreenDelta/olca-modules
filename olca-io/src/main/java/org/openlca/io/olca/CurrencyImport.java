package org.openlca.io.olca;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

class CurrencyImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private CurrencyDao sourceDao;
	private CurrencyDao destDao;
	private Seq seq;
	private RefSwitcher refs;

	CurrencyImport(Config config) {
		this.sourceDao = new CurrencyDao(config.source());
		this.destDao = new CurrencyDao(config.target());
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import currencies");
		try {
			Currency refCurrency = sourceDao.getReferenceCurrency();
			if (refCurrency == null)
				return;
			if (!seq.contains(seq.CURRENCY, refCurrency.refId)) {
				copy(refCurrency);
			}
			for (Currency srcCurrency : sourceDao.getAll()) {
				if (Objects.equal(refCurrency, srcCurrency))
					continue;
				if (seq.contains(seq.CURRENCY, srcCurrency.refId))
					continue;
				copy(srcCurrency);
			}
		} catch (Exception e) {
			log.error("Currency import failed", e);
		}
	}

	private void copy(Currency srcCurrency) {
		var copy = srcCurrency.copy();
		if (Objects.equal(srcCurrency, srcCurrency.referenceCurrency)) {
			copy.referenceCurrency = copy;
		} else {
			copy.referenceCurrency = refs.switchRef(
					srcCurrency.referenceCurrency);
		}
		copy = destDao.insert(copy);
		seq.put(seq.CURRENCY, srcCurrency.refId, copy.id);
	}

}
