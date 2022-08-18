package org.openlca.proto.io;

import java.util.Set;

import org.openlca.core.model.ModelType;
import org.openlca.proto.ProtoActor;
import org.openlca.proto.ProtoCurrency;
import org.openlca.proto.ProtoDQSystem;
import org.openlca.proto.ProtoEpd;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowProperty;
import org.openlca.proto.ProtoImpactCategory;
import org.openlca.proto.ProtoImpactMethod;
import org.openlca.proto.ProtoLocation;
import org.openlca.proto.ProtoParameter;
import org.openlca.proto.ProtoProcess;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.proto.ProtoProject;
import org.openlca.proto.ProtoResult;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.proto.ProtoSource;
import org.openlca.proto.ProtoUnitGroup;

public interface ProtoStoreReader {

  Set<String> getIds(ModelType modelType);

  ProtoActor getActor(String id);

	ProtoCurrency getCurrency(String id);

	ProtoDQSystem getDQSystem(String id);

	ProtoEpd getEpd(String id);

	ProtoFlow getFlow(String id);

	ProtoFlowProperty getFlowProperty(String id);

	ProtoImpactCategory getImpactCategory(String id);

	ProtoImpactMethod getImpactMethod(String id);

	ProtoLocation getLocation(String id);

	ProtoParameter getParameter(String id);

	ProtoProcess getProcess(String id);

	ProtoProductSystem getProductSystem(String id);

	ProtoProject getProject(String id);

	ProtoResult getResult(String id);

	ProtoSocialIndicator getSocialIndicator(String id);

  ProtoSource getSource(String id);

  ProtoUnitGroup getUnitGroup(String id);

}
