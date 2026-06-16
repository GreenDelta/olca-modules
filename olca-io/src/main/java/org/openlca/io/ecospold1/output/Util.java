package org.openlca.io.ecospold1.output;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.commons.Strings;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Flow;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IExchange;
import org.openlca.io.Xml;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Util {

	private Util() {
	}

	static String comment(RefEntity model, EcoSpold1Config config) {
		if (model == null) return null;
		if (config == null || !config.withRefIdInfo)
			return model.description;
		var refIdInfo = "openLCA UUID: " + model.refId;
		return Strings.isNotBlank(model.description)
			? model.description + "\n\n" + refIdInfo
			: refIdInfo;
	}

	static XMLGregorianCalendar xmlYear(int year) {
		try {
			var xmlCal = Xml.types().newXMLGregorianCalendar();
			xmlCal.setYear(year);
			return xmlCal;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Util.class);
			log.warn("failed to set year of source ", e);
			return null;
		}
	}

	static void setDataSetAttributes(DataSet ds, RefEntity model) {
		var r = ds.root();
		if (model != null) {
			r.setNumber((int) model.id);
		}
		r.setGenerator("openLCA");
		r.setTimestamp(Xml.calendar(new Date()));
	}

	static void mapFlowInformation(IExchange e, Flow flow) {
		if (Strings.isNotBlank(flow.casNumber)) {
			e.setCASNumber(flow.casNumber);
		}
		e.setFormula(flow.formula);
		e.setInfrastructureProcess(flow.infrastructureFlow);
		if (flow.location != null) {
			if (flow.location.code != null) {
				e.setLocation(flow.location.code);
			} else if (flow.location.name != null) {
				e.setLocation(flow.location.name);
			}
		}
		e.setInfrastructureProcess(flow.infrastructureFlow);
	}

	static int personOf(Actor actor, DataSet ds) {
		if (actor == null)
			return -1;
		int id = (int) actor.id;
		for (var p : ds.getPersons()) {
			if (p.getNumber() == id)
				return id;
		}
		var p = ds.withPerson();
		p.setNumber(id);
		p.setName(actor.name);
		p.setAddress(actor.address);
		p.setEmail(actor.email);
		p.setTelefax(actor.telefax);
		p.setTelephone(actor.telephone);
		if (Strings.isNotBlank(actor.country)) {
			var code = ds.factory().getCountryCode(actor.country);
			p.setCountryCode(code);
		}
		return id;
	}

	static int sourceOf(Source source, DataSet ds) {
		if (source == null)
			return -1;
		int id = (int) source.id;
		for (var s : ds.getSources()) {
			if (s.getNumber() == id)
				return id;
		}
		var s = ds.withSource();
		s.setNumber(id);
		s.setTitle(source.name);
		s.setFirstAuthor(source.textReference);
		s.setText(source.description);
		if (source.year != null) {
			s.setYear(Util.xmlYear(source.year));
		}
		s.setPlaceOfPublications("unknown");
		s.setSourceType(0);
		return id;
	}

	static float versionOf(RootEntity e) {
		if (e == null)
			return 0f;
		var v = new Version(e.version);
		var major = (float) v.getMajor();
		var minor = (float) v.getMinor();
		return major + (minor / 100f);
	}

	static float internalVersionOf(RootEntity e) {
		if (e == null)
			return 0f;
		var v = new Version(e.version);
		return v.getUpdate();
	}
}
