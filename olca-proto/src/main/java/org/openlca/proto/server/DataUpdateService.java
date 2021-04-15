package org.openlca.proto.server;

import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.MemStore;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.data.DataSet;
import org.openlca.proto.generated.data.DataUpdateServiceGrpc;
import org.openlca.proto.generated.data.DeleteRequest;
import org.openlca.proto.input.ProtoImport;
import org.openlca.util.Strings;

public class DataUpdateService extends
  DataUpdateServiceGrpc.DataUpdateServiceImplBase {

  private final IDatabase db;

  DataUpdateService(IDatabase db) {
    this.db = db;
  }

  @Override
  public void delete(DeleteRequest req, StreamObserver<Empty> resp) {

    var modelType = DataUtil.forceCategorizedTypeOf(
      req.getModelType(), resp);
    if (modelType == null)
      return;
    var id = req.getId();
    if (Strings.nullOrEmpty(id)) {
      Response.invalidArg(resp, "A data set ID is required");
      return;
    }

    var model = db.get(modelType.getModelClass(), id);
    if (model == null) {
      Response.notFound(resp,
        "A data set of type " + modelType + " with id=" + id
        + " does not exist");
      return;
    }
    db.delete(model);
    Response.ok(resp);
  }

  @Override
  public void put(DataSet dataSet, StreamObserver<Proto.Ref> resp) {

    // put the data set into an in memory store
    var store = MemStore.create();
    if (dataSet.hasActor()) {
      store.putActor(dataSet.getActor());
    } else if (dataSet.hasCategory()) {
      store.putCategory(dataSet.getCategory());
    } else if (dataSet.hasCurrency()) {
      store.putCurrency(dataSet.getCurrency());
    } else if (dataSet.hasDqSystem()) {
      store.putDQSystem(dataSet.getDqSystem());
    } else if (dataSet.hasFlow()) {
      store.putFlow(dataSet.getFlow());
    } else if (dataSet.hasFlowProperty()) {
      store.putFlowProperty(dataSet.getFlowProperty());
    } else if (dataSet.hasImpactCategory()) {
      store.putImpactCategory(dataSet.getImpactCategory());
    } else if (dataSet.hasImpactMethod()) {
      store.putImpactMethod(dataSet.getImpactMethod());
    } else if (dataSet.hasLocation()) {
      store.putLocation(dataSet.getLocation());
    } else if (dataSet.hasParameter()) {
      store.putParameter(dataSet.getParameter());
    } else if (dataSet.hasProcess()) {
      store.putProcess(dataSet.getProcess());
    } else if (dataSet.hasProductSystem()) {
      store.putProductSystem(dataSet.getProductSystem());
    } else if (dataSet.hasProject()) {
      store.putProject(dataSet.getProject());
    } else if (dataSet.hasSocialIndicator()) {
      store.putSocialIndicator(dataSet.getSocialIndicator());
    } else if (dataSet.hasSource()) {
      store.putSource(dataSet.getSource());
    } else if (dataSet.hasUnitGroup()) {
      store.putUnitGroup(dataSet.getUnitGroup());
    }

    // run the import
    var obj = new AtomicReference<RootEntity>();
    new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.ALWAYS)
      .run();

  }
}
