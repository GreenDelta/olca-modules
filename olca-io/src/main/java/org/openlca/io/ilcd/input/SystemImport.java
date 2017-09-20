package org.openlca.io.ilcd.input;

import java.util.List;
import java.util.Objects;

import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.models.Connection;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ModelName;
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.models.QuantitativeReference;
import org.openlca.ilcd.models.Technology;
import org.openlca.ilcd.util.ClassList;
import org.openlca.ilcd.util.Models;
import org.openlca.util.Strings;

public class SystemImport {

	private final ImportConfig config;
	private ProductSystem system;

	public SystemImport(ImportConfig config) {
		this.config = config;
	}

	public ProductSystem run(Model model) throws ImportException {
		if (model == null)
			return null;
		try {
			ProductSystemDao dao = new ProductSystemDao(config.db);
			system = dao.getForRefId(model.getUUID());
			if (system != null)
				return system;
			system = new ProductSystem();
			system.setRefId(model.getUUID());
			mapMetaData(model);
			mapModel(model);
			return dao.insert(system);
		} catch (Exception e) {
			throw new ImportException("Failed to get/create product system", e);
		}
	}

	private void mapMetaData(Model model) throws ImportException {
		system.setName(getName(model));
		CategoryImport categoryImport = new CategoryImport(config,
				ModelType.PRODUCT_SYSTEM);
		Category category = categoryImport.run(ClassList.sortedList(model));
		system.setCategory(category);
	}

	@SuppressWarnings("unchecked")
	private String getName(Model m) {
		ModelName mn = Models.getModelName(m);
		if (mn == null)
			return "";
		List<?>[] parts = new List<?>[] { mn.name, mn.technicalDetails,
				mn.mixAndLocation, mn.flowProperties };
		String name = "";
		for (List<?> part : parts) {
			String s = LangString.getFirst((List<LangString>) part,
					config.langs);
			if (Strings.nullOrEmpty(s))
				continue;
			if (name.length() > 0)
				name += "; ";
			name += s.trim();
		}
		return name;
	}

	private void mapModel(Model m) throws ImportException {
		Technology tech = Models.getTechnology(m);
		if (tech == null)
			return;
		QuantitativeReference qRef = Models.getQuantitativeReference(m);
		int refProcess = -1;
		if (qRef != null && qRef.refProcess != null)
			refProcess = qRef.refProcess.intValue();
		for (ProcessInstance pi : tech.processes) {
			if (pi.process == null)
				continue;
			ProcessImport pImport = new ProcessImport(config);
			Process p = pImport.run(pi.process.uuid);
			if (refProcess == pi.id) {
				mapRefProcess(pi, p);
				system.getProcesses().add(p.getId());
			}
			// TODO: links
		}
	}

	private void mapRefProcess(ProcessInstance pi, Process process) {
		if (pi == null || process == null)
			return;
		system.setReferenceProcess(process);
		Exchange e = findRefExchange(pi, process);
		if (e == null)
			return;
		system.setReferenceExchange(e);
		system.setTargetAmount(e.amount);
		system.setTargetFlowPropertyFactor(e.flowPropertyFactor);
		system.setTargetUnit(e.unit);
	}

	private Exchange findRefExchange(ProcessInstance pi, Process process) {
		for (Connection con : pi.connections) {
			String flowID = con.outputFlow;
			if (flowID == null)
				continue;
			for (Exchange e : process.getExchanges()) {
				if (e.flow == null)
					continue;
				if (Objects.equals(flowID, e.flow.getRefId()))
					return e;
			}
		}
		return null;
	}

}
