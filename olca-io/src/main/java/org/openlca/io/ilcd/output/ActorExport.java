package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataSetInfo;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;

public class ActorExport {

	private final Export exp;
	private Actor actor;
	private String baseUri;

	public ActorExport(Export exp) {
		this.exp = exp;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public void write(Actor actor) {
		if (actor == null || exp.store.contains(Contact.class, actor.refId))
			return;
		this.actor = actor;
		var contact = new Contact();
		contact
				.withAdminInfo(makeAdminInfo())
				.withContactInfo()
				.withDataSetInfo(makeDataSetInfo());
		exp.store.put(contact);
		this.actor = null;
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo()
				.withUUID(actor.refId)
				.withEmail(actor.email)
				.withTelefax(actor.telefax)
				.withTelephone(actor.telephone)
				.withWebSite(actor.website);
		exp.add(info::withName, actor.name);
		addAddress(info);
		exp.add(info::withDescription, actor.description);
		Categories.toClassification(
				actor.category, info::withClassifications);
		return info;
	}

	private void addAddress(DataSetInfo dataSetInfo) {
		var address = actor.address;
		if (address == null)
			return;
		if (actor.zipCode != null) {
			address += ", " + actor.zipCode;
		}
		if (actor.city != null) {
			address += " " + actor.city;
		}
		exp.add(dataSetInfo::withContactAddress, address);
	}

	private AdminInfo makeAdminInfo() {
		var info = new AdminInfo();
		info.withDataEntry()
				.withTimeStamp(Xml.calendar(actor.lastChange))
				.withFormats().add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		var uri = baseUri == null
				? "http://openlca.org/ilcd/resource/"
				: baseUri;
		if (!uri.endsWith("/")) {
			uri += "/";
		}
		info.withPublication()
				.withVersion(Version.asString(actor.version))
				.withUri(uri);
	}
}
