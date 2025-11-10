package org.openlca.io.xls.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.model.Process;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.store.EntityStore;
import org.openlca.util.Dirs;

public class XlsProcessWriter {

	private final EntityStore db;

	private XlsProcessWriter(EntityStore db) {
		this.db = db;
	}

	public static XlsProcessWriter of(EntityStore db) {
		return new XlsProcessWriter(db);
	}

	public void write(ProcessDescriptor d, File file) {
		if (d == null || file == null)
			return;
		var process = db.get(Process.class, d.id);
		if (process == null)
			return;
		write(process, file);
	}

	public void write(Process process, File file) {
		if (process == null || file == null)
			return;
		try (var wb = new XSSFWorkbook();
				 var out = new FileOutputStream(file)) {
			var writer = new OutConfig(wb, db, process);
			writer.write();
			wb.write(out);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to write process " + process + " to file " + file, e);
		}
	}

	public void writeAllToFolder(
		Iterable<ProcessDescriptor> processes, File dir) {
		if (processes == null || dir == null)
			return;
		Dirs.createIfAbsent(dir);
		var usedNames = new HashSet<String>();
		for (var p : processes) {

			var process = p.id > 0
				? db.get(Process.class, p.id)
				: db.get(Process.class, p.refId);
			if (process == null)
				continue;

			// try to find a friendly file name
			String name = null;
			if (p.name != null) {
				name = p.name.replaceAll("\\W+", "_").toLowerCase();
				if (name.length() > 50) {
					name = name.substring(0, 50);
				}
				if (usedNames.contains(name)) {
					name = null;
				} else {
					usedNames.add(name);
				}
			}
			// fall back to an ID based name
			if (name == null) {
				var version = new Version(p.version).toString();
				name = p.refId + "_" + version ;
			}

			var file = new File(dir, name + ".xlsx");
			write(process, file);
		}
	}
}
