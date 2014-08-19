package org.openlca.geo.multi;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public class MultiPlacemarkMerger {

	/**
	 * Merges multiple placemark elements to one placemark containing a
	 * MultiGeometry. Throws error if geometries are not homogeneous or
	 * geometries intercept
	 * 
	 * @param kml
	 * @return
	 */
	public String merge(String kmlInput) {
		Kml kml = Kml.unmarshal(new ByteArrayInputStream(kmlInput.getBytes()));
		Folder folder = (Folder) kml.getFeature();
		if (folder.getFeature().size() <= 1)
			return kmlInput;
		for (Feature feature : folder.getFeature())
			if (!(feature instanceof Placemark))
				return kmlInput;
		MultiGeometry geometries = new MultiGeometry();
		for (Feature feature : folder.getFeature()) {
			Placemark placemark = (Placemark) feature;
			Geometry geometry = placemark.getGeometry();
			checkAndAdd(geometries, geometry);
		}
		Placemark mainPlacemark = (Placemark) folder.getFeature().get(0);
		mainPlacemark.setGeometry(geometries);
		folder.getFeature().clear();
		folder.getFeature().add(mainPlacemark);
		StringWriter writer = new StringWriter();
		kml.marshal(writer);
		return writer.toString();
	}

	private void checkAndAdd(MultiGeometry geometries, Geometry geometry) {
		checkHomogeneous(geometries, geometry);
		checkNotIntercepting(geometries, geometry);
		if (geometry instanceof MultiGeometry)
			for (Geometry geo : ((MultiGeometry) geometry).getGeometry())
				geometries.addToGeometry(geo);
		else
			geometries.addToGeometry(geometry);
	}

	private void checkHomogeneous(MultiGeometry geometries, Geometry geometry) {
		if (geometry instanceof MultiGeometry) {
			for (Geometry geo : ((MultiGeometry) geometry).getGeometry())
				if (!fits(geometries, geo))
					throw new IllegalArgumentException(
							"Geometries are not homogeneous, aborting transformation");
		} else if (!fits(geometries, geometry))
			throw new IllegalArgumentException(
					"Geometries are not homogeneous, aborting transformation");
	}

	private void checkNotIntercepting(MultiGeometry geometries,
			Geometry geometry) {
		if (geometry instanceof MultiGeometry) {
			for (Geometry geo : ((MultiGeometry) geometry).getGeometry())
				if (intercepts(geometries, geo))
					throw new IllegalArgumentException(
							"Geometries are intercepting, aborting transformation");
		} else if (intercepts(geometries, geometry))
			throw new IllegalArgumentException(
					"Geometries are intercepting, aborting transformation");
	}

	private boolean fits(MultiGeometry geometries, Geometry geometry) {
		if (geometries.getGeometry().isEmpty())
			return true;
		Geometry item = geometries.getGeometry().get(0);
		return item.getClass() == geometry.getClass();
	}

	private boolean intercepts(MultiGeometry geometries, Geometry geometry) {
		if (geometries.getGeometry().isEmpty())
			return false;
		for (Geometry geo : geometries.getGeometry())
			if (intercepts(geo, geometry))
				return true;
		return false;
	}

	private boolean intercepts(Geometry geometry, Geometry interceptor) {
		return JakToGeotools.convert(geometry).intersects(
				JakToGeotools.convert(interceptor));
	}
}
