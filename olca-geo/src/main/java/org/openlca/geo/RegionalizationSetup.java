package org.openlca.geo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.geo.kml.LocationKml;
import org.openlca.geo.kml.KmlLoader;
import org.openlca.geo.parameter.ParameterCalculator;
import org.openlca.geo.parameter.ParameterCache;
import org.openlca.geo.parameter.ParameterSet;
import org.openlca.geo.parameter.ShapeFileFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionalizationSetup {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase database;
	private final ImpactMethodDescriptor impactMethod;

	private List<LocationKml> kmlData;
	private File shapeFileDir;
	private List<Parameter> shapeFileParameters;
	private ShapeFileFolder shapeFileRepository;
	private ParameterCache parameterRepository;
	private ParameterSet parameterSet;

	public RegionalizationSetup(IDatabase database,
			ImpactMethodDescriptor impactMethod, File shapeFileDir) {
		this.database = database;
		this.impactMethod = impactMethod;
		this.shapeFileDir = shapeFileDir;
	}

	/**
	 * Initializes the resources for regionalized LCIA calculation. Returns
	 * false if a regionalized calculation cannot be done and logs the
	 * respective problem in this case.
	 */
	public boolean init(KmlLoader kmlLoader, ProductIndex productIndex) {
		shapeFileParameters = getShapeFileParameters();
		if (shapeFileParameters.isEmpty()) {
			log.warn("Cannot calculate regionalized LCIA because there is "
					+ "no LCIA method with shapefile parameters selected.");
			return false;
		}
		kmlData = kmlLoader.load(productIndex);
		if (kmlData.isEmpty()) {
			log.warn("Cannot calculate regionalized LCIA because none of the "
					+ "processes in the product system contains a KML feature.");
			return false;
		}
		if (!initRepositories())
			return false;
		if (!shapeFilesExist())
			return false;
		initParameterSet();
		return true;
	}

	private List<Parameter> getShapeFileParameters() {
		if (impactMethod == null)
			return Collections.emptyList();
		long methodId = impactMethod.getId();
		String query = "select m.parameters from ImpactMethod m where "
				+ "m.id = :methodId";
		ParameterDao dao = new ParameterDao(database);
		List<Parameter> allParams = dao.getAll(query,
				Collections.singletonMap("methodId", methodId));
		List<Parameter> shapeFileParams = new ArrayList<>();
		for (Parameter param : allParams) {
			if (param == null)
				continue;
			if (param.getExternalSource() == null)
				continue;
			if (!"SHAPE_FILE".equals(param.getSourceType()))
				continue;
			shapeFileParams.add(param);
		}
		return shapeFileParams;
	}

	private boolean initRepositories() {
		if (!shapeFileDir.exists()) {
			log.warn("Cannot calculate regionalized LCIA because no shapefiles "
					+ "where found (location for shapefiles is "
					+ shapeFileDir.getAbsolutePath());
			return false;
		}
		shapeFileRepository = new ShapeFileFolder(shapeFileDir);
		parameterRepository = new ParameterCache(shapeFileRepository);
		return true;
	}

	private boolean shapeFilesExist() {
		if (shapeFileRepository == null)
			return false;
		List<String> shapeFiles = shapeFileRepository.getShapeFiles();
		for (Parameter parameter : shapeFileParameters) {
			String shapefile = parameter.getExternalSource();
			if (shapeFiles.contains(shapefile))
				continue;
			log.error("Cannot calculate regionalized LCIA because "
					+ "shapefile {} referenced from parameter "
					+ "{} does not exist", shapefile, parameter);
			return false;
		}
		return true;
	}

	private void initParameterSet() {
		ParameterCalculator calculator = new ParameterCalculator(
				shapeFileParameters, shapeFileRepository, parameterRepository);
		parameterSet = calculator.calculate(kmlData);
	}

	public List<LocationKml> getKmlData() {
		return kmlData;
	}

	public ParameterSet getParameterSet() {
		return parameterSet;
	}

	public ImpactMethodDescriptor getImpactMethod() {
		return impactMethod;
	}

}
