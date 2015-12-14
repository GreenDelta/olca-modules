package org.openlca.core;

import java.io.File;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.core.math.JavaSolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;

public class Tests {

	private static final boolean USE_FILE_BASED_DB = false;

	private static IDatabase db;

	public static IMatrixSolver getDefaultSolver() {
		return new JavaSolver();
	}

	public static void emptyCache() {
		if (db != null) {
			db.getEntityFactory().getCache().evictAll();
		}
	}

	public static IDatabase getDb() {
		if (db == null) {
			if (USE_FILE_BASED_DB)
				db = initFileBasedDb();
			else
				db = DerbyDatabase.createInMemory();
		}
		return db;
	}

	private static IDatabase initFileBasedDb() {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String dbName = "olca_test_db_1.4";
		File tmpDir = new File(tmpDirPath);
		File folder = new File(tmpDir, dbName);
		IDatabase db = new DerbyDatabase(folder);
		try {
			// (currently) it should be always possible to run the database
			// updates on databases that were already updated as the
			// updated should check if an update is necessary or not. Thus
			// we reset the version here and test if the updates work.
			String versionReset = "update openlca_version set version = 1";
			NativeSql.on(db).runUpdate(versionReset);
			Upgrades.runUpgrades(db);
			return db;
		} catch (Exception e) {
			throw new RuntimeException("DB-upgrades failed", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends CategorizedEntity> T insert(T entity) {
		IDatabase database = getDb();
		ModelType type = ModelType.forModelClass(entity.getClass());
		switch (type) {
		case ACTOR:
			return (T) new ActorDao(database).insert((Actor) entity);
		case CURRENCY:
			return (T) new CurrencyDao(database).insert((Currency) entity);
		case FLOW:
			return (T) new FlowDao(database).insert((Flow) entity);
		case FLOW_PROPERTY:
			return (T) new FlowPropertyDao(database)
					.insert((FlowProperty) entity);
		case IMPACT_METHOD:
			return (T) new ImpactMethodDao(database)
					.insert((ImpactMethod) entity);
		case PROCESS:
			return (T) new ProcessDao(database).insert((Process) entity);
		case PRODUCT_SYSTEM:
			return (T) new ProductSystemDao(database)
					.insert((ProductSystem) entity);
		case PROJECT:
			return (T) new ProjectDao(database).insert((Project) entity);
		case SOCIAL_INDICATOR:
			return (T) new SocialIndicatorDao(database)
					.insert((SocialIndicator) entity);
		case SOURCE:
			return (T) new SourceDao(database).insert((Source) entity);
		case UNIT_GROUP:
			return (T) new UnitGroupDao(database).insert((UnitGroup) entity);
		case LOCATION:
			return (T) new LocationDao(database).insert((Location) entity);
		case PARAMETER:
			return (T) new ParameterDao(database).insert((Parameter) entity);
		case CATEGORY:
			return (T) new CategoryDao(database).insert((Category) entity);
		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends CategorizedEntity> T update(T entity) {
		IDatabase database = getDb();
		ModelType type = ModelType.forModelClass(entity.getClass());
		switch (type) {
		case ACTOR:
			return (T) new ActorDao(database).update((Actor) entity);
		case CURRENCY:
			return (T) new CurrencyDao(database).update((Currency) entity);
		case FLOW:
			return (T) new FlowDao(database).update((Flow) entity);
		case FLOW_PROPERTY:
			return (T) new FlowPropertyDao(database)
					.update((FlowProperty) entity);
		case IMPACT_METHOD:
			return (T) new ImpactMethodDao(database)
					.update((ImpactMethod) entity);
		case PROCESS:
			return (T) new ProcessDao(database).update((Process) entity);
		case PRODUCT_SYSTEM:
			return (T) new ProductSystemDao(database)
					.update((ProductSystem) entity);
		case PROJECT:
			return (T) new ProjectDao(database).update((Project) entity);
		case SOCIAL_INDICATOR:
			return (T) new SocialIndicatorDao(database)
					.update((SocialIndicator) entity);
		case SOURCE:
			return (T) new SourceDao(database).update((Source) entity);
		case UNIT_GROUP:
			return (T) new UnitGroupDao(database).update((UnitGroup) entity);
		case LOCATION:
			return (T) new LocationDao(database).update((Location) entity);
		case PARAMETER:
			return (T) new ParameterDao(database).update((Parameter) entity);
		case CATEGORY:
			return (T) new CategoryDao(database).update((Category) entity);
		default:
			return null;
		}
	}

	public static void delete(CategorizedEntity entity) {
		IDatabase database = getDb();
		ModelType type = ModelType.forModelClass(entity.getClass());
		switch (type) {
		case ACTOR:
			new ActorDao(database).delete((Actor) entity);
			break;
		case CURRENCY:
			new CurrencyDao(database).delete((Currency) entity);
			break;
		case FLOW:
			new FlowDao(database).delete((Flow) entity);
			break;
		case FLOW_PROPERTY:
			new FlowPropertyDao(database).delete((FlowProperty) entity);
			break;
		case IMPACT_METHOD:
			new ImpactMethodDao(database).delete((ImpactMethod) entity);
			break;
		case PROCESS:
			new ProcessDao(database).delete((Process) entity);
			break;
		case PRODUCT_SYSTEM:
			new ProductSystemDao(database).delete((ProductSystem) entity);
			break;
		case PROJECT:
			new ProjectDao(database).delete((Project) entity);
			break;
		case SOCIAL_INDICATOR:
			new SocialIndicatorDao(database).delete((SocialIndicator) entity);
			break;
		case SOURCE:
			new SourceDao(database).delete((Source) entity);
			break;
		case UNIT_GROUP:
			new UnitGroupDao(database).delete((UnitGroup) entity);
			break;
		case LOCATION:
			new LocationDao(database).delete((Location) entity);
			break;
		case PARAMETER:
			new ParameterDao(database).delete((Parameter) entity);
			break;
		case CATEGORY:
			new CategoryDao(database).delete((Category) entity);
			break;
		default:
			break;
		}
	}

}
