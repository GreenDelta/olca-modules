package org.openlca.geo.multi;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public class MultiGeometryParser {

	public List<Geometry> parse(String kmlInput) {
		Kml kml = Kml.unmarshal(new ByteArrayInputStream(kmlInput.getBytes()));
		Feature feature = getFeature(kml);
		if (!(feature instanceof Placemark))
			return new ArrayList<>();
		Placemark pm = (Placemark) feature;
		if (!(pm.getGeometry() instanceof MultiGeometry))
			return new ArrayList<>();
		MultiGeometry geometry = (MultiGeometry) pm.getGeometry();
		return geometry.getGeometry();
	}

	private Feature getFeature(Kml kml) {
		if (kml.getFeature() instanceof Folder) {
			Folder folder = (Folder) kml.getFeature();
			if (folder.getFeature().size() == 0)
				return null;
			return folder.getFeature().get(0);
		} else if (kml.getFeature() instanceof Document) {
			Document document = (Document) kml.getFeature();
			if (document.getFeature().size() == 0)
				return null;
			return document.getFeature().get(0);
		}
		return null;
	}
	
}
