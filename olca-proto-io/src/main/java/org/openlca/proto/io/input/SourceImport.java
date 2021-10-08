package org.openlca.proto.io.input;

import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Source;
import org.openlca.proto.ProtoSource;

class SourceImport implements Import<Source>{

  private final ProtoImport imp;

  SourceImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<Source> of(String id) {
    var source = imp.get(Source.class, id);

    // check if we are in update mode
    var update = false;
    if (source != null) {
      update = imp.shouldUpdate(source);
      if(!update) {
        return ImportStatus.skipped(source);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getSource(id);
    if (proto == null)
      return source != null
        ? ImportStatus.skipped(source)
        : ImportStatus.error("Could not resolve Source " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(source, wrap))
        return ImportStatus.skipped(source);
    }

    // map the data
    if (source == null) {
      source = new Source();
    }
    wrap.mapTo(source, imp);
    map(proto, source);

    // insert or update it
    var dao = new SourceDao(imp.db);
    source = update
      ? dao.update(source)
      : dao.insert(source);
    imp.putHandled(source);
    return update
      ? ImportStatus.updated(source)
      : ImportStatus.created(source);
  }

  private void map(ProtoSource proto, Source source) {
    source.url = proto.getUrl();
    source.externalFile = proto.getExternalFile();
    source.textReference = proto.getTextReference();
    var year = proto.getYear();
    if (year != 0) {
      source.year = (short) year;
    }
  }
}

