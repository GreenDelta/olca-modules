package org.openlca.proto.output;

import org.openlca.core.model.Project;
import org.openlca.proto.generated.EntityType;
import org.openlca.proto.generated.Proto;

public class ProjectWriter {

  private final WriterConfig config;

  public ProjectWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Project write(Project project) {
    var proto = Proto.Project.newBuilder();
    if (project == null)
      return proto.build();
    proto.setEntityType(EntityType.Project);
    Out.map(project, proto);
    Out.dep(config, project.category);

    // model specific fields
    // TODO

    return proto.build();
  }
}
