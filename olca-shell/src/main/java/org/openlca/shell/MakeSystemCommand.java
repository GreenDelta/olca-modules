package org.openlca.shell;

import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.IProductSystemBuilder;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MakeSystemCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	public void exec(Shell shell, String[] args) {
		if (args.length < 1) {
			log.error("a process ID is expected");
			return;
		}
		IDatabase database = shell.getDatabase();
		if (database == null) {
			log.error("no database connection");
			return;
		}
		try {
			long processId = Long.parseLong(args[0]);
			run(processId, database);
		} catch (Exception e) {
			log.error("failed to create a product "
					+ "system for process with ID = " + args[0], e);
		}
	}

	private void run(long processId, IDatabase database) {
		log.trace("create a product system for process {}", processId);
		ProductSystem system = createSystem(processId, database);
		log.trace("auto-complete new product system {}", system);
		IProductSystemBuilder builder = IProductSystemBuilder.Factory.create(
				database, null, false);
		builder.autoComplete(system);
		log.trace("new system created");
	}

	private ProductSystem createSystem(long processId, IDatabase database) {
		Process process = database.createDao(Process.class).getForId(processId);
		Exchange refExchange = process.getQuantitativeReference();
		ProductSystem system = new ProductSystem();
		system.setDescription("generated from shell");
		system.setName(process.getName() + " - system");
		system.setReferenceExchange(refExchange);
		system.setReferenceProcess(process);
		system.setRefId(UUID.randomUUID().toString());
		system.setTargetAmount(1d);
		system.setTargetFlowPropertyFactor(refExchange.getFlowPropertyFactor());
		system.setTargetUnit(refExchange.getUnit());
		database.createDao(ProductSystem.class).insert(system);
		return system;
	}

}
