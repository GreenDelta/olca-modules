package org.openlca.proto.input;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class DqSystemImport {

  private final ProtoImport imp;

  public DqSystemImport(ProtoImport imp) {
    this.imp = imp;
  }

  public DQSystem of(String id) {
    if (id == null)
      return null;
    var dqSystem = imp.get(DQSystem.class, id);

    // check if we are in update mode
    var update = false;
    if (dqSystem != null) {
      update = imp.shouldUpdate(dqSystem);
      if(!update) {
        return dqSystem;
      }
    }

    // check the proto object
    var proto = imp.store.getDQSystem(id);
    if (proto == null)
      return dqSystem;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(dqSystem, wrap))
        return dqSystem;
    }

    // map the data
    if (dqSystem == null) {
      dqSystem = new DQSystem();
      dqSystem.refId = id;
    }
    wrap.mapTo(dqSystem, imp);
    map(proto, dqSystem);

    // insert it
    var dao = new DQSystemDao(imp.db);
    dqSystem = update
      ? dao.update(dqSystem)
      : dao.insert(dqSystem);
    imp.putHandled(dqSystem);
    return dqSystem;
  }

  private void map(Proto.DQSystem proto, DQSystem sys) {
    sys.hasUncertainties = proto.getHasUncertainties();
    var sourceID = proto.getSource().getId();
    if (Strings.notEmpty(sourceID)) {
      sys.source = new SourceImport(imp).of(sourceID);
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
