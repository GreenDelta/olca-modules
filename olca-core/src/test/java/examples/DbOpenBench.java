package examples;

import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.core.model.Process;

public class DbOpenBench {

	public static void main(String[] args) {

		var dirs = DataDir.get().getDatabasesDir().listFiles();
		if (dirs == null) {
			System.out.println("no databases retrieved");
			return;
		}

		var text = new StringBuilder(
				"Java: " + System.getProperty("java.version") + "\n" +
						"| Database | Version | Processes | Time [s] |\n");
		for (var dir : dirs) {
			var start = System.nanoTime();
			var db = new Derby(dir);
			int v = db.getVersion();
			var pcount = db.getDescriptors(Process.class).size();
			db.close();
			var time = ((double) (System.nanoTime() - start)) / 1e9;
			text.append(String.format(
					"| %s | %d | %d | %.2f |%n", dir.getName(), v, pcount, time));
		}
		System.out.println(text);

	}

}
