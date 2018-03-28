package org.openlca.io.ecospold1.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.ecospold.IAllocation;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpoldIO;
import org.openlca.io.FileImport;
import org.openlca.io.ImportEvent;
import org.openlca.io.ImportInfo;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

/**
 * Parses EcoSpold01 xml files and creates openLCA objects and inserts them into
 * the database
 */
public class EcoSpold01Import implements FileImport {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private Category processCategory;
	private HashMap<Integer, Exchange> localExchangeCache = new HashMap<>();
	private DB db;
	private FlowImport flowImport;
	private EventBus eventBus;
	private boolean canceled = false;
	private File[] files;

	private final ImportInfo.Collector infos = new ImportInfo.Collector();

	public EcoSpold01Import(ImportConfig config) {
		this.db = new DB(config.db);
		this.flowImport = new FlowImport(db, config);
	}

	public List<ImportInfo> getInfos() {
		return infos.get();
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/** Set an optional root category for the new processes. */
	public void setProcessCategory(Category processCategory) {
		this.processCategory = processCategory;
	}

	/**
	 * Runs the import with a set of files (use the respective constructor of the
	 * setter method for the files).
	 */
	public void run() {
		if (files == null || files.length == 0)
			return;
		for (File file : files) {
			if (canceled)
				break;
			if (file.isDirectory())
				continue;
			String fileName = file.getName().toLowerCase();
			if (fileName.endsWith(".xml"))
				importXml(file);
			else if (fileName.endsWith(".zip"))
				importZip(file);
			else
				log.warn("unexpected file for import {}", file);
		}
	}

	private void importXml(File file) {
		try {
			DataSetType type = EcoSpoldIO.getEcoSpoldType(file);
			FileInputStream in = new FileInputStream(file);
			fireEvent(file.getName());
			run(in, type);
		} catch (Exception e) {
			log.error("failed to import XML file " + file, e);
		}
	}

	private void importZip(File file) {
		try (ZipFile zipFile = new ZipFile(file)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements() && !canceled) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory())
					continue;
				String name = entry.getName().toLowerCase();
				if (!name.endsWith(".xml"))
					continue;
				fireEvent(name);
				DataSetType type = EcoSpoldIO.getEcoSpoldType(zipFile
						.getInputStream(entry));
				run(zipFile.getInputStream(entry), type);
			}
		} catch (Exception e) {
			log.error("failed to import ZIP file " + file, e);
		}
	}

	private void fireEvent(String dataSetName) {
		log.trace("import data set {}", dataSetName);
		if (eventBus == null)
			return;
		eventBus.post(new ImportEvent(dataSetName));
	}

	public void run(InputStream is, DataSetType type) throws Exception {
		if (is == null || type == null)
			return;
		IEcoSpold spold = EcoSpoldIO.readFrom(is, type);
		if (spold == null || spold.getDataset().isEmpty())
			return;
		if (type == DataSetType.PROCESS) {
			for (IDataSet ds : spold.getDataset()) {
				DataSet dataSet = new DataSet(ds, type.getFactory());
				handleProcessDataSet(dataSet);
			}
		} else if (!spold.getDataset().isEmpty()) {
			impactMethod(spold);
		}
	}

	private void handleProcessDataSet(DataSet ds) {
		if (ds.getReferenceFunction() == null)
			return;
		persons(ds);
		sources(ds);
		locations(ds);
		process(ds);
	}

	private void persons(DataSet ds) {
		for (IPerson person : ds.getPersons()) {
			String id = ES1KeyGen.forPerson(person);
			Actor actor = db.findActor(person, id);
			if (actor != null) {
				infos.ignored(actor);
				continue;
			}
			actor = new Actor();
			actor.setRefId(id);
			Mapper.mapPerson(person, actor);
			db.put(actor, id);
			infos.imported(actor);
		}
	}

	private void sources(DataSet ds) {
		for (ISource eSource : ds.getSources()) {
			String id = ES1KeyGen.forSource(eSource);
			Source oSource = db.findSource(eSource, id);
			if (oSource != null) {
				infos.ignored(oSource);
				continue;
			}
			oSource = new Source();
			oSource.setRefId(id);
			Mapper.mapSource(eSource, oSource);
			db.put(oSource, id);
			infos.imported(oSource);
		}
	}

	private void locations(DataSet ds) {
		Set<String> codes = new HashSet<>();
		IGeography geo = ds.getGeography();
		if (geo != null && geo.getLocation() != null) {
			codes.add(geo.getLocation());
		}
		for (IExchange e : ds.getExchanges()) {
			if (e.getLocation() != null) {
				codes.add(e.getLocation());
			}
		}
		for (String code : codes) {
			String id = KeyGen.get(code);
			Location loc = db.findLocation(code, id);
			if (loc != null) {
				infos.ignored(loc);
				continue;
			}
			loc = new Location();
			loc.setRefId(id);
			loc.setName(code);
			loc.setCode(code);
			db.put(loc, id);
			infos.imported(loc);
		}
	}

	private void process(DataSet ds) {
		String id = ES1KeyGen.forProcess(ds);
		Process p = db.get(Process.class, id);
		if (p != null) {
			log.trace("Process {} already exists, not imported", id);
			infos.ignored(p);
			return;
		}

		p = new Process();
		p.setRefId(id);
		ProcessDocumentation doc = new ProcessDocumentation();
		p.setDocumentation(doc);

		IReferenceFunction refFun = ds.getReferenceFunction();
		if (refFun != null)
			mapReferenceFunction(refFun, p, doc);

		p.setProcessType(Mapper.getProcessType(ds));
		mapTimeAndGeography(ds, p, doc);

		if (ds.getTechnology() != null
				&& ds.getTechnology().getText() != null) {
			doc.setTechnology(Strings.cut(
					(ds.getTechnology().getText()), 65500));
		}

		mapExchanges(ds.getExchanges(), p);
		if (p.getQuantitativeReference() == null)
			createProductFromRefFun(ds, p);

		if (ds.getAllocations() != null
				&& ds.getAllocations().size() > 0) {
			mapAllocations(p, ds.getAllocations());
			p.setDefaultAllocationMethod(AllocationMethod.CAUSAL);
		}

		Mapper.mapModellingAndValidation(ds, doc);
		Mapper.mapAdminInfo(ds, p);
		mapActors(doc, ds);
		mapSources(doc, ds);

		db.put(p, id);
		localExchangeCache.clear();
		infos.imported(p);
	}

	private void mapTimeAndGeography(DataSet ds, Process p,
			ProcessDocumentation doc) {
		ProcessTime time = new ProcessTime(ds.getTimePeriod());
		time.map(doc);
		if (ds.getGeography() != null) {
			String locationCode = ds.getGeography().getLocation();
			if (locationCode != null) {
				String genKey = KeyGen.get(locationCode);
				p.setLocation(db.findLocation(locationCode, genKey));
			}
			doc.setGeography(ds.getGeography().getText());
		}
	}

	private void mapActors(ProcessDocumentation doc, DataSet ds) {
		Map<Integer, Actor> actors = new HashMap<>();
		for (IPerson person : ds.getPersons()) {
			Actor actor = db.findActor(person, ES1KeyGen.forPerson(person));
			if (actor != null)
				actors.put(person.getNumber(), actor);
		}
		if (ds.getDataGeneratorAndPublication() != null)
			doc.setDataGenerator(actors.get(ds
					.getDataGeneratorAndPublication().getPerson()));
		if (ds.getValidation() != null)
			doc.setReviewer(actors.get(ds.getValidation()
					.getProofReadingValidator()));
		if (ds.getDataEntryBy() != null)
			doc.setDataDocumentor(actors.get(ds.getDataEntryBy()
					.getPerson()));
	}

	private void mapAllocations(Process process,
			List<IAllocation> allocations) {
		for (IAllocation allocation : allocations) {
			double factor = Math.round(allocation.getFraction() * 10000d)
					/ 1000000d;
			Exchange product = localExchangeCache.get(allocation
					.getReferenceToCoProduct());
			for (Integer i : allocation.getReferenceToInputOutput()) {
				Exchange e = localExchangeCache.get(i);
				if (e == null) {
					log.warn("allocation factor points to an exchange that "
							+ "does not exist: {}", i);
					continue;
				}
				AllocationFactor af = new AllocationFactor();
				af.setProductId(product.flow.getId());
				af.setValue(factor);
				af.setAllocationType(AllocationMethod.CAUSAL);
				af.setExchange(e);
				process.getAllocationFactors().add(af);
			}
		}
	}

	private void mapExchanges(List<IExchange> inExchanges, Process ioProcess) {
		for (IExchange inExchange : inExchanges) {
			FlowBucket flow = flowImport.handleProcessExchange(inExchange);
			if (flow == null || !flow.isValid()) {
				log.error("Could not import flow {}", inExchange);
				continue;
			}
			Exchange outExchange = ioProcess.exchange(flow.flow, flow.flowProperty, flow.unit);
			outExchange.isInput = inExchange.getInputGroup() != null;
			ExchangeAmount exchangeAmount = new ExchangeAmount(outExchange, inExchange);
			outExchange.description = inExchange.getGeneralComment();
			exchangeAmount.map(flow.conversionFactor);
			localExchangeCache.put(inExchange.getNumber(), outExchange);
			if (ioProcess.getQuantitativeReference() == null && inExchange.getOutputGroup() != null
					&& (inExchange.getOutputGroup() == 0 || inExchange.getOutputGroup() == 2)) {
				ioProcess.setQuantitativeReference(outExchange);
			}
		}
	}

	private void mapFactors(List<IExchange> inFactors,
			ImpactCategory ioCategory) {
		for (IExchange inFactor : inFactors) {
			FlowBucket flow = flowImport.handleImpactFactor(inFactor);
			if (flow == null || !flow.isValid()) {
				log.error("Could not import flow {}", inFactor);
				continue;
			}
			ImpactFactor factor = new ImpactFactor();
			factor.flow = flow.flow;
			factor.flowPropertyFactor = flow.flow.getFactor(flow.flowProperty);
			factor.unit = flow.unit;
			factor.value = flow.conversionFactor * inFactor.getMeanValue();
			ioCategory.impactFactors.add(factor);
		}
	}

	private ImpactCategory mapReferenceFunction(
			IReferenceFunction inRefFunction) {
		ImpactCategory category = new ImpactCategory();
		category.setRefId(UUID.randomUUID().toString());
		String name = inRefFunction.getSubCategory();
		if (inRefFunction.getName() != null) {
			name = name.concat(" - ").concat(inRefFunction.getName());
		}
		category.setName(name);
		category.referenceUnit = inRefFunction.getUnit();
		return category;
	}

	private void mapReferenceFunction(IReferenceFunction refFun,
			Process ioProcess, ProcessDocumentation doc) {
		ioProcess.setName(refFun.getName());
		ioProcess.setDescription(refFun.getGeneralComment());
		ioProcess.setInfrastructureProcess(refFun.isInfrastructureProcess());
		String topCategory = refFun.getCategory();
		String subCategory = refFun.getSubCategory();
		Category cat = null;
		if (processCategory != null)
			cat = db.getPutCategory(processCategory, topCategory, subCategory);
		else
			cat = db.getPutCategory(ModelType.PROCESS, topCategory,
					subCategory);
		ioProcess.setCategory(cat);
	}

	private void createProductFromRefFun(DataSet dataSet, Process ioProcess) {
		FlowBucket flow = flowImport.handleProcessProduct(dataSet);
		if (flow == null || !flow.isValid()) {
			log.warn("Could not create reference flow {}", dataSet);
			return;
		}
		Exchange outExchange = ioProcess.exchange(flow.flow, flow.flowProperty, flow.unit);
		outExchange.isInput = false;
		double amount = dataSet.getReferenceFunction().getAmount()
				* flow.conversionFactor;
		outExchange.amount = amount;
		ioProcess.setQuantitativeReference(outExchange);
	}

	private void mapSources(ProcessDocumentation doc, DataSet adapter) {
		Map<Integer, Source> sources = new HashMap<>();
		for (ISource source : adapter.getSources()) {
			Source s = db.findSource(source, ES1KeyGen.forSource(source));
			if (s != null) {
				sources.put(source.getNumber(), s);
				doc.getSources().add(s);
			}
		}
		if (adapter.getDataGeneratorAndPublication() != null
				&& adapter.getDataGeneratorAndPublication()
						.getReferenceToPublishedSource() != null)
			doc.setPublication(sources.get(adapter
					.getDataGeneratorAndPublication()
					.getReferenceToPublishedSource()));
	}

	private void impactMethod(IEcoSpold es) {
		if (es.getDataset().isEmpty())
			return;
		DataSet dataSet = new DataSet(es.getDataset().get(0),
				DataSetType.IMPACT_METHOD.getFactory());
		String methodId = ES1KeyGen.forImpactMethod(dataSet);
		ImpactMethod method = db.get(ImpactMethod.class, methodId);
		if (method != null) {
			log.trace("LCIA method {} already exists, not imported", methodId);
			infos.ignored(method);
			return;
		}

		method = new ImpactMethod();
		method.setRefId(methodId);
		method.setName(dataSet.getReferenceFunction().getCategory());
		method.setDescription(dataSet.getReferenceFunction()
				.getGeneralComment());
		for (IDataSet adapter : es.getDataset()) {
			dataSet = new DataSet(adapter,
					DataSetType.IMPACT_METHOD.getFactory());
			ImpactCategory lciaCategory = mapReferenceFunction(dataSet
					.getReferenceFunction());
			mapFactors(dataSet.getExchanges(), lciaCategory);
			method.impactCategories.add(lciaCategory);
		}
		db.put(method, methodId);
		infos.imported(method);
	}

}
