package org.openlca.proto.io;

import java.util.Set;

import org.openlca.core.model.ModelType;
import org.openlca.proto.Proto;

public interface ProtoReader {

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

}
