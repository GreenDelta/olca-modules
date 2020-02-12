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
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
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
			ProcessDao dao = new ProcessDao(db);
			for (ProcessDescriptor p : processes) {
				Process process = dao.getForId(p.id);
				writeProcess(buffer, process);
			}
		} catch (Exception e) {
			throw e instanceof RuntimeException
					? (RuntimeException) e
					: new RuntimeException(e);
		}
	}

	private void writeProcess(BufferedWriter w, Process p) {
		writeProcessDoc(w, p);

		r(w, "End");
		r(w, "");
	}

	private void writeProcessDoc(BufferedWriter w, Process p) {
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
		r(w, p.processType == ProcessType.UNIT_PROCESS
				? "Unit process"
				: "System");
		r(w, "");

		r(w, "Process name");
		r(w, unsep(p.name));
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

		r(w, "Infrastructure");
		r(w, "No");
		r(w, "");

		r(w, "Date");
		r(w, new SimpleDateFormat("dd.MM.yyyy")
				.format(new Date()));
		r(w, "");

		// we keep the following sections empty
		String[] eSections = {
				"Record",
				"Generator",
				"External documents",
				"Literature references",
				"Collection method",
				"Data treatment",
				"Verification",
				"Comment",
				"Allocation rules",
		};
		for (String s : eSections) {
			r(w, s);
			r(w, "");
			r(w, "");
		}

		r(w, "System description");
		r(w, ";");
		r(w, "");
	}

	public void writerHeader(BufferedWriter w) {
		r(w, "{SimaPro 8.5.0.0}");
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
		r(w, "{CSV Format version: 8.0.5}");
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

	private String unsep(String s) {
		if (s == null)
			return "";
		return s.replace(';', ',');
	}

	public static void main(String[] args) {
		String dbPath = "C:/Users/Win10/Downloads/sp/Database_to_convert";
		String target = "C:/Users/Win10/Downloads/sp/OUT.CSV";
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
