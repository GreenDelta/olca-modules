package org.openlca.proto.io;

import org.openlca.proto.ProtoActor;
import org.openlca.proto.ProtoCategory;
import org.openlca.proto.ProtoCurrency;
import org.openlca.proto.ProtoDQSystem;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowProperty;
import org.openlca.proto.ProtoImpactCategory;
import org.openlca.proto.ProtoImpactMethod;
import org.openlca.proto.ProtoLocation;
import org.openlca.proto.ProtoParameter;
import org.openlca.proto.ProtoProcess;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.proto.ProtoProject;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.proto.ProtoSource;
import org.openlca.proto.ProtoUnitGroup;

public interface ProtoWriter {

  void putActor(ProtoActor actor);

  void putCategory(ProtoCategory category);

  void putCurrency(ProtoCurrency currency);

  void putDQSystem(ProtoDQSystem dqSystem);

  void putFlow(ProtoFlow flow);

  void putFlowProperty(ProtoFlowProperty property);

  void putImpactCategory(ProtoImpactCategory impact);

  void putImpactMethod(ProtoImpactMethod method);

  void putLocation(ProtoLocation location);

  void putParameter(ProtoParameter parameter);

  void putProcess(ProtoProcess process);

  void putProductSystem(ProtoProductSystem system);

  void putProject(ProtoProject project);

  void putSocialIndicator(ProtoSocialIndicator indicator);

  void putSource(ProtoSource source);

  void putUnitGroup(ProtoUnitGroup group);

}
