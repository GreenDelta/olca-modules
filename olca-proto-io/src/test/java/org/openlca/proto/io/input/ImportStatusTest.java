package org.openlca.proto.io.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.input.UpdateMode;
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
import org.openlca.proto.io.InMemoryProtoStore;
import org.openlca.proto.io.Tests;
import org.openlca.proto.io.output.Out;

public class ImportStatusTest {

	private final IDatabase db = Tests.db();
	private ProtoImport protoImport;

	@Before
	public void setup() {
		protoImport = new ProtoImport(InMemoryProtoStore.create(), db);
	}

	@Test
	public void testNullId() {
		for (var type : ModelType.values()) {
			// TODO: support results
			if (!type.isRoot()
				|| type == ModelType.RESULT
				|| type == ModelType.EPD)
				continue;
			var imp = protoImport.getImport(type);
			var status = imp.of(null);
			assertTrue(status.isError());
			assertNull(status.model());
			assertTrue(status.error()
				.startsWith("Could not resolve"));
		}
	}

	@Test
	public void testUnknownId() {
		for (var type : ModelType.values()) {
			// TODO: support results
			if (!type.isRoot()
				|| type == ModelType.RESULT
				|| type == ModelType.EPD)
				continue;
			var imp = protoImport.getImport(type);
			var status = imp.of(UUID.randomUUID().toString());
			assertTrue(status.isError());
			assertNull(status.model());
			assertTrue(status.error()
				.startsWith("Could not resolve"));
		}
	}

	@Test
	public void testUpdate() throws Exception {
		int i = 0;
		for (var type : ModelType.values()) {
			// TODO: support results
			if (!type.isRoot()
				|| type == ModelType.CATEGORY
				|| type == ModelType.RESULT
				|| type == ModelType.EPD)
				continue;
			i++;

			var id = UUID.randomUUID().toString();
			var instance = (RootEntity) type.getModelClass()
				.getConstructor()
				.newInstance();
			instance.refId = id;
			instance.name = type.name();
			instance.version = Version.valueOf(0, 0, 1);
			instance.lastChange = new Date().getTime();

			// create new object
			var status1 = put(type, instance);
			assertTrue(status1.isCreated());
			assertEquals(id, status1.model().refId);

			// skip existing
			var status2 = put(type, instance);
			assertTrue(status2.isSkipped());
			assertEquals(id, status2.model().refId);

			// update existing
			instance.version = Version.valueOf(0, 0, 2);
			var status3 = put(type, instance);
			assertTrue(status3.isUpdated());
			assertEquals(id, status3.model().refId);

			// delete it from the database
			db.delete(status3.model());
		}
		assertEquals(15, i);
	}

	private ImportStatus<?> put(ModelType type, RefEntity entity) {

		var store = InMemoryProtoStore.create();
		var proto = Out.toProto(db, entity);

		switch (type) {
			case ACTOR -> store.putActor(
				(ProtoActor) proto);
			case CATEGORY -> store.putCategory(
				(ProtoCategory) proto);
			case CURRENCY -> store.putCurrency(
				(ProtoCurrency) proto);
			case DQ_SYSTEM -> store.putDQSystem(
				(ProtoDQSystem) proto);
			case FLOW -> store.putFlow(
				(ProtoFlow) proto);
			case FLOW_PROPERTY -> store.putFlowProperty(
				(ProtoFlowProperty) proto);
			case IMPACT_CATEGORY -> store.putImpactCategory(
				(ProtoImpactCategory) proto);
			case IMPACT_METHOD -> store.putImpactMethod(
				(ProtoImpactMethod) proto);
			case LOCATION -> store.putLocation(
				(ProtoLocation) proto);
			case PARAMETER -> store.putParameter(
				(ProtoParameter) proto);
			case PROCESS -> store.putProcess(
				(ProtoProcess) proto);
			case PRODUCT_SYSTEM -> store.putProductSystem(
				(ProtoProductSystem) proto);
			case PROJECT -> store.putProject(
				(ProtoProject) proto);
			case SOCIAL_INDICATOR -> store.putSocialIndicator(
				(ProtoSocialIndicator) proto);
			case SOURCE -> store.putSource(
				(ProtoSource) proto);
			case UNIT_GROUP -> store.putUnitGroup(
				(ProtoUnitGroup) proto);
		}

		var protoImport = new ProtoImport(store, db)
			.withUpdateMode(UpdateMode.IF_NEWER);
		return protoImport.getImport(type).of(entity.refId);
	}

}
