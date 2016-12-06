package org.openlca.ecospold2;

import org.jdom2.Element;

public class AdministrativeInformation {

	public DataEntryBy dataEntryBy;
	public DataGenerator dataGenerator;
	public FileAttributes fileAttributes;

	static AdministrativeInformation fromXml(Element e) {
		if (e == null)
			return null;
		AdministrativeInformation info = new AdministrativeInformation();
		info.dataEntryBy = DataEntryBy.fromXml(In.child(e, "dataEntryBy"));
		info.dataGenerator = DataGenerator.fromXml(In.child(e,
				"dataGeneratorAndPublication"));
		info.fileAttributes = FileAttributes.fromXml(In.child(e,
				"fileAttributes"));
		return info;
	}

	Element toXml() {
		Element element = new Element("administrativeInformation", IO.NS);
		if (dataEntryBy != null)
			element.addContent(dataEntryBy.toXml());
		if (dataGenerator != null)
			element.addContent(dataGenerator.toXml());
		if (fileAttributes != null)
			element.addContent(fileAttributes.toXml());
		return element;
	}

}
