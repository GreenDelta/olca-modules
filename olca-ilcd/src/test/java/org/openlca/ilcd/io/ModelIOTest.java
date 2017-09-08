package org.openlca.ilcd.io;

import java.io.StringWriter;
import java.util.UUID;

import org.junit.Test;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.util.Models;

public class ModelIOTest {

	@Test
	public void testSimpleModel() throws Exception {
		Model model = new Model();
		Models.dataSetInfo(model).uuid = UUID.randomUUID().toString();
		Models.modelName(model).name.add(LangString.of("Example model", "en"));

		Classification classification = new Classification();
		Category category = new Category();
		category.level = 0;
		category.value = "Life cycle models";
		classification.categories.add(category);
		Models.classifications(model).add(classification);

		StringWriter writer = new StringWriter();
		XmlBinder binder = new XmlBinder();
		binder.toWriter(model, writer);
		// JAXB.marshal(model, writer);
		System.out.println(writer.toString());
	}

}
