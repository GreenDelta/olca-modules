package org.openlca.proto.io.output;

import jakarta.persistence.metamodel.EntityType;
import org.openlca.core.model.Currency;
import org.openlca.proto.Proto;
import org.openlca.proto.ProtoCurrency;
import org.openlca.proto.ProtoType;
import org.openlca.util.Strings;

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
