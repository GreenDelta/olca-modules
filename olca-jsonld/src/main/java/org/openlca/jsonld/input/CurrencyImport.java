package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.Currency;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

class CurrencyImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private String refId;
	private ImportConfig conf;

	private CurrencyImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}

	static Currency run(String refId, ImportConfig conf) {
		return new CurrencyImport(refId, conf).run();
	}

	private Currency run() {
		if (refId == null || conf == null)
			return null;
		try {
			Currency c = conf.db.getCurrency(refId);
			if (c != null)
				return c;
			JsonObject json = conf.store.get(ModelType.CURRENCY, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import currency " + refId, e);
			return null;
		}
	}

	private Currency map(JsonObject json) {
		if (json == null)
			return null;
		Currency c = new Currency();
		In.mapAtts(json, c);
		String catId = In.getRefId(json, "category");
		c.setCategory(CategoryImport.run(catId, conf));
		c.code = In.getString(json, "code");
		c.conversionFactor = In.getDouble(json, "conversionFactor", 1.0);
		String refCurrencyId = In.getRefId(json, "referenceCurrency");
		if (Objects.equals(refCurrencyId, refId))
			c.referenceCurrency = c;
		else
			c.referenceCurrency = CurrencyImport.run(refCurrencyId, conf);
		return conf.db.put(c);
	}

}
