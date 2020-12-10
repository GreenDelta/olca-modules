package org.openlca.proto.input;

import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Source;
import org.openlca.proto.Proto;

public class SourceImport {

  private final ProtoImport imp;

  public SourceImport(ProtoImport imp) {
    this.imp = imp;
  }

  public Source of(String id) {
    if (id == null)
      return null;
    var source = imp.get(Source.class, id);

    // check if we are in update mode
    var update = false;
    if (source != null) {
      update = imp.shouldUpdate(source);
      if(!update) {
        return source;
      }
    }

    // check the proto object
    var proto = imp.store.getSource(id);
    if (proto == null)
      return source;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(source, wrap))
        return source;
    }

    // map the data
    if (source == null) {
      source = new Source();
    }
    wrap.mapTo(source, imp);
    map(proto, source);

    // insert it
    var dao = new SourceDao(imp.db);
    source = update
      ? dao.update(source)
      : dao.insert(source);
    imp.putHandled(source);
    return source;
  }

  private void map(Proto.Source proto, Source source) {
    source.url = proto.getUrl();
    source.externalFile = proto.getExternalFile();
    source.textReference = proto.getTextReference();
    var year = proto.getYear();
    if (year != 0) {
      source.year = (short) year;
    }
  }
}

