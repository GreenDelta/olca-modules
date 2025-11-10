package org.openlca.proto.io.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.input.UpdateMode;
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
import org.openlca.proto.io.InMemoryProtoStore;
import org.openlca.proto.io.Tests;
import org.openlca.proto.io.output.Out;

public class ImportTest {

	private final IDatabase db = Tests.db();

	@Test
	public void testUpdate() throws Exception {
		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;

			var id = UUID.randomUUID().toString();
			var instance = (RootEntity) type.getModelClass()
				.getConstructor()
				.newInstance();
			instance.refId = id;
			instance.name = type.name();
			instance.version = Version.valueOf(0, 0, 1);
			instance.lastChange = new Date().getTime();

			// create new object
			var m1 = put(type, instance);
			assertTrue(m1.id > 0);
			assertEquals(m1.refId, instance.refId);

			// update object
			instance.version = Version.valueOf(0, 0, 2);
			var m2 = put(type, instance);
			assertEquals(m1.id, m2.id);
			assertEquals(instance.version, m2.version);

			// delete it from the database
			db.delete(m2);
		}
	}

	private RootEntity put(ModelType type, RootEntity entity) {

		var store = InMemoryProtoStore.create();
		var proto = Out.toProto(db, entity);

		switch (type) {
			case ACTOR -> store.putActor((ProtoActor) proto);
			case CURRENCY -> store.putCurrency((ProtoCurrency) proto);
			case DQ_SYSTEM -> store.putDQSystem((ProtoDQSystem) proto);
			case EPD -> store.putEpd((ProtoEpd) proto);
			case FLOW -> store.putFlow((ProtoFlow) proto);
			case FLOW_PROPERTY -> store.putFlowProperty((ProtoFlowProperty) proto);
			case IMPACT_CATEGORY -> store.putImpactCategory(
				(ProtoImpactCategory) proto);
			case IMPACT_METHOD -> store.putImpactMethod((ProtoImpactMethod) proto);
			case LOCATION -> store.putLocation((ProtoLocation) proto);
			case PARAMETER -> store.putParameter((ProtoParameter) proto);
			case PROCESS -> store.putProcess((ProtoProcess) proto);
			case PRODUCT_SYSTEM -> store.putProductSystem((ProtoProductSystem) proto);
			case PROJECT -> store.putProject((ProtoProject) proto);
			case RESULT -> store.putResult((ProtoResult) proto);
			case SOCIAL_INDICATOR -> store.putSocialIndicator(
				(ProtoSocialIndicator) proto);
			case SOURCE -> store.putSource((ProtoSource) proto);
			case UNIT_GROUP -> store.putUnitGroup((ProtoUnitGroup) proto);
		}

		return new ProtoImport(store, db)
			.setUpdateMode(UpdateMode.IF_NEWER)
			.run(type, entity.refId);
	}
}
