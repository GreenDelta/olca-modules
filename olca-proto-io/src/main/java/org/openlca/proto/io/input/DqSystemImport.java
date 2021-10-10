package org.openlca.proto.io.input;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.proto.ProtoDQSystem;
import org.openlca.util.Strings;

class DqSystemImport implements Import<DQSystem> {

  private final ProtoImport imp;

  DqSystemImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<DQSystem> of(String id) {
    var dqSystem = imp.get(DQSystem.class, id);

    // check if we are in update mode
    var update = false;
    if (dqSystem != null) {
      update = imp.shouldUpdate(dqSystem);
      if(!update) {
        return ImportStatus.skipped(dqSystem);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getDQSystem(id);
    if (proto == null)
      return dqSystem != null
        ? ImportStatus.skipped(dqSystem)
        : ImportStatus.error("Could not resolve DQSystem " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(dqSystem, wrap))
        return ImportStatus.skipped(dqSystem);
    }

    // map the data
    if (dqSystem == null) {
      dqSystem = new DQSystem();
      dqSystem.refId = id;
    }
    wrap.mapTo(dqSystem, imp);
    map(proto, dqSystem);

    // insert or update it
    var dao = new DQSystemDao(imp.db);
    dqSystem = update
      ? dao.update(dqSystem)
      : dao.insert(dqSystem);
    imp.putHandled(dqSystem);
    return update
      ? ImportStatus.updated(dqSystem)
      : ImportStatus.created(dqSystem);
  }

  private void map(ProtoDQSystem proto, DQSystem sys) {
    sys.hasUncertainties = proto.getHasUncertainties();
    var sourceID = proto.getSource().getId();
    if (Strings.notEmpty(sourceID)) {
      sys.source = new SourceImport(imp).of(sourceID).model();
    }
    for (var protoInd : proto.getIndicatorsList()) {
      var ind = new DQIndicator();
      sys.indicators.add(ind);
      ind.name = protoInd.getName();
      ind.position = protoInd.getPosition();
      for (var protoScore : protoInd.getScoresList()) {
        var score = new DQScore();
        ind.scores.add(score);
        score.position = protoScore.getPosition();
        score.label = protoScore.getLabel();
        score.description = protoScore.getDescription();
        score.uncertainty = protoScore.getUncertainty();
      }
    }
  }
}
