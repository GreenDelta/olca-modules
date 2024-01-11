package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInfo;
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
		Contact contact = new Contact();
		contact.version = "1.1";
		ContactInfo info = new ContactInfo();
		contact.contactInfo = info;
		info.dataSetInfo = makeDataSetInfo();
		contact.adminInfo = makeAdminInfo();
		exp.store.put(contact);
		this.actor = null;
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo();
		info.uuid = actor.refId;
		exp.add(info.name, actor.name);
		info.email = actor.email;
		info.telefax = actor.telefax;
		info.telephone = actor.telephone;
		info.webSite = actor.website;
		addAddress(info);
		exp.add(info.description, actor.description);
		Categories.toClassification(actor.category)
				.ifPresent(info.classifications::add);
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
		exp.add(dataSetInfo.contactAddress, address);
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Xml.calendar(actor.lastChange);
		entry.formats.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = Version.asString(actor.version);
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.uri = baseUri + "contacts/" + actor.refId;
	}
}
