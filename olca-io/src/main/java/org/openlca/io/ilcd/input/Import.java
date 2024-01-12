package org.openlca.io.ilcd.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.io.ImportLog;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.model.RootEntity;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.Processes;
import org.openlca.io.ilcd.input.models.ModelImport;
import org.openlca.io.maps.FlowSync;
import org.openlca.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Import implements org.openlca.io.Import {

	private final DataStore store;
	private final IDatabase db;
	private final FlowSync flowSync;
	private final ImportLog log;

	private boolean allFlows;
	private boolean withGabiGraphs = false;
	private String[] langOrder = {"en"};
	private ExchangeProviderQueue providers;

	private volatile boolean canceled = false;

	final ImportCache cache;

	private Import(DataStore store, IDatabase db, FlowMap flowMap) {
		this.store = Objects.requireNonNull(store);
		this.db = Objects.requireNonNull(db);
		log = new ImportLog();
		flowSync = flowMap == null
				? FlowSync.of(db, FlowMap.empty())
				: FlowSync.of(db, flowMap);
		flowSync.withLog(log);
		cache = new ImportCache(this);
	}

	public static Import of(DataStore store, IDatabase db) {
		return new Import(store, db, null);
	}

	public static Import of(DataStore store, IDatabase db, FlowMap flowMap) {
		return new Import(store, db, flowMap);
	}

	public Import withAllFlows(boolean b) {
		allFlows = b;
		return this;
	}

	/**
	 * Set if Gabi graphs are supported in eILCD model imports or not. Gabi has
	 * some specific model features: processes can be connected by different flows
	 * (e.g. a material can be connected with a transport flow); the same process
	 * can occur with different scaling factors in the same graph; processes can
	 * be connected by arbitrary flow types (not only wastes and products), and
	 * more. When Gabi graph support is enabled and an eILCD model of unknown
	 * origin is imported, copies of the processes in the system are created and
	 * linked in order to make it computable in openLCA.
	 */
	public Import withGabiGraphSupport(boolean b) {
		withGabiGraphs = b;
		return this;
	}

	public boolean hasGabiGraphSupport() {
		return withGabiGraphs;
	}

	/**
	 * Define the order in which a multi-language string should be evaluated. It
	 * first checks if there is a string for the first language of this list, then
	 * the second, etc.
	 */
	public Import withLanguageOrder(String... codes) {
		if (codes != null && codes.length > 0) {
			var filtered = Arrays.stream(codes)
					.filter(Strings::notEmpty)
					.map(s -> s.trim().toLowerCase())
					.toArray(String[]::new);
			if (filtered.length > 0) {
				langOrder = filtered;
			}
		}
		return this;
	}

	@Override
	public void cancel() {
		this.canceled = true;
		log().info("cancel import");
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public ImportLog log() {
		return log;
	}

	public DataStore store() {
		return store;
	}

	public IDatabase db() {
		return db;
	}

	public boolean withAllFlows() {
		return allFlows;
	}

	String[] langOrder() {
		return langOrder;
	}

	public FlowSync flowSync() {
		return flowSync;
	}

	public ExchangeProviderQueue providers() {
		if (providers == null) {
			providers = ExchangeProviderQueue.create(db);
		}
		return providers;
	}

	@Override
	public void run() {
		if (canceled)
			return;
		importAll(Contact.class);
		importAll(Source.class);
		importAll(UnitGroup.class);
		importAll(FlowProperty.class);
		if (allFlows) {
			importAll(Flow.class);
		}
		importAll(Process.class);
		importAll(ImpactMethod.class);
		importAll(Model.class);
	}

	private <T extends IDataSet> void importAll(Class<T> type) {
		if (canceled)
			return;
		try {
			var it = store.iterator(type);
			while (!canceled && it.hasNext()) {
				importOf(it.next());
			}
		} catch (Exception e) {
			log.error("Import of data of type "
					+ type.getSimpleName() + " failed", e);
		}
	}

	private <T extends IDataSet> void importOf(T dataSet) {
		if (dataSet == null)
			return;
		try {
			if (dataSet instanceof Contact contact) {
				new ContactImport(this).run(contact);
			} else if (dataSet instanceof Source source) {
				new SourceImport(this).run(source);
			} else if (dataSet instanceof UnitGroup group) {
				new UnitGroupImport(this).run(group);
			} else if (dataSet instanceof FlowProperty prop) {
				new FlowPropertyImport(this).run(prop);
			} else if (dataSet instanceof Flow flow) {
				new FlowImport(this).run(flow);
			} else if (dataSet instanceof Process process) {
				var method = Processes.getInventoryMethod(process);
				if (method != null && method.processType == ProcessType.EPD) {
					new EpdImport(this, process).run();
				} else {
					new ProcessImport(this).run(process);
				}
			} else if (dataSet instanceof ImpactMethod impact) {
				new ImpactImport(this, impact).run();
			} else if (dataSet instanceof Model model) {
				new ModelImport(this).run(model);
			} else {
				log.error("No matching import for data set " + dataSet + " available");
			}
		} catch (Exception e) {
			log.error("Import of " + dataSet + " failed", e);
		}
	}

	String str(List<LangString> list) {
		return LangString.getFirst(list, langOrder);
	}

	<T extends RootEntity> T insert(T e) {
		var r = db.insert(e);
		log.imported(r);
		return r;
	}

}
