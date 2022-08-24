package org.openlca.proto.io.output;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class WriterTest {

	@Test
	public void testWriteCategoryPath() {
		var actor = Actor.of("actor");
		var path = "Some/Actor/Category";
		Category category = null;
		for (var seg : path.split("/")) {
			var next = Category.of(seg, ModelType.ACTOR);
			if (category != null) {
				next.category = category;
				category.childCategories.add(next);
			}
			category = next;
		}
		actor.category = category;

		var proto = new ActorWriter().write(actor);
		assertEquals(path, proto.getCategory());
	}

}
