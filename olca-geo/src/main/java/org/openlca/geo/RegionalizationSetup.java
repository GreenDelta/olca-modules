package org.openlca.geo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.geo.kml.IKmlLoader;
import org.openlca.geo.kml.KmlFeature;
import org.openlca.geo.parameter.ParameterRepository;
import org.openlca.geo.parameter.ParameterSet;
import org.openlca.geo.parameter.ParameterSetBuilder;
import org.openlca.geo.parameter.ShapeFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionalizationSetup {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase database;
	private final ImpactMethodDescriptor impactMethod;

	private Map<LongPair, KmlFeature> features;
	private List<Parameter> shapeFileParameters;
	private ShapeFileRepository shapeFileRepository;
	private ParameterRepository parameterRepository;
	private ParameterSet parameterSet;

	public RegionalizationSetup(IDatabase database,
			ImpactMethodDescriptor impactMethod) {
		this.database = database;
		this.impactMethod = impactMethod;
	}

	/**
	 * Initializes the resources for regionalized LCIA calculation. Returns
	 * false if a regionalized calculation cannot be done and logs the
	 * respective problem in this case.
	 */
	public boolean init(IKmlLoader kmlLoader, ProductIndex productIndex) {
		shapeFileParameters = getShapeFileParameters();
		if (shapeFileParameters.isEmpty()) {
			log.warn("Cannot calculate regionalized LCIA because there is "
					+ "no LCIA method with shapefile parameters selected.");
			return false;
		}
		features = kmlLoader.load(productIndex);
		if (features.isEmpty()) {
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
			if (param.getExternalSource() == null)
				continue;
			if (!"SHAPE_FILE".equals(param.getSourceType()))
				continue;
			shapeFileParams.add(param);
		}
		return shapeFileParams;
	}

	private boolean initRepositories() {
		File shapeFileDir = new File(database.getFileStorageLocation(),
				"shapefiles");
		File methodDir = new File(shapeFileDir, impactMethod.getRefId());
		if (!methodDir.exists()) {
			log.warn("Cannot calculate regionalized LCIA because no shapefiles "
					+ "where found (location for shapefiles is "
					+ "<database file location>/shapefiles/<method uuid>");
			return false;
		}
		shapeFileRepository = new ShapeFileRepository(methodDir);
		parameterRepository = new ParameterRepository(shapeFileRepository);
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
		ParameterSetBuilder builder = ParameterSetBuilder.createBuilder(
				shapeFileParameters, shapeFileRepository, parameterRepository);
		HashSet<KmlFeature> unique = new HashSet<KmlFeature>();
		unique.addAll(features.values());
		parameterSet = builder.build(unique);
	}

	public Map<LongPair, KmlFeature> getFeatures() {
		return features;
	}

	public ParameterSet getParameterSet() {
		return parameterSet;
	}

	public ImpactMethodDescriptor getImpactMethod() {
		return impactMethod;
	}

}
