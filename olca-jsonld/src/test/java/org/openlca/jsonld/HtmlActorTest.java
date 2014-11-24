package org.openlca.jsonld;

import java.util.UUID;
import org.junit.Test;
import org.openlca.core.database.DatabaseContent;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.model.Actor;

public class HtmlActorTest {


	@Test
	public void testWriteHtml() {
		Actor actor = new Actor();
		actor.setName("Test");
		actor.setRefId(UUID.randomUUID().toString());
		String html = Document.toHtml(actor,
				DerbyDatabase.createInMemory(DatabaseContent.EMPTY));
		System.out.println(html);

	}


}
