package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.Currency;
import org.openlca.core.model.Version;
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

    // root entity fields
    proto.setType("Currency");
    proto.setId(Strings.orEmpty(c.refId));
    proto.setName(Strings.orEmpty(c.name));
    proto.setDescription(Strings.orEmpty(c.description));
    proto.setVersion(Version.asString(c.version));
    if (c.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(c.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(c.tags)) {
      Arrays.stream(c.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (c.category != null) {
      proto.setCategory(Out.refOf(c.category, config));
    }

    // model specific fields
    proto.setCode(Strings.orEmpty(c.code));
    proto.setConversionFactor(c.conversionFactor);
    if (c.referenceCurrency != null) {
      proto.setReferenceCurrency(
        Out.refOf(c.referenceCurrency, config));
    }

    return proto.build();
  }
}
