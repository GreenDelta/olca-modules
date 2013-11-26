package org.openlca.ecospold2;

import org.jdom2.Element;

public class AdministrativeInformation {

	private DataEntryBy dataEntryBy;
	private DataGenerator dataGenerator;

	public void setDataEntryBy(DataEntryBy dataEntryBy) {
		this.dataEntryBy = dataEntryBy;
	}

	public DataEntryBy getDataEntryBy() {
		return dataEntryBy;
	}

	static AdministrativeInformation fromXml(Element e) {
		if (e == null)
			return null;
		AdministrativeInformation info = new AdministrativeInformation();
		info.dataEntryBy = DataEntryBy.fromXml(In.child(e, "dataEntryBy"));
		info.dataGenerator = DataGenerator.fromXml(In.child(e,
				"dataGeneratorAndPublication"));
		return info;
	}

	Element toXml() {
		Element element = new Element("administrativeInformation", Out.NS);
		if (dataEntryBy != null)
			element.addContent(dataEntryBy.toXml());
		if (dataGenerator != null)
			element.addContent(dataGenerator.toXml());
		return element;
	}

}
