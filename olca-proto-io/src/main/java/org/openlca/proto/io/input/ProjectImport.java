package org.openlca.proto.io.input;

import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.Project;
import org.openlca.proto.ProtoProject;

class ProjectImport implements Import<Project> {

  private final ProtoImport imp;

  ProjectImport(ProtoImport imp) {
    this.imp = imp;
  }

  @Override
  public ImportStatus<Project> of(String id) {
    var project = imp.get(Project.class, id);

    // check if we are in update mode
    var update = false;
    if (project != null) {
      update = imp.shouldUpdate(project);
      if (!update) {
        return ImportStatus.skipped(project);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getProject(id);
    if (proto == null)
      return project != null
        ? ImportStatus.skipped(project)
        : ImportStatus.error("Could not resolve Project " + id);

    var wrap = ProtoWrap.of(proto);
    if (update) {
      if (imp.skipUpdate(project, wrap))
        return ImportStatus.skipped(project);
    }

    // map the data
    if (project == null) {
      project = new Project();
      project.refId = id;
    }
    wrap.mapTo(project, imp);
    map(proto, project);

    // insert or update it
    var dao = new ProjectDao(imp.db);
    project = update
      ? dao.update(project)
      : dao.insert(project);
    imp.putHandled(project);
    return update
      ? ImportStatus.updated(project)
      : ImportStatus.created(project);
  }

  private void map(ProtoProject proto, Project project) {
    // TODO: map project data
  }
}
