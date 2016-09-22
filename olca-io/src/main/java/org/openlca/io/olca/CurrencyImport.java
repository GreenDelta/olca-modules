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
	private Sequence seq;
	private RefSwitcher refs;

	CurrencyImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.sourceDao = new CurrencyDao(source);
		this.destDao = new CurrencyDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import currencies");
		try {
			Currency refCurrency = sourceDao.getReferenceCurrency();
			if (refCurrency == null)
				return;
			if (!seq.contains(seq.CURRENCY, refCurrency.getRefId())) {
				copy(refCurrency);
			}
			for (Currency srcCurrency : sourceDao.getAll()) {
				if (Objects.equal(refCurrency, srcCurrency))
					continue;
				if (seq.contains(seq.CURRENCY, srcCurrency.getRefId()))
					continue;
				copy(srcCurrency);
			}
		} catch (Exception e) {
			log.error("Currency import failed", e);
		}
	}

	private void copy(Currency srcCurrency) {
		Currency destCurrency = srcCurrency.clone();
		if (Objects.equal(srcCurrency, srcCurrency.referenceCurrency)) {
			destCurrency.referenceCurrency = destCurrency;
		} else {
			destCurrency.referenceCurrency = refs.switchRef(
					srcCurrency.referenceCurrency);
		}
		destCurrency = destDao.insert(destCurrency);
		seq.put(seq.CURRENCY, srcCurrency.getRefId(), destCurrency.getId());
	}

}
