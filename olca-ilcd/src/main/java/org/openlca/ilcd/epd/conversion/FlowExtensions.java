package org.openlca.ilcd.epd.conversion;

import java.util.List;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.epd.model.EpdProduct;
import org.openlca.ilcd.epd.model.MaterialPropertyValue;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.Modelling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class FlowExtensions {

	public static EpdProduct read(Flow flow) {
		EpdProduct product = new EpdProduct();
		if (flow == null)
			return product;
		product.flow = flow;
		readInfoExtension(product);
		readMethodExtension(product);
		return product;
	}

	private static void readInfoExtension(EpdProduct p) {
		Other extension = getInfoExtension(p.flow, false);
		if (extension == null)
			return;
		MatML matML = new MatML(extension);
		List<MaterialPropertyValue> values = matML.readValues();
		p.properties.addAll(values);
		p.genericFlow = RefExtension.readFrom(extension, "isA").orElse(null);
	}

	private static void readMethodExtension(EpdProduct p) {
		Other extension = getMethodExtension(p.flow, false);
		if (extension == null)
			return;
		Element elem = Dom.getElement(extension, "vendorSpecificProduct");
		if (elem != null) {
			try {
				p.vendorSpecific = Boolean
					.parseBoolean(elem.getTextContent());
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(FlowExtensions.class);
				log.error("vendorSpecificProduct contains not a boolean", e);
			}
		}
		p.vendor = RefExtension.readFrom(
			extension, "referenceToVendor").orElse(null);
		p.documentation = RefExtension.readFrom(
			extension, "referenceToSource").orElse(null);
	}

	/**
	 * Writes the EPD extensions of the given product to the underlying ILCD
	 * flow data set.
	 */
	public static void write(EpdProduct p) {
		if (p == null || p.flow == null)
			return;
		try {
			writeInfoExtension(p);
			writeMethodExtension(p);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(FlowExtensions.class);
			log.error("failed to write flow properties for " + p, e);
		}
	}

	private static void writeInfoExtension(EpdProduct p) {
		if (p.genericFlow == null && p.properties.isEmpty()) {
			// clear the extension point
			if (p.flow == null)
				return;
			if (p.flow.flowInfo == null)
				return;
			if (p.flow.flowInfo.dataSetInfo == null)
				return;
			p.flow.flowInfo.dataSetInfo.other = null;
			return;
		}

		Other ext = getInfoExtension(p.flow, true);
		RefExtension.writeTo(ext, "isA", p.genericFlow);
		MatML matML = new MatML(ext);
		if (p.properties.isEmpty()) {
			matML.clear();
		} else {
			matML.createStructure(LangString.getFirst(p.flow.getName()));
			for (MaterialPropertyValue value : p.properties) {
				matML.append(value);
			}
		}
	}

	private static void writeMethodExtension(EpdProduct p) {
		var extension = getMethodExtension(p.flow, true);
		writeVendorSpecificTag(p, extension);
		RefExtension.writeTo(extension, "referenceToVendor", p.vendor);
		RefExtension.writeTo(extension, "referenceToSource", p.documentation);
	}

	private static void writeVendorSpecificTag(EpdProduct p, Other ext) {
		String tag = "vendorSpecificProduct";
		Element e = Dom.getElement(ext, tag);
		if (e == null) {
			e = Dom.createElement(Vocab.NS_EPD, tag);
			ext.any.add(e);
		}
		e.setTextContent(Boolean.toString(p.vendorSpecific));
	}

	private static Other getInfoExtension(Flow flow, boolean create) {
		FlowInfo flowInfo = flow.flowInfo;
		if (flowInfo == null) {
			if (!create)
				return null;
			flowInfo = new FlowInfo();
			flow.flowInfo = flowInfo;
		}
		DataSetInfo dataInfo = flowInfo.dataSetInfo;
		if (dataInfo == null) {
			if (!create)
				return null;
			dataInfo = new DataSetInfo();
			flowInfo.dataSetInfo = dataInfo;
		}
		Other other = dataInfo.other;
		if (other == null && create) {
			other = new Other();
			dataInfo.other = other;
		}
		return other;
	}

	private static Other getMethodExtension(Flow flow, boolean create) {
		Modelling mav = flow.modelling;
		if (mav == null) {
			if (!create)
				return null;
			mav = new Modelling();
			flow.modelling = mav;
		}
		LCIMethod method = mav.lciMethod;
		if (method == null) {
			if (!create)
				return null;
			method = new LCIMethod();
			mav.lciMethod = method;
		}
		Other other = method.other;
		if (other == null && create) {
			other = new Other();
			method.other = other;
		}
		return other;
	}
}
