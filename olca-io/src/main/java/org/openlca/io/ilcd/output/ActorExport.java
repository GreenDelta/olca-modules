/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/

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
		DataSetInformation dataSetInfo = makeDataSetInfo();
		Contact contact = ContactBuilder.makeContact().withBaseUri(baseUri)
				.withDataSetInfo(dataSetInfo).getContact();
		dataStore.put(contact, actor.getRefId());
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
