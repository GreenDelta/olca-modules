package org.openlca.ecospold2;

import org.jdom2.Element;

public class AdministrativeInformation {

	private DataEntryBy dataEntryBy;

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

		// // dataEntryBy
		// administrativeinformation.dataEntryBy = In.childText(e,
		// "dataEntryBy");
		// administrativeinformation.dataEntryBy = e
		// .getAttributeValue("dataEntryBy");
		//
		// // dataGeneratorAndPublication
		// administrativeinformation.dataGeneratorAndPublication =
		// In.childText(e,
		// "dataGeneratorAndPublication");
		// administrativeinformation.dataGeneratorAndPublication = e
		// .getAttributeValue("dataGeneratorAndPublication");
		//
		// // fileAttributes
		// administrativeinformation.fileAttributes = In.childText(e,
		// "fileAttributes");
		// administrativeinformation.fileAttributes = e
		// .getAttributeValue("fileAttributes");

		return info;
	}

	Element toXml() {
		Element element = new Element("administrativeInformation", Out.NS);

		if (dataEntryBy != null)
			element.addContent(dataEntryBy.toXml());
		// Out.addChild(element, "dataEntryBy", dataEntryBy);
		// // element.setAttribute("dataEntryBy", dataEntryBy);
		//
		// if (dataGeneratorAndPublication != null)
		// Out.addChild(element, "dataGeneratorAndPublication",
		// dataGeneratorAndPublication);
		// // element.setAttribute("dataGeneratorAndPublication",
		// // dataGeneratorAndPublication);
		//
		// if (fileAttributes != null)
		// Out.addChild(element, "fileAttributes", fileAttributes);
		// // element.setAttribute("fileAttributes", fileAttributes);

		return element;
	}

}
