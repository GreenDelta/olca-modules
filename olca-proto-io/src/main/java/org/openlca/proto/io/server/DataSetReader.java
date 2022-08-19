package org.openlca.proto.io.server;

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
import org.openlca.proto.grpc.ProtoDataSet;
import org.openlca.proto.io.ProtoStoreReader;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

record DataSetReader(ProtoDataSet dataSet) implements ProtoStoreReader {

	static DataSetReader of(ProtoDataSet dataSet) {
		return new DataSetReader(dataSet);
	}

	@Override
	public Set<String> getIds(ModelType type) {
		if (type == null)
			return Collections.emptySet();
		if (dataSet.hasActor())
			return Set.of(dataSet.getActor().getId());
		if (dataSet.hasCurrency())
			return Set.of(dataSet.getCurrency().getId());
		if (dataSet.hasDqSystem())
			return Set.of(dataSet.getDqSystem().getId());
		if (dataSet.hasEpd())
			return Set.of(dataSet.getEpd().getId());
		if (dataSet.hasFlowProperty())
			return Set.of(dataSet.getFlowProperty().getId());
		if (dataSet.hasFlow())
			return Set.of(dataSet.getFlow().getId());
		if (dataSet.hasImpactCategory())
			return Set.of(dataSet.getImpactCategory().getId());
		if (dataSet.hasImpactMethod())
			return Set.of(dataSet.getImpactMethod().getId());
		if (dataSet.hasLocation())
			return Set.of(dataSet.getLocation().getId());
		if (dataSet.hasParameter())
			return Set.of(dataSet.getParameter().getId());
		if (dataSet.hasProcess())
			return Set.of(dataSet.getProcess().getId());
		if (dataSet.hasProductSystem())
			return Set.of(dataSet.getProductSystem().getId());
		if (dataSet.hasProject())
			return Set.of(dataSet.getProject().getId());
		if (dataSet.hasResult())
			return Set.of(dataSet.getResult().getId());
		if (dataSet.hasSocialIndicator())
			return Set.of(dataSet.getSocialIndicator().getId());
		if (dataSet.hasSource())
			return Set.of(dataSet.getSource().getId());
		if (dataSet.hasUnitGroup())
			return Set.of(dataSet.getUnitGroup().getId());
		return Collections.emptySet();
	}

	@Override
	public ProtoActor getActor(String id) {
		if (!dataSet.hasActor())
			return null;
		return Objects.equals(id, dataSet.getActor().getId())
			? dataSet.getActor()
			: null;
	}

	@Override
	public ProtoCurrency getCurrency(String id) {
		if (!dataSet.hasCurrency())
			return null;
		return Objects.equals(id, dataSet.getCurrency().getId())
			? dataSet.getCurrency()
			: null;
	}

	@Override
	public ProtoDQSystem getDQSystem(String id) {
		if (!dataSet.hasDqSystem())
			return null;
		return Objects.equals(id, dataSet.getDqSystem().getId())
			? dataSet.getDqSystem()
			: null;
	}

	@Override
	public ProtoEpd getEpd(String id) {
		if (!dataSet.hasEpd())
			return null;
		return Objects.equals(id, dataSet.getEpd().getId())
			? dataSet.getEpd()
			: null;
	}

	@Override
	public ProtoFlowProperty getFlowProperty(String id) {
		if (!dataSet.hasFlowProperty())
			return null;
		return Objects.equals(id, dataSet.getFlowProperty().getId())
			? dataSet.getFlowProperty()
			: null;
	}

	@Override
	public ProtoFlow getFlow(String id) {
		if (!dataSet.hasFlow())
			return null;
		return Objects.equals(id, dataSet.getFlow().getId())
			? dataSet.getFlow()
			: null;
	}

	@Override
	public ProtoImpactCategory getImpactCategory(String id) {
		if (!dataSet.hasImpactCategory())
			return null;
		return Objects.equals(id, dataSet.getImpactCategory().getId())
			? dataSet.getImpactCategory()
			: null;
	}

	@Override
	public ProtoImpactMethod getImpactMethod(String id) {
		if (!dataSet.hasImpactMethod())
			return null;
		return Objects.equals(id, dataSet.getImpactMethod().getId())
			? dataSet.getImpactMethod()
			: null;
	}

	@Override
	public ProtoLocation getLocation(String id) {
		if (!dataSet.hasLocation())
			return null;
		return Objects.equals(id, dataSet.getLocation().getId())
			? dataSet.getLocation()
			: null;
	}

	@Override
	public ProtoParameter getParameter(String id) {
		if (!dataSet.hasParameter())
			return null;
		return Objects.equals(id, dataSet.getParameter().getId())
			? dataSet.getParameter()
			: null;
	}

	@Override
	public ProtoProcess getProcess(String id) {
		if (!dataSet.hasProcess())
			return null;
		return Objects.equals(id, dataSet.getProcess().getId())
			? dataSet.getProcess()
			: null;
	}

	@Override
	public ProtoProductSystem getProductSystem(String id) {
		if (!dataSet.hasProductSystem())
			return null;
		return Objects.equals(id, dataSet.getProductSystem().getId())
			? dataSet.getProductSystem()
			: null;
	}

	@Override
	public ProtoProject getProject(String id) {
		if (!dataSet.hasProject())
			return null;
		return Objects.equals(id, dataSet.getProject().getId())
			? dataSet.getProject()
			: null;
	}

	@Override
	public ProtoResult getResult(String id) {
		if (!dataSet.hasResult())
			return null;
		return Objects.equals(id, dataSet.getResult().getId())
			? dataSet.getResult()
			: null;
	}

	@Override
	public ProtoSocialIndicator getSocialIndicator(String id) {
		if (!dataSet.hasSocialIndicator())
			return null;
		return Objects.equals(id, dataSet.getSocialIndicator().getId())
			? dataSet.getSocialIndicator()
			: null;
	}

	@Override
	public ProtoSource getSource(String id) {
		if (!dataSet.hasSource())
			return null;
		return Objects.equals(id, dataSet.getSource().getId())
			? dataSet.getSource()
			: null;
	}

	@Override
	public ProtoUnitGroup getUnitGroup(String id) {
		if (!dataSet.hasUnitGroup())
			return null;
		return Objects.equals(id, dataSet.getUnitGroup().getId())
			? dataSet.getUnitGroup()
			: null;
	}
}
