package org.openlca.io.simapro.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a set of processes to a SimaPro CSV file.
 */
public class ProcessWriter {

	private final IDatabase db;

	private Map<Unit, SimaProUnit> units = new HashMap<>();

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
			writeQuantities(buffer);
			writeReferenceFlows(buffer);
			writeGlobalParameters(buffer);
		} catch (Exception e) {
			throw e instanceof RuntimeException
					? (RuntimeException) e
					: new RuntimeException(e);
		}
	}

	private void writeQuantities(BufferedWriter w) {

		// we always write at least the kilogram data
		// into the quantity sections
		SimaProUnit kg = SimaProUnit.kg;

		// quantities
		Set<String> quantities = units.values().stream()
				.map(u -> u.quantity)
				.collect(Collectors.toSet());
		r(w, "Quantities");
		if (!quantities.contains(kg.quantity)) {
			r(w, kg.quantity, "Yes");
		}
		for (String q : quantities) {
			r(w, q, "Yes");
		}
		r(w, "");
		r(w, "End");
		r(w, "");
		r(w, "");

		// units
		Set<SimaProUnit> us = new HashSet<>(units.values());
		r(w, "Units");
		if (!us.contains(kg)) {
			r(w, kg.symbol,
					kg.quantity,
					Double.toString(kg.factor),
					kg.refUnit);
		}
		for (SimaProUnit u : us) {
			r(w, u.symbol,
					u.quantity,
					Double.toString(u.factor),
					u.refUnit);
		}
		r(w, "");
		r(w, "End");
		r(w, "");
		r(w, "");
	}

	private void writeReferenceFlows(BufferedWriter w) {
		for (ElementaryFlowType type : ElementaryFlowType.values()) {
			r(w, type.getReferenceHeader());
			r(w, "");
			r(w, "End");
			r(w, "");
			r(w, "");
		}
	}

	private void writeGlobalParameters(BufferedWriter w) {
		String [] sections = {
				"Database Input parameters",
				"Database Calculated parameters",
				"Project Input parameters",
				"Project Calculated parameters",
		};
		for (String s : sections) {
			r(w, s);
			r(w, "");
			r(w, "End");
			r(w, "");
		}
	}

	private void writeProcess(BufferedWriter w, Process p) {
		writeProcessDoc(w, p);

		r(w, "Products");
		for (Exchange e : p.exchanges) {
			if (!isProductOutput(e))
				continue;
			r(w, unsep(e.flow.name),
					unit(e),
					Double.toString(e.amount),
					"100",
					"not defined",
					category(e.flow),
					"");
		}
		r(w, "");

		String[] sections = {
				"Avoided products",
				"Resources",
				"Materials/fuels",
				"Electricity/heat",
				"Emissions to air",
				"Emissions to water",
				"Emissions to soil",
				"Final waste flows",
				"Non material emissions",
				"Social issues",
				"Economic issues",
				"Waste to treatment",
				"Input parameters",
				"Calculated parameters",
		};

		for (String s : sections) {
			r(w, s);
			r(w, "");
		}

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

	private boolean isProductOutput(Exchange e) {
		if (e == null || e.flow == null || e.isAvoided)
			return false;
		FlowType ft = e.flow.flowType;
		return (ft == FlowType.PRODUCT_FLOW && !e.isInput)
				|| (ft == FlowType.WASTE_FLOW && e.isInput);
	}

	private String unit(Exchange e) {
		if (e == null || e.unit == null)
			return SimaProUnit.kg.symbol;
		SimaProUnit unit = units.get(e.unit);
		if (unit != null)
			return unit.symbol;
		unit = SimaProUnit.find(e.unit.name);
		if (unit != null) {
			units.put(e.unit, unit);
			return unit.symbol;
		}
		if (e.unit.synonyms != null) {
			for (String syn : e.unit.synonyms.split(";")) {
				unit = SimaProUnit.find(syn);
				if (unit != null) {
					units.put(e.unit, unit);
					return unit.symbol;
				}
			}
		}
		Logger log = LoggerFactory.getLogger(getClass());
		log.warn("No corresponding SimaPro unit" +
				" for '{}' found; fall back to 'kg'", e.unit.name);
		units.put(e.unit, SimaProUnit.kg);
		return SimaProUnit.kg.symbol;
	}

	private String category(CategorizedEntity e) {
		if (e == null)
			return "";
		StringBuilder path = null;
		Category c = e.category;
		while (c != null) {
			if (path == null) {
				path = new StringBuilder(c.name);
			} else {
				path.insert(0, c.name + '\\');
			}
			c = c.category;
		}
		return path == null ? "" : path.toString();
	}

	private void r(BufferedWriter w, String... s) {
		String row = s.length == 1
				? s[0]
				: String.join(";", s);
		try {
			w.write(row);
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
		String dbPath = "C:\\Users\\Win10\\openLCA-data-1.4\\databases\\database_to_convert";
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
