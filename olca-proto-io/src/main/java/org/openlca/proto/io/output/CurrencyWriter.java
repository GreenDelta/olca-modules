package org.openlca.proto.io.output;

import org.openlca.core.model.Currency;
import org.openlca.proto.EntityType;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class CurrencyWriter {

  private final WriterConfig config;

  public CurrencyWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Currency write(Currency c) {
    var proto = Proto.Currency.newBuilder();
    if (c == null)
      return proto.build();
    proto.setEntityType(EntityType.Currency);
    Out.map(c, proto);
    Out.dep(config, c.category);

    proto.setCode(Strings.orEmpty(c.code));
    proto.setConversionFactor(c.conversionFactor);
    if (c.referenceCurrency != null) {
      proto.setReferenceCurrency(
        Refs.refOf(c.referenceCurrency));
      Out.dep(config, c.referenceCurrency);
    }

    return proto.build();
  }
}
