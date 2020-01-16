package org.openlca.geo;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.io.IOUtils;
import org.geotools.geometry.jts.GeometryBuilder;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.geo.parameter.ShapeFileFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Tests {

	private static ShapeFileFolder repository;
	private static IDatabase db;

	private Tests() {
	}

	public static IDatabase getDb() {
		if (db == null) {
			db = DerbyDatabase.createInMemory();
		}
		return db;
	}

	public static void clearDb() {
		try {
			IDatabase db = getDb();
			List<String> tables = new ArrayList<>();
			// type = T means user table
			String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE = 'T'";
			NativeSql.on(db).query(sql, r -> {
				tables.add(r.getString(1));
				return true;
			});
			for (String table : tables) {
				if (table.equalsIgnoreCase("SEQUENCE"))
					continue;
				if (table.equalsIgnoreCase("OPENLCA_VERSION"))
					continue;
				NativeSql.on(db).runUpdate("DELETE FROM " + table);
			}
			NativeSql.on(db).runUpdate("UPDATE SEQUENCE SET SEQ_COUNT = 0");
			db.getEntityFactory().getCache().evictAll();
		} catch (Exception e) {
			throw new RuntimeException("failed to clear database", e);
		}
	}

	public static String getKml(String file) {
		try {
			return IOUtils.toString(Tests.class.getResourceAsStream(file),
					"utf-8");
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Tests.class);
			log.error("failed to load kml " + file, e);
			return null;
		}
	}

	public static Geometry createPolygon(double... coordinates) {
		GeometryBuilder builder = new GeometryBuilder();
		return builder.polygon(coordinates);
	}

	public static Geometry createMultiGeometry(Geometry... geometries) {
		GeometryBuilder builder = new GeometryBuilder();
		return builder.geometryCollection(geometries);
	}

	public static ShapeFileFolder getRepository() {
		if (repository == null)
			repository = initRepository();
		return repository;
	}

	private static ShapeFileFolder initRepository() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File testDir = new File(tempDir, "olca_geo_test_dir");
		File repoDir = new File(testDir, "repo");
		if (!repoDir.exists()) {
			repoDir.mkdirs();
			extractSampleShapeFile(repoDir);
		}
		return new ShapeFileFolder(repoDir);
	}

	private static void extractSampleShapeFile(File repoDir) {
		try {
			File tempFile = Files.createTempFile("us_states_shp", ".zip")
					.toFile();
			FileOutputStream os = new FileOutputStream(tempFile);
			InputStream is = Tests.class
					.getResourceAsStream("us_states_shp.zip");
			IOUtils.copy(is, os);
			ZipFile zipFile = new ZipFile(tempFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File file = new File(repoDir, entry.getName());
				FileOutputStream fos = new FileOutputStream(file);
				InputStream zis = zipFile.getInputStream(entry);
				IOUtils.copy(zis, fos);
				zis.close();
				fos.close();
			}
			zipFile.close();
			is.close();
			os.close();
			tempFile.delete();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Tests.class);
			log.error("failed to init test repo", e);
		}
	}

}
