package org.openlca.jsonld;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

class HtmlSerializer {

	public String serialize(RootEntity entity, IDatabase database) {
		if (entity == null)
			return null;
		String json = Document.toJson(entity, database);
		String template = getTemplate(entity);
		if (template == null)
			return null;
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(template);
		Map<String, Object> content = new HashMap<>();
		content.put("model", entity);
		content.put("json", json);
		StringWriter writer = new StringWriter();
		mustache.execute(writer, content);
		writer.flush();
		return writer.toString();
	}

	private String getTemplate(RootEntity entity) {
		if (entity instanceof Actor)
			return "actor.template.html";
		if (entity instanceof Source)
			return "source.template.html";
		if (entity instanceof UnitGroup)
			return "unit_group.template.html";
		if (entity instanceof FlowProperty)
			return "flow_property.template.html";
		if (entity instanceof Flow)
			return "flow.template.html";
		if (entity instanceof Process)
			return "process.template.html";
		else
			return null;
	}
}
