package org.openlca.io.ecospold2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.Compartment;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.Geography;
import org.openlca.io.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports the reference data from a set of EcoSpold 02 files. During the import
 * it creates an index that then can be used in a real process import.
 */
class RefDataImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private CategoryDao categoryDao;
	private RootEntityDao<Location> locationDao;
	private RefDataIndex index;

	public RefDataImport(IDatabase database) {
		this.index = new RefDataIndex();
		this.categoryDao = new CategoryDao(database.getEntityFactory());
		this.locationDao = new RootEntityDao<>(Location.class,
				database.getEntityFactory());
		try {
			loadUnitMaps(database);
		} catch (Exception e) {
			log.error("failed to load unit map", e);
		}
	}

	public RefDataIndex getIndex() {
		return index;
	}

	private void loadUnitMaps(IDatabase database) throws Exception {
		InputStream is = getClass().getResourceAsStream("ei3_unit_map.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] args = line.split(",");
			String eiUnitKey = args[0];
			EntityManagerFactory emf = database.getEntityFactory();
			RootEntityDao<Unit> unitDao = new RootEntityDao<>(Unit.class, emf);
			Unit unit = unitDao.getForRefId(args[1]);
			FlowPropertyDao propDao = new FlowPropertyDao(emf);
			FlowProperty prop = propDao.getForRefId(args[2]);
			if (unit == null || prop == null)
				log.warn("no unit or property found for {} in database, "
						+ "no reference data?", eiUnitKey);
			else {
				index.putUnit(eiUnitKey, unit);
				index.putFlowProperty(eiUnitKey, prop);
			}
		}
	}

	public void importDataSet(DataSet dataSet) {
		if (dataSet == null)
			return;
		try {
			classification(dataSet);
			geography(dataSet);
			for (ElementaryExchange exchange : dataSet.getElementaryExchanges())
				compartment(exchange.getCompartment());
		} catch (Exception e) {
			log.error("failed to import reference data from data set", e);
		}
	}

	private void classification(DataSet dataSet) throws Exception {
		Classification classification = findClassification(dataSet);
		if (classification == null
				|| classification.getClassificationId() == null)
			return;
		String refId = classification.getClassificationId();
		Category category = index.getProcessCategory(refId);
		if (category != null)
			return;
		category = categoryDao.getForRefId(refId);
		if (category == null) {
			category = new Category();
			category.setDescription(classification.getClassificationSystem());
			category.setModelType(ModelType.PROCESS);
			category.setName(classification.getClassificationValue());
			category.setRefId(refId);
			category = categoryDao.insert(category);
		}
		index.putProcessCategory(refId, category);
	}

	private Classification findClassification(DataSet dataSet) {
		for (Classification classification : dataSet.getClassifications()) {
			if (classification.getClassificationSystem() == null)
				continue;
			if (classification.getClassificationSystem().startsWith("ISIC"))
				return classification;
		}
		return null;
	}

	private void geography(DataSet dataSet) throws Exception {
		Geography geography = dataSet.getGeography();
		if (geography == null || geography.getId() == null
				|| geography.getShortName() == null)
			return;
		String refId = geography.getId();
		Location location = index.getLocation(refId);
		if (location != null)
			return;
		String genKey = KeyGen.get(geography.getShortName());
		location = locationDao.getForRefId(genKey);
		if (location == null) {
			location = new Location();
			location.setCode(geography.getShortName());
			location.setName(geography.getShortName());
			location.setDescription("imported via EcoSpold 02 import");
			location.setRefId(genKey);
			location = locationDao.insert(location);
		}
		index.putLocation(refId, location);
	}

	private void compartment(Compartment compartment) {
		if (compartment == null || compartment.getSubcompartmentId() == null)
			return;
		String refId = compartment.getSubcompartmentId();
		Category category = index.getCompartment(refId);
		if (category != null)
			return;

	}

}
