package org.openlca.proto.io.input;

import java.util.Objects;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.model.Currency;
import org.openlca.proto.ProtoCurrency;

class CurrencyImport implements Import<Currency> {

  private final ProtoImport imp;

  CurrencyImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<Currency> of(String id) {
    var currency = imp.get(Currency.class, id);

    // check if we are in update mode
    var update = false;
    if (currency != null) {
      update = imp.shouldUpdate(currency);
      if(!update) {
        return ImportStatus.skipped(currency);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getCurrency(id);
    if (proto == null)
      return currency != null
        ? ImportStatus.skipped(currency)
        : ImportStatus.error("Could not resolve Currency " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(currency, wrap))
        return ImportStatus.skipped(currency);
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
    return update
      ? ImportStatus.updated(currency)
      : ImportStatus.created(currency);
  }

  private void map(ProtoCurrency proto, Currency currency) {
    currency.code = proto.getCode();
    currency.conversionFactor = proto.getConversionFactor();
    var refCurrencyID = proto.getReferenceCurrency().getId();
    if (Objects.equals(refCurrencyID, proto.getId())) {
      currency.referenceCurrency = currency;
    } else {
      currency.referenceCurrency = of(refCurrencyID).model();
    }
  }
}

