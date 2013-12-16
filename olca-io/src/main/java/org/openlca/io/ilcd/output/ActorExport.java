package org.openlca.io.ilcd.output;

import org.openlca.core.model.Actor;
import org.openlca.ilcd.commons.ClassificationInformation;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataSetInformation;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.ContactBuilder;
import org.openlca.ilcd.util.LangString;

/**
 * The export of an openLCA actor to an ILCD contact data set.
 */
public class ActorExport {

	private Actor actor;
	private DataStore dataStore;
	private String baseUri;

	public ActorExport(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public Contact run(Actor actor) throws DataStoreException {
		this.actor = actor;
		DataSetInformation dataSetInfo = makeDataSetInfo();
		Contact contact = ContactBuilder.makeContact().withBaseUri(baseUri)
				.withDataSetInfo(dataSetInfo).getContact();
		dataStore.put(contact, actor.getRefId());
		this.actor = null;
		return contact;
	}

	private DataSetInformation makeDataSetInfo() {
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(actor.getRefId());
		LangString.addLabel(dataSetInfo.getName(), actor.getName());
		dataSetInfo.setEmail(actor.getEmail());
		dataSetInfo.setTelefax(actor.getTelefax());
		dataSetInfo.setTelephone(actor.getTelephone());
		dataSetInfo.setWWWAddress(actor.getWebsite());
		addAddress(dataSetInfo);
		if (actor.getDescription() != null) {
			LangString.addShortText(dataSetInfo.getDescription(),
					actor.getDescription());
		}
		addClassification(dataSetInfo);
		return dataSetInfo;
	}

	private void addAddress(DataSetInformation dataSetInfo) {
		String address = actor.getAddress();
		if (address != null) {
			if (actor.getZipCode() != null) {
				address += ", " + actor.getZipCode();
			}
			if (actor.getCity() != null) {
				address += " " + actor.getCity();
			}
			LangString.addShortText(dataSetInfo.getCentralContactPoint(),
					address);
		}
	}

	private void addClassification(DataSetInformation dataSetInfo) {
		if (actor.getCategory() != null) {
			CategoryConverter converter = new CategoryConverter();
			ClassificationInformation classification = converter
					.getClassificationInformation(actor.getCategory());
			if (classification != null) {
				dataSetInfo.setClassificationInformation(classification);
			}
		}
	}
}
