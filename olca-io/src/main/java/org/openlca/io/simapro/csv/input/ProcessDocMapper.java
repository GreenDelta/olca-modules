package org.openlca.io.simapro.csv.input;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.simapro.csv.model.enums.ValueEnum;
import org.openlca.simapro.csv.model.process.LiteratureReferenceRow;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessDocMapper {

	private RefData refData;

	private ProcessBlock block;
	private Process process;

	public ProcessDocMapper(IDatabase database, RefData refData) {
		this.refData = refData;
	}

	public void map(ProcessBlock block, Process process) {
		this.block = block;
		this.process = process;
		mapSources();
		mapDefaults();
		mapDescription();
	}

	private void mapSources() {
		for (LiteratureReferenceRow row : block.getLiteratureReferences()) {
			Source source = refData.getSource(row.getName());
			if (source == null)
				continue;
			process.getDocumentation().getSources().add(source);
		}
	}

	private void mapDefaults() {
		ProcessDocumentation doc = new ProcessDocumentation();
		process.setDocumentation(doc);
		mapTime(doc);
		if (block.getTechnology() != null)
			doc.setTechnology(block.getTechnology().getValue());
		if (block.getInfrastructure() != null)
			process.setInfrastructureProcess(block.getInfrastructure());
		doc.setDataTreatment(block.getDataTreatment());
		doc.setSampling(block.getCollectionMethod());
		doc.setReviewDetails(block.getVerification());
		doc.setInventoryMethod(block.getAllocationRules());
	}

	private void mapTime(ProcessDocumentation doc) {
		if (block.getTime() == null)
			return;
		String text = block.getTime().getValue();
		Pattern pattern = Pattern.compile("(\\d{4})-(\\d{4})");
		Matcher m = pattern.matcher(text);
		if (!m.matches()) {
			doc.setTime(text);
			return;
		}
		try {
			int startYear = Integer.parseInt(m.group(1));
			Calendar c = Calendar.getInstance();
			c.set(startYear, 0, 1, 0, 0);
			doc.setValidFrom(c.getTime());
			int endYear = Integer.parseInt(m.group(2));
			c.set(endYear, 11, 31, 0, 0);
			doc.setValidUntil(c.getTime());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to convert time", e);
		}
	}

	private void mapDescription() {
		StringBuilder builder = new StringBuilder();
		if (block.getComment() != null)
			builder.append(block.getComment());
		append("Status", block.getStatus(), builder);
		append("Representativeness", block.getRepresentativeness(), builder);
		append("Cut off rules", block.getCutoff(), builder);
		append("Multiple output allocation", block.getAllocation(), builder);
		append("Substitution allocation", block.getSubstitution(), builder);
		append("Capital goods", block.getCapitalGoods(), builder);
		append("Boundary with nature", block.getBoundaryWithNature(), builder);
		append("Record", block.getRecord(), builder);
		append("Generator", block.getGenerator(), builder);
		process.setDescription(builder.toString());
	}

	private void append(String label, String value, StringBuilder builder) {
		if (value == null)
			return;
		builder.append(label).append(": ").append(value).append("\n");
	}

	private void append(String label, ValueEnum venum, StringBuilder builder) {
		if (venum == null)
			return;
		builder.append(label).append(": ").append(venum.getValue())
				.append("\n");
	}

}
