package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInfo;
import org.openlca.ilcd.contacts.DataSetInfo;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.Refs;

public class ActorExport {

	private final ExportConfig config;
	private Actor actor;
	private String baseUri;

	public ActorExport(ExportConfig config) {
		this.config = config;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public Contact run(Actor actor) throws DataStoreException {
		if (config.store.contains(Contact.class, actor.getRefId()))
			return config.store.get(Contact.class, actor.getRefId());
		this.actor = actor;
		Contact contact = new Contact();
		contact.version = "1.1";
		ContactInfo info = new ContactInfo();
		contact.contactInfo = info;
		info.dataSetInfo = makeDataSetInfo();
		contact.adminInfo = makeAdminInfo();
		config.store.put(contact);
		this.actor = null;
		return contact;
	}

	private DataSetInfo makeDataSetInfo() {
		DataSetInfo info = new DataSetInfo();
		info.uuid = actor.getRefId();
		LangString.set(info.name, actor.getName(), config.lang);
		info.email = actor.getEmail();
		info.telefax = actor.getTelefax();
		info.telephone = actor.getTelephone();
		info.wwwAddress = actor.getWebsite();
		addAddress(info);
		if (actor.getDescription() != null) {
			LangString.set(info.description,
					actor.getDescription(), config.lang);
		}
		addClassification(info);
		return info;
	}

	private void addAddress(DataSetInfo dataSetInfo) {
		String address = actor.getAddress();
		if (address == null)
			return;
		if (actor.getZipCode() != null)
			address += ", " + actor.getZipCode();
		if (actor.getCity() != null)
			address += " " + actor.getCity();
		LangString.set(dataSetInfo.contactAddress, address,
				config.lang);
	}

	private void addClassification(DataSetInfo dataSetInfo) {
		if (actor.getCategory() == null)
			return;
		CategoryConverter converter = new CategoryConverter();
		Classification classification = converter.getClassification(
				actor.getCategory());
		if (classification != null) {
			dataSetInfo.classifications.add(classification);
		}
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Out.getTimestamp(actor);
		entry.formats.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = Version.asString(actor.getVersion());
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.uri = baseUri + "contacts/" + actor.getRefId();
	}
}
