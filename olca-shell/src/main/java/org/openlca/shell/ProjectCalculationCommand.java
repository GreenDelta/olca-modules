package org.openlca.shell;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.ProjectCalculator;
import org.openlca.core.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectCalculationCommand {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private long projectId;
	private File exportFile;

	public void solve(Shell shell, String[] args) {
		boolean valid = parseArgs(shell, args);
		if (!valid)
			return;
		try {
			log.trace("calculate project {}", projectId);
			Project project = database.createDao(Project.class).getForId(
					projectId);
			ProjectCalculator calculator = new ProjectCalculator(database);
			calculator.solve(project);
			if (exportFile == null)
				log.trace("no export file defined");
			else {
				// TODO: project result export
			}
			log.trace("calculation done");
		} catch (Exception e) {
			log.error("failed to calculate project", e);
		}
	}

	private boolean parseArgs(Shell shell, String[] args) {
		if (shell.getDatabase() == null) {
			log.error("no database connection");
			return false;
		}
		database = shell.getDatabase();
		if (args.length < 1) {
			log.error("a project ID is expected");
			return false;
		}
		try {
			projectId = Long.parseLong(args[0]);
		} catch (Exception e) {
			log.error("the product system id is not valid");
			return false;
		}
		if (args.length > 1 && args[1] != null)
			exportFile = new File(args[1]);
		return true;
	}

}
