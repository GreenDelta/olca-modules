package org.openlca.io.xls.process.input;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

class FlowSheets {

	private class Factor {
		double factor;
		String property;
		String refUnit;

		Factor(final int row) {
			property = config.getString(factorSheet, row, 2);
			factor = config.getDouble(factorSheet, row, 3);
			refUnit = config.getString(factorSheet, row, 4);
		}
	}

	public static void read(final Config config) {
		new FlowSheets(config).read();
	}

	private final Config config;
	private final FlowDao dao;
	private final HashMap<String, List<Factor>> factors = new HashMap<>();

	private final Sheet factorSheet;

	private final Sheet flowSheet;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private FlowSheets(final Config config) {
		this.config = config;
		this.dao = new FlowDao(config.database);
		this.factorSheet = config.workbook.getSheet("Flow property factors");
		this.flowSheet = config.workbook.getSheet("Flows");
	}

	private boolean addFactors(final Flow flow, final List<Factor> factors) {
		boolean updated = false;
		for (final Factor factor : factors) {
			final FlowProperty property = config.refData
					.getFlowProperty(factor.property);
			if (property == null || flow.getFactor(property) != null) {
				continue;
			}
			final FlowPropertyFactor f = new FlowPropertyFactor();
			f.setFlowProperty(property);
			f.setConversionFactor(factor.factor);
			flow.getFlowPropertyFactors().add(f);
			updated = true;
		}
		return updated;
	}

	private void createFlow(final int row, final String uuid) {
		final String name = config.getString(flowSheet, row, 1);
		final String category = config.getString(flowSheet, row, 3);
		Flow flow = new Flow();
		flow.setRefId(uuid);
		flow.setName(name);
		flow.setCategory(config.getCategory(category, ModelType.FLOW));
		setAttributes(row, flow);
		final List<Factor> factors = this.factors.get(key(name, category));
		if (factors == null) {
			log.error("could not create flow {}/{}; no flow property factors",
					name, category);
			return;
		}
		createPropertyFactors(row, flow, factors);
		flow = dao.insert(flow);
		config.refData.putFlow(name, category, flow);
	}

	private void createPropertyFactors(final int row, final Flow flow,
			final List<Factor> factors) {
		final String refProperty = config.getString(flowSheet, row, 10);
		final RefData refData = config.refData;
		for (final Factor factor : factors) {
			final FlowProperty property = refData
					.getFlowProperty(factor.property);
			if (property == null) {
				log.error("could not find flow property {} of flow property "
						+ "factor ", factor.property);
				continue;
			}
			final FlowPropertyFactor f = new FlowPropertyFactor();
			f.setFlowProperty(property);
			f.setConversionFactor(factor.factor);
			flow.getFlowPropertyFactors().add(f);
			if (Objects.equals(refProperty, property.getName())) {
				flow.setReferenceFlowProperty(property);
			}
		}
	}

	private Location getLocation(final int row) {
		final String code = config.getString(flowSheet, row, 9);
		if (Strings.isNullOrEmpty(code)) {
			return null;
		} else {
			return config.refData.getLocation(code);
		}
	}

	private FlowType getType(final int row) {
		String t = config.getString(flowSheet, row, 6);
		if (Strings.isNullOrEmpty(t)) {
			return FlowType.ELEMENTARY_FLOW;
		}
		t = t.toLowerCase().trim();
		switch (t) {
		case "elementary flow":
			return FlowType.ELEMENTARY_FLOW;
		case "product flow":
			return FlowType.PRODUCT_FLOW;
		case "waste flow":
			return FlowType.WASTE_FLOW;
		default:
			log.warn("unknown flow type {}", t);
			return FlowType.ELEMENTARY_FLOW;
		}
	}

	private String key(final String name, final String category) {
		return category == null ? name : name + category;
	}

	private void read() {
		if (flowSheet == null || factorSheet == null) {
			return;
		}
		try {
			log.trace("import flows");
			readFactors();
			readFlows();
		} catch (final Exception e) {
			log.error("failed to import flows", e);
		}
	}

	private void readFactors() {
		int row = 1;
		while (true) {
			final String name = config.getString(factorSheet, row, 0);
			if (Strings.isNullOrEmpty(name)) {
				break;
			}
			final String category = config.getString(factorSheet, row, 1);
			final String key = key(name, category);
			List<Factor> list = factors.get(key);
			if (list == null) {
				factors.put(key, list = new ArrayList<>());
			}
			final Factor factor = new Factor(row);
			list.add(factor);
			row++;
		}
	}

	private void readFlows() {
		int row = 1;
		while (true) {
			final String uuid = config.getString(flowSheet, row, 0);
			if (Strings.isNullOrEmpty(uuid)) {
				break;
			}
			final Flow flow = dao.getForRefId(uuid);
			if (flow != null) {
				syncFlow(row, flow);
			} else {
				createFlow(row, uuid);
			}
			row++;
		}
	}

	private void setAttributes(final int row, final Flow flow) {
		flow.setDescription(config.getString(flowSheet, row, 2));
		final String version = config.getString(flowSheet, row, 4);
		flow.setVersion(Version.fromString(version).getValue());
		final Date lastChange = config.getDate(flowSheet, row, 5);
		if (lastChange != null) {
			flow.setLastChange(lastChange.getTime());
		}
		flow.setFlowType(getType(row));
		flow.setCasNumber(config.getString(flowSheet, row, 7));
		flow.setFormula(config.getString(flowSheet, row, 8));
		flow.setLocation(getLocation(row));
	}

	private void syncFlow(final int row, Flow flow) {
		final String name = config.getString(flowSheet, row, 1);
		final String category = config.getString(flowSheet, row, 3);
		final List<Factor> factors = this.factors.get(key(name, category));
		if (factors == null || flow.getReferenceFlowProperty() == null) {
			config.refData.putFlow(name, category, flow);
			return;
		}
		final String refProperty = config.getString(flowSheet, row, 10);
		if (!Objects.equals(refProperty, flow.getReferenceFlowProperty()
				.getName())) {
			// cannot add more factors as the reference flow property is not
			// the same
			config.refData.putFlow(name, category, flow);
			return;
		}
		final boolean updated = addFactors(flow, factors);
		if (updated) {
			flow = dao.update(flow);
		}
		config.refData.putFlow(name, category, flow);
	}
}
