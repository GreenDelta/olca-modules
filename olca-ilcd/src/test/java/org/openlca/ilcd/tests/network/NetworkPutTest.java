package org.openlca.ilcd.tests.network;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.SampleSource;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyReference;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.FlowBuilder;
import org.openlca.ilcd.util.FlowPropertyBag;
import org.openlca.ilcd.util.FlowPropertyBuilder;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.UnitGroupBag;
import org.openlca.ilcd.util.UnitGroupBuilder;

public class NetworkPutTest {

	private NetworkClient client;

	@Before
	public void setUp() throws Exception {
		if (!Network.isAppAlive())
			return;
		client = Network.createClient();
	}

	@Test
	public void testPutSource() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		org.openlca.ilcd.sources.DataSetInformation dataSetInfo = new org.openlca.ilcd.sources.DataSetInformation();
		String name = "xtest source - " + new Random().nextInt(1000);
		LangString.addLabel(dataSetInfo.getShortName(), name);
		dataSetInfo.setUUID(id);
		Source source = SampleSource.create();
		source.getSourceInformation().getDataSetInformation().setUUID(id);
		client.put(source, id);
	}

	@Test
	public void testPutUnitGroup() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		UnitGroup unitGroup = makeUnitGroup(id);
		client.put(unitGroup, id);
	}

	private UnitGroup makeUnitGroup(String id) {
		org.openlca.ilcd.units.DataSetInformation dataSetInfo = new org.openlca.ilcd.units.DataSetInformation();
		String name = "xtest unit group - " + new Random().nextInt(1000);
		LangString.addLabel(dataSetInfo.getName(), name);
		dataSetInfo.setUUID(id);
		Unit unit = new Unit();
		unit.setDataSetInternalID(BigInteger.valueOf(0));
		unit.setMeanValue(1.0);
		unit.setName("kg");
		UnitGroup unitGroup = UnitGroupBuilder.makeUnitGroup()
				.withBaseUri(Network.RESOURCE_URL).withDataSetInfo(dataSetInfo)
				.withReferenceUnitId(0)
				.withUnits(Collections.singletonList(unit)).getUnitGroup();
		return unitGroup;
	}

	private DataSetReference toRef(UnitGroup group) {
		UnitGroupBag bag = new UnitGroupBag(group);
		DataSetReference ref = new DataSetReference();
		ref.setType(DataSetType.UNIT_GROUP_DATA_SET);
		ref.setUri("../unitgroups/" + bag.getId());
		ref.setUuid(bag.getId());
		ref.setVersion("01.00.000");
		LangString.addShortText(ref.getShortDescription(), bag.getName());
		return ref;
	}

	@Test
	public void testPutFlowProperty() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		FlowProperty flowProperty = makeFlowProperty(id);
		client.put(flowProperty, id);
	}

	private FlowProperty makeFlowProperty(String id) {
		org.openlca.ilcd.flowproperties.DataSetInformation dataSetInfo = new org.openlca.ilcd.flowproperties.DataSetInformation();
		String name = "xtest flow property - " + new Random().nextInt(1000);
		LangString.addLabel(dataSetInfo.getName(), name);
		dataSetInfo.setUUID(id);
		DataSetReference unitGroupRef = toRef(makeUnitGroup(id));
		FlowProperty flowProperty = FlowPropertyBuilder.makeFlowProperty()
				.withBaseUri(Network.RESOURCE_URL).withDataSetInfo(dataSetInfo)
				.withUnitGroupReference(unitGroupRef).getFlowProperty();
		return flowProperty;
	}

	private DataSetReference toRef(FlowProperty prop) {
		FlowPropertyBag bag = new FlowPropertyBag(prop);
		DataSetReference ref = new DataSetReference();
		ref.setType(DataSetType.UNIT_GROUP_DATA_SET);
		ref.setUri("../unitgroups/" + bag.getId());
		ref.setUuid(bag.getId());
		ref.setVersion("01.00.000");
		LangString.addShortText(ref.getShortDescription(), bag.getName());
		return ref;
	}

	@Test
	public void testPutFlow() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		org.openlca.ilcd.flows.DataSetInformation dataSetInfo = new org.openlca.ilcd.flows.DataSetInformation();
		String name = "xtest flow - " + new Random().nextInt(1000);
		FlowName flowName = new FlowName();
		dataSetInfo.setName(flowName);
		LangString.addLabel(flowName.getBaseName(), name);
		dataSetInfo.setUUID(id);
		FlowPropertyReference propRef = new FlowPropertyReference();
		propRef.setMeanValue(1.0);
		propRef.setDataSetInternalID(BigInteger.valueOf(0));
		propRef.setFlowProperty(toRef(makeFlowProperty(id)));
		Flow flow = FlowBuilder.makeFlow().withBaseUri(Network.RESOURCE_URL)
				.withDataSetInfo(dataSetInfo)
				.withFlowProperties(Collections.singletonList(propRef))
				.withReferenceFlowPropertyId(0).getFlow();
		client.put(flow, id);
	}

}
