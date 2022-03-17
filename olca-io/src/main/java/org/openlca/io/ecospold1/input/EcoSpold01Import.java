package org.openlca.io.ecospold1.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openlca.core.io.ImportLog;
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
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpold;
import org.openlca.io.Import;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

/**
 * Parses EcoSpold01 xml files and creates openLCA objects and inserts them into
 * the database
 */
public class EcoSpold01Import implements Import {

	private Category processCategory;
	private final HashMap<Integer, Exchange> localExchangeCache = new HashMap<>();
	private final DB db;
	private final FlowImport flowImport;
	private boolean canceled = false;
	private File[] files;
	private final ImportLog log = new ImportLog();

	public EcoSpold01Import(ImportConfig config) {
		this.db = new DB(config.db);
		this.flowImport = new FlowImport(db, config);
	}

	@Override
	public ImportLog log() {
		return log;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Set an optional root category for the new processes.
	 */
	public void setProcessCategory(Category processCategory) {
		this.processCategory = processCategory;
	}

	/**
	 * Runs the import with a set of files (use the respective constructor of
	 * the setter method for the files).
	 */
	@Override
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
				log.warn("unexpected file for import: " + file);
		}
	}

	private void importXml(File file) {
		var type = EcoSpold.typeOf(file);
		if (type.isEmpty()) {
			log.warn("could not detect ecoSpold type of: " + file);
			return;
		}
		try (var stream = new FileInputStream(file)) {
			log.info("import file " + file.getName());
			run(stream, type.get());
		} catch (Exception e) {
			log.error("failed to import XML file " + file, e);
		}
	}

	private void importZip(File file) {
		try (var zip = new ZipFile(file)) {
			var entries = zip.entries();
			while (entries.hasMoreElements() && !canceled) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory())
					continue;
				String name = entry.getName().toLowerCase();
				if (!name.endsWith(".xml"))
					continue;
				log.info("import file: " + name);
				var type = EcoSpold.typeOf(zip.getInputStream(entry));
				if (type.isEmpty())
					continue;
				run(zip.getInputStream(entry), type.get());
			}
		} catch (Exception e) {
			log.error("failed to import ZIP file " + file, e);
		}
	}

	public void run(InputStream is, DataSetType type) {
		if (is == null || type == null)
			return;
		var spold = EcoSpold.read(is, type);
		if (spold == null || spold.getDataset().isEmpty())
			return;
		if (type == DataSetType.IMPACT_METHOD) {
			importImpacts(spold);
		} else {
			for (var ds : spold.getDataset()) {
				var wrap = new DataSet(ds, type.getFactory());
				importProcess(wrap);
			}
		}
	}

	private void importProcess(DataSet ds) {
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
				log.skipped(actor);
				continue;
			}
			actor = new Actor();
			actor.refId = id;
			Mapper.mapPerson(person, actor);
			db.put(actor, id);
			log.imported(actor);
		}
	}

	private void sources(DataSet ds) {
		for (ISource eSource : ds.getSources()) {
			String id = ES1KeyGen.forSource(eSource);
			Source oSource = db.findSource(eSource, id);
			if (oSource != null) {
				log.skipped(oSource);
				continue;
			}
			oSource = new Source();
			oSource.refId = id;
			Mapper.mapSource(eSource, oSource);
			db.put(oSource, id);
			log.imported(oSource);
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
				log.skipped(loc);
				continue;
			}
			loc = new Location();
			loc.refId = id;
			loc.name = code;
			loc.code = code;
			db.put(loc, id);
			log.imported(loc);
		}
	}

	private void process(DataSet ds) {
		String id = ES1KeyGen.forProcess(ds);
		Process p = db.get(Process.class, id);
		if (p != null) {
			log.skipped(p);
			return;
		}

		p = new Process();
		p.refId = id;
		ProcessDocumentation doc = new ProcessDocumentation();
		p.documentation = doc;

		IReferenceFunction refFun = ds.getReferenceFunction();
		if (refFun != null)
			mapReferenceFunction(refFun, p);

		p.processType = Mapper.getProcessType(ds);
		mapTimeAndGeography(ds, p, doc);

		if (ds.getTechnology() != null
			&& ds.getTechnology().getText() != null) {
			doc.technology = Strings.cut(
				(ds.getTechnology().getText()), 65500);
		}

		mapExchanges(ds.getExchanges(), p);
		if (p.quantitativeReference == null)
			createProductFromRefFun(ds, p);

		if (ds.getAllocations() != null
			&& ds.getAllocations().size() > 0) {
			mapAllocations(p, ds.getAllocations());
			p.defaultAllocationMethod = AllocationMethod.CAUSAL;
		}

		Mapper.mapModellingAndValidation(ds, doc);
		Mapper.mapAdminInfo(ds, p);
		mapActors(doc, ds);
		mapSources(doc, ds);

		db.put(p, id);
		localExchangeCache.clear();
		log.imported(p);
	}

	private void mapTimeAndGeography(DataSet ds, Process p,
																	 ProcessDocumentation doc) {
		ProcessTime time = new ProcessTime(ds.getTimePeriod());
		time.map(doc);
		if (ds.getGeography() != null) {
			String locationCode = ds.getGeography().getLocation();
			if (locationCode != null) {
				String genKey = KeyGen.get(locationCode);
				p.location = db.findLocation(locationCode, genKey);
			}
			doc.geography = ds.getGeography().getText();
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
			doc.dataGenerator = actors.get(ds
				.getDataGeneratorAndPublication().getPerson());
		if (ds.getValidation() != null)
			doc.reviewer = actors.get(ds.getValidation()
				.getProofReadingValidator());
		if (ds.getDataEntryBy() != null)
			doc.dataDocumentor = actors.get(ds.getDataEntryBy()
				.getPerson());
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
						+ "does not exist: " + i);
					continue;
				}
				AllocationFactor af = new AllocationFactor();
				af.productId = product.flow.id;
				af.value = factor;
				af.method = AllocationMethod.CAUSAL;
				af.exchange = e;
				process.allocationFactors.add(af);
			}
		}
	}

	private void mapExchanges(List<IExchange> inExchanges, Process ioProcess) {
		for (IExchange inExchange : inExchanges) {
			FlowBucket flow = flowImport.handleProcessExchange(inExchange);
			if (flow == null || !flow.isValid()) {
				log.error("Could not import flow: " + inExchange);
				continue;
			}
			Exchange outExchange = ioProcess.add(Exchange.of(flow.flow,
				flow.flowProperty, flow.unit));
			outExchange.isInput = inExchange.getInputGroup() != null;
			ExchangeAmount exchangeAmount = new ExchangeAmount(outExchange,
				inExchange);
			outExchange.description = inExchange.getGeneralComment();
			exchangeAmount.map(flow.conversionFactor);
			localExchangeCache.put(inExchange.getNumber(), outExchange);
			if (ioProcess.quantitativeReference == null
				&& inExchange.getOutputGroup() != null
				&& (inExchange.getOutputGroup() == 0
				|| inExchange.getOutputGroup() == 2)) {
				ioProcess.quantitativeReference = outExchange;
			}
		}
	}

	private void mapFactors(List<IExchange> inFactors,
													ImpactCategory ioCategory) {
		for (IExchange inFactor : inFactors) {
			FlowBucket flow = flowImport.handleImpactFactor(inFactor);
			if (flow == null || !flow.isValid()) {
				log.error("Could not import flow: " + inFactor);
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

	private void mapReferenceFunction(IReferenceFunction refFun, Process ioProcess) {
		ioProcess.name = refFun.getName();
		ioProcess.description = refFun.getGeneralComment();
		ioProcess.infrastructureProcess = refFun.isInfrastructureProcess();
		String topCategory = refFun.getCategory();
		String subCategory = refFun.getSubCategory();
		ioProcess.category = processCategory != null
			? db.getPutCategory(processCategory, topCategory, subCategory)
			: db.getPutCategory(ModelType.PROCESS, topCategory, subCategory);
	}

	private void createProductFromRefFun(DataSet dataSet, Process process) {
		FlowBucket flow = flowImport.handleProcessProduct(dataSet);
		if (flow == null || !flow.isValid()) {
			log.warn("Could not create reference flow: "  + dataSet);
			return;
		}
		var exchange = process.add(
			Exchange.of(flow.flow, flow.flowProperty, flow.unit));
		exchange.isInput = false;
		exchange.amount = dataSet.getReferenceFunction().getAmount()
			* flow.conversionFactor;
		process.quantitativeReference = exchange;
	}

	private void mapSources(ProcessDocumentation doc, DataSet dataSet) {
		Map<Integer, Source> sources = new HashMap<>();
		for (ISource source : dataSet.getSources()) {
			Source s = db.findSource(source, ES1KeyGen.forSource(source));
			if (s != null) {
				sources.put(source.getNumber(), s);
				doc.sources.add(s);
			}
		}
		if (dataSet.getDataGeneratorAndPublication() != null
			&& dataSet.getDataGeneratorAndPublication()
			.getReferenceToPublishedSource() != null)
			doc.publication = sources.get(dataSet
				.getDataGeneratorAndPublication()
				.getReferenceToPublishedSource());
	}

	private void importImpacts(IEcoSpold es) {
		if (es == null)
			return;
		var db = this.db.database;
		for (var ds : es.getDataset()) {
			var wrap = new DataSet(
				ds, DataSetType.IMPACT_METHOD.getFactory());
			var ref = wrap.getReferenceFunction();
			if (ref == null)
				continue;

			// get or create the LCIA category
			var impactID = ES1KeyGen.forImpactCategory(wrap);
			var impact = db.get(ImpactCategory.class, impactID);
			if (impact == null) {
				var name = ref.getSubCategory();
				if (ref.getName() != null) {
					name = name.concat(" - ").concat(ref.getName());
				}
				impact = ImpactCategory.of(name, ref.getUnit());
				impact.refId = impactID;
				mapFactors(wrap.getExchanges(), impact);
				impact = db.insert(impact);
				log.imported(impact);
			}

			// get or create the method
			var methodID = ES1KeyGen.forImpactMethod(wrap);
			var method = db.get(ImpactMethod.class, methodID);
			if (method == null) {
				method = ImpactMethod.of(ref.getCategory());
				method.refId = methodID;
				method.description = ref.getGeneralComment();
				method = db.insert(method);
			}

			var alreadyExists = method.impactCategories.stream()
				.anyMatch(i -> Objects.equals(i.refId, impactID));
			if (alreadyExists)
				continue;
			method.impactCategories.add(impact);
			db.update(method);
		}
	}
}
