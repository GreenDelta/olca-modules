package org.openlca.proto.io;

import java.util.Set;

import org.openlca.core.model.ModelType;
import org.openlca.proto.Proto;
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

public interface ProtoReader {

  Set<String> getIds(ModelType modelType);

  ProtoCategory getCategory(String id);

  ProtoActor getActor(String id);

  ProtoSource getSource(String id);

  ProtoCurrency getCurrency(String id);

  ProtoUnitGroup getUnitGroup(String id);

  ProtoFlowProperty getFlowProperty(String id);

  ProtoDQSystem getDQSystem(String id);

  ProtoFlow getFlow(String id);

  ProtoImpactMethod getImpactMethod(String id);

  ProtoLocation getLocation(String id);

  ProtoParameter getParameter(String id);

  ProtoImpactCategory getImpactCategory(String id);

  ProtoProcess getProcess(String id);

  ProtoProject getProject(String id);

  ProtoSocialIndicator getSocialIndicator(String id);

  ProtoProductSystem getProductSystem(String id);

}
