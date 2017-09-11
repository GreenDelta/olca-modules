package org.openlca.ilcd.io;

import java.io.StringWriter;
import java.util.UUID;

import org.junit.Test;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.models.Connection;
import org.openlca.ilcd.models.DownstreamLink;
import org.openlca.ilcd.models.Group;
import org.openlca.ilcd.models.GroupRef;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.Parameter;
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.models.Technology;
import org.openlca.ilcd.util.Models;

public class ModelIOTest {

	@Test
	public void testSimpleModel() throws Exception {
		Model model = new Model();
		Models.dataSetInfo(model).uuid = UUID.randomUUID().toString();
		Models.modelName(model).name.add(LangString.of("Example model", "en"));
		Models.publication(model).version = "01.00.000";

		Classification classification = new Classification();
		Category category = new Category();
		category.level = 0;
		category.value = "Life cycle models";
		classification.categories.add(category);
		Models.classifications(model).add(classification);

		Models.quantitativeReference(model).refProcess = 42;

		Technology tech = Models.technology(model);
		Group group = new Group();
		group.id = 42;
		group.name.add(LangString.of("Use phase", "en"));
		tech.groups.add(group);

		ProcessInstance pi = new ProcessInstance();
		tech.processes.add(pi);
		GroupRef groupRef = new GroupRef();
		groupRef.groupID = 42;
		pi.groupRefs.add(groupRef);

		Parameter param = new Parameter();
		param.name = "distance";
		param.value = 42.42;
		pi.parameters.add(param);

		Connection con = new Connection();
		con.outputFlow = UUID.randomUUID().toString();
		DownstreamLink link = new DownstreamLink();
		link.inputFlow = UUID.randomUUID().toString();
		link.process = 42;
		con.downstreamLinks.add(link);
		pi.connections.add(con);

		StringWriter writer = new StringWriter();
		XmlBinder binder = new XmlBinder();
		binder.toWriter(model, writer);
		// JAXB.marshal(model, writer);
		System.out.println(writer.toString());
	}

}
