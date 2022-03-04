package org.openlca.proto.io.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import com.google.protobuf.Empty;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoType;
import org.openlca.proto.grpc.DataFetchServiceGrpc;
import org.openlca.proto.grpc.GetAllRequest;
import org.openlca.proto.grpc.GetAllResponse;
import org.openlca.proto.grpc.GetDescriptorsRequest;
import org.openlca.proto.grpc.ProtoTechFlow;
import org.openlca.proto.io.Tests;

public class DataFetchTest {

	private final IDatabase db = Tests.db();

	@Test
	public void getDescriptors() {

		// create some actors
		var actors = new Actor[10];
		var found = new boolean[actors.length];
		for (int i = 0; i < actors.length; i++) {
			actors[i] = db.insert(Actor.of("actor " + i));
		}

		// collect the descriptors from the service
		ServiceTests.on(channel -> {
			var stub = DataFetchServiceGrpc.newBlockingStub(channel);
			var refs = stub.getDescriptors(
				GetDescriptorsRequest.newBuilder()
					.setType(ProtoType.Actor)
					.build());
			while (refs.hasNext()) {
				var ref = refs.next();
				for (int i = 0; i < actors.length; i++) {
					if (ref.getId().equals(actors[i].refId)) {
						found[i] = true;
					}
				}
			}
		});

		// delete them
		for (int i = 0; i < actors.length; i++) {
			assertTrue(found[i]);
			db.delete(actors[i]);
		}
	}

	@Test
	public void testGetAll() {

		// create and call the deleteAll function
		Runnable deleteAll = () -> db.getDescriptors(Actor.class)
			.forEach(d -> {
				var actor = db.get(Actor.class, d.id);
				db.delete(actor);
			});
		deleteAll.run();

		// create 142 actors
		int count = 142;
		var created = new HashMap<String, Actor>();
		for (int i = 0; i < count; i++) {
			var actor = new Actor();
			actor.refId = UUID.randomUUID().toString();
			actor.name = "actor " + i;
			db.insert(actor);
			created.put(actor.refId, actor);
		}
		assertEquals(count, created.size());

		// check that the actors in the given response match the expected
		// size and that all actors are also in the database
		BiConsumer<Integer, GetAllResponse> check = (size, response) -> {
			assertEquals(size.intValue(), response.getPageSize());
			int found = 0;
			for (var ds : response.getDataSetList()) {
				found++;
				assertTrue(ds.hasActor());
				var dbActor = created.get(ds.getActor().getId());
				assertEquals(dbActor.name, ds.getActor().getName());
			}
			assertEquals(size.intValue(), found);
		};

		// execute the requests
		ServiceTests.on(channel -> {
			var stub = DataFetchServiceGrpc.newBlockingStub(channel);
			var req = GetAllRequest.newBuilder()
				.setType(ProtoType.Actor);

			var firstPage = stub.getAll(req.build());
			assertEquals(1, firstPage.getPage());
			check.accept(100, firstPage);

			var secondPage = stub.getAll(req.setPage(2).build());
			assertEquals(2, secondPage.getPage());
			check.accept(42, secondPage);

			var emptyPage = stub.getAll(req.setPage(3).build());
			assertEquals(3, emptyPage.getPage());
			check.accept(0, emptyPage);

			var smallPage = stub.getAll(req.setPage(3).setPageSize(10).build());
			assertEquals(3, smallPage.getPage());
			check.accept(10, smallPage);

			var bigPage = stub.getAll(req.setPage(1).setPageSize(1000).build());
			assertEquals(1, bigPage.getPage());
			check.accept(count, bigPage);
		});

		deleteAll.run();
	}

	@Test
	public void testGetProviders() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("Product", mass);
		var process = Process.of("Process", product);

		db.insert(units, mass, product, process);
		var ref = new AtomicReference<ProtoTechFlow>();

		ServiceTests.on(channel -> {
			var fetch = DataFetchServiceGrpc.newBlockingStub(channel);
			var techFlows = fetch.getTechFlows(Empty.newBuilder().build());
			while (techFlows.hasNext()) {
				var next = techFlows.next();
				if (next.getProcess().getId().equals(process.refId)) {
					ref.set(next);
					break;
				}
			}
		});

		var proto = ref.get();
		assertNotNull(proto);
		assertTrue(proto.hasProcess());
		assertTrue(proto.hasProduct());
		assertFalse(proto.hasWaste());
		assertEquals(process.refId, proto.getProcess().getId());
		assertEquals(product.refId, proto.getProduct().getId());
		assertEquals("kg", proto.getProduct().getRefUnit());

		db.delete(process, product, mass, units);
	}

}
