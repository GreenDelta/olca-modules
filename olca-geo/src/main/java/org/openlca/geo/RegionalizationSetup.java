package org.openlca.geo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.ImpactMethod.ParameterMean;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.geo.kml.IKmlLoader;
import org.openlca.geo.kml.KmlLoader;
import org.openlca.geo.kml.LocationKml;
import org.openlca.geo.parameter.ParameterCalculator;
import org.openlca.geo.parameter.ParameterSet;
import org.openlca.geo.parameter.ShapeFileFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionalizationSetup {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IKmlLoader kmlLoader;
	private final ParameterMean parameterMean;

	final IDatabase database;
	final ImpactMethodDescriptor method;

	public boolean canCalculate;
	public List<LocationKml> kmlData;
	public ParameterSet parameterSet;

	/**
	 * Initializes the resources for regionalized LCIA calculation. The field
	 * <code>canCalculate</code> is false if a regionalized calculation cannot
	 * be done. The respective error message is logged the in this case.
	 */
	public static RegionalizationSetup create(IDatabase db,
			ImpactMethodDescriptor method, TechIndex index) {
		return create(db, method, index, new KmlLoader(db));
	}

	/**
	 * Initializes the resources for regionalized LCIA calculation. The field
	 * <code>canCalculate</code> is false if a regionalized calculation cannot
	 * be done. The respective error message is logged the in this case.
	 */
	public static RegionalizationSetup create(IDatabase db,
			ImpactMethodDescriptor method, TechIndex index, IKmlLoader kmlLoader) {
		RegionalizationSetup setup = new RegionalizationSetup(db, method, kmlLoader);
		if (db == null || method == null || index == null) {
			setup.canCalculate = false;
			return setup;
		}
		try {
			setup.init(index);
		} catch (Exception e) {
			setup.canCalculate = false;
			setup.log.error("failed to create regionalization setup", e);
		}
		return setup;
	}

	private RegionalizationSetup(IDatabase database,
			ImpactMethodDescriptor method, IKmlLoader kmlLoader) {
		this.database = database;
		this.method = method;
		this.kmlLoader = kmlLoader;
		this.parameterMean = parameterMean();
	}

	private void init(TechIndex index) {
		canCalculate = true;
		List<Parameter> params = getShapeFileParameters();
		if (params.isEmpty()) {
			log.warn("Cannot calculate regionalized LCIA because there is "
					+ "no LCIA method with shapefile parameters selected.");
			canCalculate = false;
			return;
		}
		kmlData = kmlLoader.load(index);
		if (kmlData.isEmpty()) {
			log.warn("Cannot calculate regionalized LCIA because none of the "
					+ "processes in the product system contains a KML feature.");
			canCalculate = false;
			return;
		}
		ShapeFileFolder folder = getShapeFileFolder();
		if (!shapeFilesExist(folder, params)) {
			canCalculate = false;
			return;
		}
		try (ParameterCalculator pCalc = new ParameterCalculator(
				params, folder, parameterMean)) {
			parameterSet = pCalc.calculate(kmlData);
		}
	}

	private List<Parameter> getShapeFileParameters() {
		String query = "select m.parameters from ImpactMethod m where "
				+ "m.id = :methodId";
		ParameterDao dao = new ParameterDao(database);
		List<Parameter> all = dao.getAll(query,
				Collections.singletonMap("methodId", method.getId()));
		List<Parameter> params = new ArrayList<>();
		for (Parameter param : all) {
			if (param == null || param.getExternalSource() == null)
				continue;
			if (!"SHAPE_FILE".equals(param.getSourceType()))
				continue;
			params.add(param);
		}
		return params;
	}

	private ShapeFileFolder getShapeFileFolder() {
		File dir = new FileStore(database).getFolder(method);
		if (dir == null || !dir.exists()) {
			log.warn("Cannot calculate regionalized LCIA because no shapefiles "
					+ "where found (location for shapefiles is {})", dir);
			canCalculate = false;
			return null;
		}
		return new ShapeFileFolder(dir);
	}

	private boolean shapeFilesExist(ShapeFileFolder folder, List<Parameter> params) {
		if (folder == null)
			return false;
		List<String> shapeFiles = folder.getShapeFiles();
		for (Parameter parameter : params) {
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

	private ParameterMean parameterMean() {
		if (method == null || database == null)
			return null;
		try {
			String sql = "select parameter_mean from tbl_impact_methods"
					+ " where id = " + method.getId();
			AtomicReference<String> ref = new AtomicReference<String>(null);
			NativeSql.on(database).query(sql, r -> {
				String s = r.getString(1);
				ref.set(s);
				return false; // only one result
			});
			String val = ref.get();
			return val == null
					? ParameterMean.WEIGHTED_MEAN
					: ParameterMean.valueOf(val);
		} catch (Exception e) {
			log.error("failed to load ParameterMean function for "
					+ method, e);
			return ParameterMean.WEIGHTED_MEAN;
		}
	}

}
