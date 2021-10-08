package org.openlca.proto.io.output;

import org.openlca.core.model.Project;
import org.openlca.proto.ProtoProject;
import org.openlca.proto.ProtoType;

public class ProjectWriter {

  private final WriterConfig config;

  public ProjectWriter(WriterConfig config) {
    this.config = config;
  }

  public ProtoProject write(Project project) {
    var proto = ProtoProject.newBuilder();
    if (project == null)
      return proto.build();
    proto.setType(ProtoType.Project);
    Out.map(project, proto);
    Out.dep(config, project.category);

    // model specific fields
    // TODO

    return proto.build();
  }
}
