package org.openlca.proto.io;

import org.openlca.proto.Proto;

public interface ProtoWriter {

  void putActor(Proto.Actor actor);

  void putCategory(Proto.Category category);

  void putCurrency(Proto.Currency currency);

  void putDQSystem(Proto.DQSystem dqSystem);

  void putFlow(Proto.Flow flow);

  void putFlowProperty(Proto.FlowProperty property);

  void putImpactCategory(Proto.ImpactCategory impact);

  void putImpactMethod(Proto.ImpactMethod method);

  void putLocation(Proto.Location location);

  void putParameter(Proto.Parameter parameter);

  void putProcess(Proto.Process process);

  void putProductSystem(Proto.ProductSystem system);

  void putProject(Proto.Project project);

  void putSocialIndicator(Proto.SocialIndicator indicator);

  void putSource(Proto.Source source);

  void putUnitGroup(Proto.UnitGroup group);

}
