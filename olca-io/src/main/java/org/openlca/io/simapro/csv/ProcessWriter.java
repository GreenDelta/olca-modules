package org.openlca.io.simapro.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Writes a set of processes to a SimaPro CSV file.
 */
public class ProcessWriter {

	private final IDatabase db;
	private BufferedWriter buffer;

	public ProcessWriter(IDatabase db) {
		this.db = db;
	}

	public void write(Collection<ProcessDescriptor> processes, File file) {
		if (processes == null || file == null)
			return;
		try (FileOutputStream fout = new FileOutputStream(file);
			 OutputStreamWriter writer = new OutputStreamWriter(
					 fout, "windows-1252");
			 BufferedWriter buffer = new BufferedWriter(writer)) {
			writerHeader(buffer);
			for (ProcessDescriptor p : processes) {
				writeProcess(buffer, p);
			}
		} catch (Exception e) {
			throw e instanceof RuntimeException
					? (RuntimeException) e
					: new RuntimeException(e);
		}
	}

	private void writeProcess(BufferedWriter w, ProcessDescriptor p) {
		if (p == null)
			return;

		r(w, "Process");
		r(w, "");

		r(w, "Category type");
		r(w, "material");
		r(w, "");

		r(w, "Process identifier");
		r(w, "Standard" + String.format("%015d", p.id));
		r(w, "");

		r(w, "Type");
		r(w, "System");
		r(w, "");

		r(w, "Process name");
		r(w, p.name);
		r(w, "");

		r(w, "Status");
		r(w, "");
		r(w, "");

		// these sections all get an `Unspecified` value
		String[] uSections = {
				"Time period",
				"Geography",
				"Technology",
				"Representativeness",
				"Multiple output allocation",
				"Substitution allocation",
				"Cut off rules",
				"Capital goods",
				"Boundary with nature",
		};
		for (String uSection : uSections) {
			r(w, uSection);
			r(w, "Unspecified");
			r(w, "");
		}



		r(w, "Time period");
		r(w, "Unspecified");
		r(w, "");
	}

	public void writerHeader(BufferedWriter w) {
		r(w, "{SimaPro 8.0}");
		r(w, "{processes}");

		// date
		String date = new SimpleDateFormat("dd.MM.yyyy")
				.format(new Date());
		r(w, "{Date: " + date + "}");

		// time
		String time = new SimpleDateFormat("HH:mm:ss")
				.format(new Date());
		r(w, "{Time: " + time + "}");

		r(w, "{Project: " + db.getName() + "}");
		r(w, "{CSV Format version: 7.0.0}");
		r(w, "{CSV separator: Semicolon}");
		r(w, "{Decimal separator: .}");
		r(w, "{Date separator: .}");
		r(w, "{Short date format: dd.MM.yyyy}");
		r(w, "");
	}

	private void r(BufferedWriter w, String s) {
		try {
			w.write(s);
			w.write("\r\n"); // write Windows line endings
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		String dbPath = "C:/Users/ms/Downloads/Database_to_convert";
		String target = "C:/Users/ms/Downloads/OUT.CSV";
		try {
			IDatabase db = new DerbyDatabase(new File(dbPath));
			ProcessWriter writer = new ProcessWriter(db);
			ProcessDao dao = new ProcessDao(db);
			writer.write(dao.getDescriptors(), new File(target));

			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
