package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.Currency;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

class CurrencyImport extends BaseImport<Currency> {

	private CurrencyImport(String refId, ImportConfig conf) {
		super(ModelType.CURRENCY, refId, conf);
	}

	static Currency run(String refId, ImportConfig conf) {
		return new CurrencyImport(refId, conf).run();
	}

	@Override
	Currency map(JsonObject json, long id) {
		if (json == null)
			return null;
		Currency c = new Currency();
		c.setId(id);
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
