package org.openlca.proto.input;

import java.util.Objects;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.model.Currency;
import org.openlca.proto.Proto;

public class CurrencyImport {

  private final ProtoImport imp;

  public CurrencyImport(ProtoImport imp) {
    this.imp = imp;
  }

  public Currency of(String id) {
    if (id == null)
      return null;
    var currency = imp.get(Currency.class, id);

    // check if we are in update mode
    var update = false;
    if (currency != null) {
      update = imp.shouldUpdate(currency);
      if(!update) {
        return currency;
      }
    }

    // check the proto object
    var proto = imp.store.getCurrency(id);
    if (proto == null)
      return currency;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(currency, wrap))
        return currency;
    }

    // map the data
    if (currency == null) {
      currency = new Currency();
    }
    wrap.mapTo(currency, imp);
    map(proto, currency);

    // insert it
    var dao = new CurrencyDao(imp.db);
    currency = update
      ? dao.update(currency)
      : dao.insert(currency);
    imp.putHandled(currency);
    return currency;
  }

  private void map(Proto.Currency proto, Currency currency) {
    currency.code = proto.getCode();
    currency.conversionFactor = proto.getConversionFactor();
    var refCurrencyID = proto.getReferenceCurrency().getId();
    if (Objects.equals(refCurrencyID, proto.getId())) {
      currency.referenceCurrency = currency;
    } else {
      currency.referenceCurrency = of(refCurrencyID);
    }
  }
}

