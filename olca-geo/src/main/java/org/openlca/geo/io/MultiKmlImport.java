package org.openlca.geo.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.util.BinUtils;
import org.openlca.util.KeyGen;

public class MultiKmlImport {

	private String kml;
	private String kmlStart;
	private String kmlEnd;
	private String placemarks;
	private LocationDao dao;

	public MultiKmlImport(IDatabase database, InputStream kml)
			throws IOException {
		this(database, IOUtils.toString(kml, "utf-8"));
	}

	public MultiKmlImport(IDatabase database, String kml) {
		int start = kml.toLowerCase().indexOf("<placemark");
		int end = kml.toLowerCase().lastIndexOf("</placemark>") + 12;
		this.kmlStart = kml.substring(0, start);
		this.placemarks = kml.substring(start, end);
		this.kmlEnd = kml.substring(end);
		this.kml = kml;
		this.dao = new LocationDao(database);
	}

	public void parseAndInsert() throws Exception {
		KMLConfiguration configuration = new KMLConfiguration();
		Parser parser = new Parser(configuration);
		StringReader reader = new StringReader(kml);
		SimpleFeature root = (SimpleFeature) parser.parse(reader);
		List<SimpleFeature> features = collect(root);
		for (SimpleFeature feature : features) {
			String name1 = (String) feature.getAttribute("NAME_0");
			String name2 = (String) feature.getAttribute("NAME_1");
			String name3 = (String) feature.getAttribute("NAME_2");
			String name = (String) feature.getAttribute("name");
			String code = (String) feature.getAttribute("HASC_2");
			if (name1 != null && name2 != null && name3 != null)
				name = name1 + " - " + name2 + " - " + name3;
			String subKml = getNextKml();
			insertLocation(name, code, subKml);
		}
	}

	private void insertLocation(String name, String code, String kml)
			throws UnsupportedEncodingException, IOException {
		String refId = null;
		if (code == null || code.isEmpty())
			refId = UUID.randomUUID().toString();
		else
			refId = KeyGen.get(code);
		if (dao.contains(refId))
			return;
		Location location = new Location();
		location.setName(name);
		location.setCode(code);
		location.setRefId(refId);
		location.setKmz(BinUtils.zip(kml.getBytes("utf-8")));
		dao.insert(location);
	}

	private List<SimpleFeature> collect(SimpleFeature root) {
		List<SimpleFeature> result = new ArrayList<>();
		List<?> features = (List<?>) root.getAttribute("Feature");
		if (features == null || features.isEmpty())
			return Collections.emptyList();
		for (Object f : features) {
			SimpleFeature feature = (SimpleFeature) f;
			List<SimpleFeature> subResult = collect(feature);
			if (!subResult.isEmpty())
				result.addAll(subResult);
			else
				result.add(feature);
		}
		return result;
	}

	private String getNextKml() {
		String placemark = placemarks.substring(0, placemarks.toLowerCase()
				.indexOf("</placemark>") + 12);
		placemarks = placemarks.substring(placemarks.toLowerCase().indexOf(
				"</placemark>") + 12);
		return kmlStart + placemark + kmlEnd;
	}

}
