package org.openlca.shell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MakeProjectCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	public void exec(Shell shell, String[] args) {
		if (args.length < 2) {
			log.error("a name and a list of product system IDs are required");
			return;
		}
		IDatabase database = shell.getDatabase();
		if (database == null) {
			log.error("no database connection");
			return;
		}
		try {
			createProject(args, database);
		} catch (Exception e) {
			log.error("failed to create project", e);
		}
	}

	private void createProject(String[] args, IDatabase database) {
		log.trace("create project {}", args[0]);
		Project project = new Project();
		project.setName(args[0]);
		List<Long> systemIds = fetchSystemIds(args[1]);
		ProductSystemDao dao = new ProductSystemDao(database);
		for (Long id : systemIds) {
			ProductSystem system = dao.getForId(id);
			if (system == null)
				continue;
			ProjectVariant variant = new ProjectVariant();
			variant.setName(system.getName());
			variant.setProductSystem(system);
			project.getVariants().add(variant);
		}
		database.createDao(Project.class).insert(project);
		log.trace("project created");
	}

	private List<Long> fetchSystemIds(String string) {
		if (string == null)
			return Collections.emptyList();
		try {
			List<Long> list = new ArrayList<>();
			for (String no : string.split("#")) {
				long val = Long.parseLong(no);
				list.add(val);
			}
			return list;
		} catch (Exception e) {
			log.error("{} is not a valid list of product system IDs", string);
			return Collections.emptyList();
		}
	}
}
