package org.openlca.sd.xmile.svg;

import java.io.StringWriter;

import org.openlca.commons.Res;
import org.openlca.sd.xmile.Xmile;
import org.openlca.sd.xmile.view.XmiView;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class Svg {

	static final String NS = "http://www.w3.org/2000/svg";

	public static SvgDoc docOf(Xmile xmile) {
		if (xmile == null || xmile.model() == null)
			return new SvgDoc();
		var views = xmile.model().views();
		return views.isEmpty()
			? new SvgDoc()
			: docOf(views.getFirst());
	}

	public static SvgDoc docOf(XmiView view) {
		return Converter.convert(view);
	}

	public static Res<String> xmlOf(Xmile xmile) {
		if (xmile == null || xmile.model() == null)
			return Res.error("No XMILE model available");
		var views = xmile.model().views();
		if (views.isEmpty())
			return Res.error("No views available in XMILE model");
		var doc = docOf(views.getFirst());
		return xmlOf(doc);
	}

	public static Res<String> xmlOf(SvgDoc svg) {
		try {
			var context = JAXBContext.newInstance(SvgDoc.class);
			var marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			var writer = new StringWriter();
			marshaller.marshal(svg, writer);
			return Res.ok(writer.toString());
		} catch (JAXBException e) {
			return Res.error("Failed to marshal SVG to XML", e);
		}
	}
}
