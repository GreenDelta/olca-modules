package org.openlca.proto.input;

import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.Project;
import org.openlca.proto.Proto;

public class ProjectImport {

  private final ProtoImport imp;

  public ProjectImport(ProtoImport imp) {
    this.imp = imp;
  }

  public Project of(String id) {
    if (id == null)
      return null;
    var project = imp.get(Project.class, id);

    // check if we are in update mode
    var update = false;
    if (project != null) {
      update = imp.shouldUpdate(project);
      if(!update) {
        return project;
      }
    }

    // check the proto object
    var proto = imp.store.getProject(id);
    if (proto == null)
      return project;
    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(project, wrap))
        return project;
    }

    // map the data
    if (project == null) {
      project = new Project();
      project.refId = id;
    }
    wrap.mapTo(project, imp);
    map(proto, project);

    // insert it
    var dao = new ProjectDao(imp.db);
    project = update
      ? dao.update(project)
      : dao.insert(project);
    imp.putHandled(project);
    return project;
  }

  private void map(Proto.Project proto, Project project) {
  }
}
