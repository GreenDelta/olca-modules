package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.contacts.AdministrativeInformation;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInformation;
import org.openlca.ilcd.contacts.DataEntry;
import org.openlca.ilcd.contacts.DataSetInformation;
import org.openlca.ilcd.contacts.Publication;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.Reference;

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
		contact.setVersion("1.1");
		ContactInformation info = new ContactInformation();
		contact.setContactInformation(info);
		info.setDataSetInformation(makeDataSetInfo());
		contact.setAdministrativeInformation(makeAdminInfo());
		config.store.put(contact, actor.getRefId());
		this.actor = null;
		return contact;
	}

	private DataSetInformation makeDataSetInfo() {
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(actor.getRefId());
		LangString.addLabel(dataSetInfo.getName(), actor.getName(),
				config.ilcdConfig);
		dataSetInfo.setEmail(actor.getEmail());
		dataSetInfo.setTelefax(actor.getTelefax());
		dataSetInfo.setTelephone(actor.getTelephone());
		dataSetInfo.setWWWAddress(actor.getWebsite());
		addAddress(dataSetInfo);
		if (actor.getDescription() != null) {
			LangString.addShortText(dataSetInfo.getDescription(),
					actor.getDescription(), config.ilcdConfig);
		}
		addClassification(dataSetInfo);
		return dataSetInfo;
	}

	private void addAddress(DataSetInformation dataSetInfo) {
		String address = actor.getAddress();
		if (address == null)
			return;
		if (actor.getZipCode() != null)
			address += ", " + actor.getZipCode();
		if (actor.getCity() != null)
			address += " " + actor.getCity();
		LangString.addShortText(dataSetInfo.getCentralContactPoint(), address,
				config.ilcdConfig);
	}

	private void addClassification(DataSetInformation dataSetInfo) {
		if (actor.getCategory() == null)
			return;
		CategoryConverter converter = new CategoryConverter();
		ClassificationInformation classification = converter
				.getClassificationInformation(actor.getCategory());
		if (classification != null) {
			dataSetInfo.setClassificationInformation(classification);
		}
	}

	private AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		DataEntry entry = new DataEntry();
		info.setDataEntry(entry);
		entry.setTimeStamp(Out.getTimestamp(actor));
		entry.getReferenceToDataSetFormat().add(
				Reference.forIlcdFormat(config.ilcdConfig));
		addPublication(info);
		return info;
	}

	private void addPublication(AdministrativeInformation info) {
		Publication pub = new Publication();
		info.setPublication(pub);
		pub.setDataSetVersion(Version.asString(actor.getVersion()));
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.setPermanentDataSetURI(baseUri + "contacts/" + actor.getRefId());
	}
}
