package org.openlca.shell;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqlCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String shortUrl;
	private String user = "root";
	private String password = "";

	public void exec(Shell shell, String[] args) {
		if (args.length < 2)
			log.error("too little arguments");
		shortUrl = args[0];
		user = args[1];
		if (args.length > 2)
			password = args[3];
		runIt(shell);
	}

	private void runIt(Shell shell) {
		try {
			log.info("connect to MySQL database {} @ {}", user, shortUrl);
			String url = "jdbc:mysql://" + shortUrl;
			IDatabase database = new MySQLDatabase(url, user, password);
			shell.setDatabase(database);
			log.info("connection established");
		} catch (Exception e) {
			log.error("failed to connect to database", e);
		}
	}

}
