package org.openlca.core.database;

import java.io.InputStream;

import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AnalysisGroup;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessGroupSet;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.MappingFile;
import org.openlca.core.model.Result;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.ComplianceDeclaration;
import org.openlca.core.model.doc.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DbUtils {

	private static final String PERSISTENCE_XML_PATH = "/META-INF/persistence.xml";
	private static volatile byte[] cachedPersistenceXml;

	private DbUtils() {
	}

	/**
	 * Returns the version of the given database, or -1 if an error occured.
	 */
	static int getVersion(IDatabase database) {
		try {
			final int[] version = new int[1];
			NativeSql.on(database).query("select version from openlca_version",
					result -> {
						version[0] = result.getInt(1);
						return true;
					});
			return version[0];
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(DbUtils.class);
			log.error("failed to get the database version", e);
			return -1;
		}
	}

	/**
	 * Caches the persistence.xml resource to avoid repeated classpath scanning
	 * and resource resolution overhead. This warms up the classloader's resource
	 * cache, making subsequent reads by EclipseLink faster.
	 */
	static void cachePersistenceXml() {
		Logger log = LoggerFactory.getLogger(DbUtils.class);
		// Use double-checked locking pattern for thread-safe lazy initialization
		if (cachedPersistenceXml != null) {
			return; // Already cached
		}
		synchronized (DbUtils.class) {
			if (cachedPersistenceXml != null) {
				return; // Another thread cached it
			}
			log.trace("Caching persistence.xml resource");
			try {
				// Pre-read persistence.xml from classpath to warm up resource cache
				InputStream stream = DbUtils.class.getResourceAsStream(PERSISTENCE_XML_PATH);
				if (stream != null) {
					try {
						cachedPersistenceXml = stream.readAllBytes();
						log.trace("Cached persistence.xml ({} bytes)", cachedPersistenceXml.length);
					} finally {
						stream.close();
					}
				} else {
					log.warn("Could not find persistence.xml at {}", PERSISTENCE_XML_PATH);
				}
			} catch (Exception e) {
				log.warn("Failed to cache persistence.xml", e);
				// Don't throw - caching is an optimization, not required
			}
		}
	}

	/**
	 * Preloads all entity classes before EntityManagerFactory creation to reduce
	 * startup time. This helps avoid the expensive class loading and annotation
	 * processing that occurs during the initialization phase.
	 */
	static void preloadEntityClasses() {
		Logger log = LoggerFactory.getLogger(DbUtils.class);
		log.trace("Preloading entity classes");
		try {
			// Load all entity classes listed in persistence.xml
			// This triggers class loading and annotation processing upfront
			Class<?>[] entities = {
				Actor.class,
				AllocationFactor.class,
				AnalysisGroup.class,
				Category.class,
				Currency.class,
				Exchange.class,
				Flow.class,
				FlowProperty.class,
				FlowPropertyFactor.class,
				ImpactCategory.class,
				ImpactFactor.class,
				ImpactMethod.class,
				Location.class,
				NwSet.class,
				NwFactor.class,
				Parameter.class,
				ParameterRedef.class,
				ParameterRedefSet.class,
				Process.class,
				ProcessDoc.class,
				ProcessGroupSet.class,
				ProcessLink.class,
				ProductSystem.class,
				Project.class,
				ProjectVariant.class,
				SocialAspect.class,
				Source.class,
				Unit.class,
				UnitGroup.class,
				SocialIndicator.class,
				DQSystem.class,
				DQIndicator.class,
				DQScore.class,
				MappingFile.class,
				Result.class,
				FlowResult.class,
				ImpactResult.class,
				Epd.class,
				EpdModule.class,
				ComplianceDeclaration.class,
				Review.class,
			};
			
			// Touch each class to trigger loading
			for (Class<?> entity : entities) {
				entity.getName(); // Trigger class loading
			}
			log.trace("Preloaded {} entity classes", entities.length);
		} catch (Exception e) {
			log.warn("Failed to preload some entity classes", e);
			// Don't throw - preloading is an optimization, not required
		}
	}

	/**
	 * Performs all pre-initialization optimizations before EntityManagerFactory
	 * creation. This includes caching persistence.xml and preloading entity classes.
	 * Should be called before createEntityManagerFactory() to reduce startup time.
	 */
	static void preInitialize() {
		cachePersistenceXml();
		preloadEntityClasses();
	}
}
