package org.openlca.io.ilcd.output;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.*;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.DataStore;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry point for the ILCD export of model components.
 */
public class Export {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	final IDatabase db;
	final DataStore store;
	private String lang = "en";

	public Export(IDatabase db, DataStore store) {
		this.db = db;
		this.store = store;
	}

	/**
	 * Set the language code for multi-language strings of the export.
	 */
	public Export withLang(String lang) {
		if (Strings.notEmpty(lang)) {
			this.lang = lang;
		}
		return this;
	}

	public void write(RootEntity e) {
		if (e == null)
			return;
		try {
			if (e instanceof Epd epd) {
				new EpdExport(this).write(epd);
			} else if (e instanceof ImpactMethod method) {
				new ImpactMethodExport(this).write(method);
			} else if (e instanceof ImpactCategory impact) {
				new ImpactCategoryExport(this).write(impact);
			} else if (e instanceof ProductSystem system) {
				new SystemExport(this).write(system);
			} else if (e instanceof Process process) {
				new ProcessExport(this).write(process);
			} else if (e instanceof Flow flow) {
				new FlowExport(this).write(flow);
			} else if (e instanceof FlowProperty prop) {
				new FlowPropertyExport(this).run(prop);
			} else if (e instanceof UnitGroup group) {
				new UnitGroupExport(this).write(group);
			} else if (e instanceof Actor actor) {
				new ActorExport(this).write(actor);
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
	 * Writes the entity and returns a reference to it. Returns {@code null}
	 * when the entity is {@code null}.
	 */
	Ref writeRef(RootEntity e) {
		if (e == null)
			return null;
		write(e);
		return refOf(e);
	}

	/**
	 * Adds the given value to the given list of language strings using the
	 * default language code of the export. It only adds the string when the given
	 * value is a non-empty string.
	 */
	void add(Supplier<List<LangString>> str, String value) {
		if (value == null || value.isEmpty())
			return;
		str.get().add(LangString.of(value, lang));
	}

	private Ref refOf(RootEntity e) {
		if (e == null) {
			return new Ref();
		}
		var type = refTypeOf(e);
		var ref = new Ref()
				.withVersion(Version.asString(e.version))
				.withType(type)
				.withUUID(e.refId)
				.withUri("../" + pathOf(type) + "/" + e.refId + ".xml");
		add(ref::withName, e.name);
		return ref;
	}

	private DataSetType refTypeOf(RootEntity e) {
		if (e instanceof Actor)
			return DataSetType.CONTACT;
		if (e instanceof Source)
			return DataSetType.SOURCE;
		if (e instanceof UnitGroup)
			return DataSetType.UNIT_GROUP;
		if (e instanceof FlowProperty)
			return DataSetType.FLOW_PROPERTY;
		if (e instanceof Flow)
			return DataSetType.FLOW;
		if (e instanceof ImpactCategory || e instanceof ImpactMethod)
			return DataSetType.IMPACT_METHOD;
		if (e instanceof Process || e instanceof Epd)
			return DataSetType.PROCESS;
		if (e instanceof ProductSystem)
			return DataSetType.MODEL;
		return null;
	}

	private static String pathOf(DataSetType type) {
		if (type == null)
			return "?";
		return switch (type) {
			case CONTACT -> "contacts";
			case SOURCE -> "sources";
			case UNIT_GROUP -> "unitgroups";
			case FLOW_PROPERTY -> "flowproperties";
			case FLOW -> "flows";
			case PROCESS -> "processes";
			case MODEL -> "lifecyclemodels";
			case IMPACT_METHOD -> "lciamethods";
			case EXTERNAL_FILE -> "external_docs";
		};
	}

	static Integer getYear(Date date) {
		if (date == null)
			return null;
		var cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}
}
