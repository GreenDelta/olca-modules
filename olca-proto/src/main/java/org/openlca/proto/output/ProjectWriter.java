package org.openlca.proto.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.Project;
import org.openlca.core.model.Version;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

public class ProjectWriter {

  private final WriterConfig config;

  public ProjectWriter(WriterConfig config) {
    this.config = config;
  }

  public Proto.Project write(Project project) {
    var proto = Proto.Project.newBuilder();
    if (project == null)
      return proto.build();

    // root entity fields
    proto.setType("Project");
    proto.setId(Strings.orEmpty(project.refId));
    proto.setName(Strings.orEmpty(project.name));
    proto.setDescription(Strings.orEmpty(project.description));
    proto.setVersion(Version.asString(project.version));
    if (project.lastChange != 0L) {
      var instant = Instant.ofEpochMilli(project.lastChange);
      proto.setLastChange(instant.toString());
    }

    // categorized entity fields
    if (Strings.notEmpty(project.tags)) {
      Arrays.stream(project.tags.split(","))
        .filter(Strings::notEmpty)
        .forEach(proto::addTags);
    }
    if (project.category != null) {
      proto.setCategory(Out.refOf(project.category, config));
    }

    // model specific fields
    // TODO

    return proto.build();
  }
}
