package org.openlca.core.database.descriptors;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.TLongSets;

public class DescriptorTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testRootDescriptors() throws Exception {

		var cat = db.insert(Category.of("some", null));
		var refId = UUID.randomUUID().toString();
		var version = Version.of(1, 2, 3).getValue();
		var lastChange = System.nanoTime();

		Consumer<RootDescriptor> check = d -> {
			assertEquals(refId, d.refId);
			assertEquals(d.type.name(), d.name);
			assertEquals("description", d.description);
			assertEquals("tags", d.tags);
			assertEquals(version, d.version);
			assertEquals(lastChange, d.lastChange);
			assertEquals("lib7", d.library);
			assertEquals(cat.id, d.category.longValue());
		};

		Consumer<Collection<? extends Descriptor>> checkIn = ds -> {
			var d = ds.stream()
					.filter(x -> refId.equals(x.refId))
					.findFirst()
					.orElseThrow();
			check.accept((RootDescriptor) d);
		};

		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;

			var clazz = type.getModelClass();
			var e = clazz.getConstructor().newInstance();
			e.refId = refId;
			e.name = type.name();
			e.category = cat;
			e.description = "description";
			e.tags = "tags";
			e.version = version;
			e.lastChange = lastChange;
			e.library = "lib7";
			db.insert(e);

			check.accept(db.getDescriptor(clazz, e.id));
			check.accept(db.getDescriptor(clazz, e.refId));
			checkIn.accept(db.getDescriptors(clazz));
			checkIn.accept(db.getDescriptors(clazz, Set.of(e.id)));
			checkIn.accept(db.getDescriptors(clazz, TLongSets.singleton(e.id)));

			var dao = Daos.root(db, clazz);
			check.accept(dao.getDescriptor(e.id));
			check.accept(dao.getDescriptorForRefId(e.refId));
			checkIn.accept(dao.getDescriptors());
			checkIn.accept(dao.getDescriptors(Set.of(e.id)));
			checkIn.accept(dao.getDescriptors(Optional.of(cat)));

			db.delete(e);
		}

		db.delete(cat);

	}


}
