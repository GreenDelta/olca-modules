package org.openlca.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shell {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public void setDatabase(IDatabase database) {
		closeDatabase();
		this.database = database;
	}

	public IDatabase getDatabase() {
		return database;
	}

	public void run() {
		System.out.println("Welcome to the openLCA Shell");
		System.out.print("olca> ");

		InputStreamReader in = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(in);
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				executeLine(line);
				System.out.println();
				System.out.print("olca> ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void executeLine(String line) {
		String[] command = line.trim().split("\\s+");
		if (command.length > 0 && command[0] != null)
			eval(command, line.trim());
	}

	private void eval(String[] fullCommand, String rawLine) {
		String command = fullCommand[0].trim().toLowerCase();
		if (command.isEmpty())
			return;
		log.trace("execute command {}", command);
		String[] args = parseArgs(fullCommand);
		switch (command) {

		case "exit":
			exit();
			break;

		case "quit":
			exit();
			break;

		case "mysql":
			new MySqlCommand().exec(this, args);
			break;

		case "derby":
			new DerbyCommand().exec(this, args);
			break;

		case "load":
			loadFile(args);
			break;

		case "sql":
			new SqlCommand().exec(this, rawLine.substring(4));
			break;

		case "import":
			new ImportCommand().exec(this, args);
			break;

		default:
			log.info("unknown command ", command);
			break;
		}
	}

	private String[] parseArgs(String[] fullCommand) {
		List<String> argList = new ArrayList<String>();
		for (int i = 1; i < fullCommand.length; i++) {
			String s = fullCommand[i];
			if (s == null)
				continue;
			s = s.trim();
			if (s.length() > 1 && s.startsWith("\"") && s.endsWith("\""))
				s = s.substring(1, s.length() - 1);
			if (s.isEmpty())
				continue;
			argList.add(s);
		}
		String[] args = argList.toArray(new String[argList.size()]);
		return args;
	}

	private void exit() {
		closeDatabase();
		System.out.println();
		System.out.println("Bye!");
		System.exit(0);
	}

	private void closeDatabase() {
		if (database == null)
			return;
		log.info("close database");
		try {
			database.close();
		} catch (Exception e) {
			log.error("failed to closed database", e);
		}
	}

	private void loadFile(String[] args) {
		if (args.length == 0 || args[0] == null) {
			log.error("no file path is given");
			return;
		}
		String path = args[0];
		log.info("load file {}", path);
		try {
			File file = new File(path);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<String> commandLines = new ArrayList<>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String command = line.trim();
				if (command.isEmpty() || command.startsWith("#"))
					continue;
				commandLines.add(command);
			}
			for (String commandLine : commandLines)
				executeLine(commandLine);
			reader.close();
		} catch (Exception e) {
			log.error("failed to execute commands from file", e);
		}
	}

	public static void main(String[] args) {
		new Shell().run();
	}

}
