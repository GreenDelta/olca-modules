
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Project;
import org.openlca.proto.ProtoProject;

public record ProjectReader(EntityResolver resolver)
	implements EntityReader<Project, ProtoProject> {

	@Override
	public Project read(ProtoProject proto) {
		var project = new Project();
		update(project, proto);
		return project;
	}

	@Override
	public void update(Project project, ProtoProject proto) {
		Util.mapBase(project, ProtoWrap.of(proto), resolver);

	}
}
