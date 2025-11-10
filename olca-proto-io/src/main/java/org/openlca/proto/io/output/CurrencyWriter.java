package org.openlca.proto.io.output;

import org.openlca.core.model.Currency;
import org.openlca.proto.ProtoCurrency;
import org.openlca.proto.ProtoType;
import org.openlca.commons.Strings;

public class CurrencyWriter {

  private final WriterConfig config;

  public CurrencyWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoCurrency write(Currency c) {
    var proto = ProtoCurrency.newBuilder();
    if (c == null)
      return proto.build();
    proto.setType(ProtoType.Currency);
    Out.map(c, proto);
    proto.setCode(Strings.notNull(c.code));
    proto.setConversionFactor(c.conversionFactor);
		config.dep(c.referenceCurrency, proto::setRefCurrency);
    return proto.build();
  }
}
