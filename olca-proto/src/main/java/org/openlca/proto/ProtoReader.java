package org.openlca.proto;

import java.io.Closeable;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.ModelType;
import org.openlca.proto.generated.Proto;

public interface ProtoReader extends Closeable {

  Set<String> getIds(ModelType modelType);

  Proto.Category getCategory(String id);

  Proto.Actor getActor(String id);

  Proto.Source getSource(String id);

  Proto.Currency getCurrency(String id);

  Proto.UnitGroup getUnitGroup(String id);

  Proto.FlowProperty getFlowProperty(String id);

  Proto.DQSystem getDQSystem(String id);

  Proto.Flow getFlow(String id);

  Proto.ImpactMethod getImpactMethod(String id);

  Proto.Location getLocation(String id);

  Proto.Parameter getParameter(String id);

  Proto.ImpactCategory getImpactCategory(String id);

  Proto.Process getProcess(String id);

  Proto.Project getProject(String id);

  Proto.SocialIndicator getSocialIndicator(String id);

  Proto.ProductSystem getProductSystem(String id);

  Proto.NwSet getNwSet(String id);

}
