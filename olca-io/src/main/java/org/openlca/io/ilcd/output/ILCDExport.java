package org.openlca.io.ilcd.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.io.DataStore;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The entry point for the ILCD export of model components.
 */
public class ILCDExport {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	final IDatabase db;
	final DataStore store;
	private String lang = "en";

	public ILCDExport(IDatabase db, DataStore store) {
		this.db = db;
		this.store = store;
	}

	/**
	 * Set the language code for multi-language strings of the export.
	 */
	public ILCDExport withLang(String lang) {
		if (Strings.notEmpty(lang)) {
			this.lang = lang;
		}
		return this;
	}

	public void write(RootEntity e) {
		if (e == null)
			return;
		try {
			if (e instanceof ImpactMethod method) {
				new ImpactMethodExport(this).run(method);
			} else if (e instanceof ProductSystem system) {
				new SystemExport(this).run(system);
			} else if (e instanceof Process process) {
				new ProcessExport(this).run(process);
			} else if (e instanceof Flow flow) {
				new FlowExport(this).run(flow);
			} else if (e instanceof FlowProperty prop) {
				new FlowPropertyExport(this).run(prop);
			} else if (e instanceof UnitGroup group) {
				new UnitGroupExport(this).run(group);
			} else if (e instanceof Actor actor) {
				new ActorExport(this).run(actor);
			} else if (e instanceof Source source) {
				new SourceExport(this).run(source);
			} else {
				log.error("cannot convert type to ILCD: {}", e);
			}
		} catch (Exception ex) {
			log.error("export of " + e + " failed", ex);
		}
	}

	/**
	 * Adds the given value to the given list of language strings using the
	 * default language code of the export. It only adds the string when the given
	 * value is a non-empty string.
	 */
	void add(List<LangString> list, String value) {
		if (value == null || value.isEmpty())
			return;
		LangString.set(list, value, lang);
	}
}
