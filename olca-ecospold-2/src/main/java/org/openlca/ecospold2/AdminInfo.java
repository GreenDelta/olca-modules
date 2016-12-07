package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class AdminInfo {

	@XmlElement(name = "dataEntryBy")
	public DataEntry dataEntry;

	@XmlElement(name = "dataGeneratorAndPublication")
	public DataGenerator dataGenerator;

	@XmlElement
	public FileAttributes fileAttributes;

	static AdminInfo fromXml(Element e) {
		if (e == null)
			return null;
		AdminInfo info = new AdminInfo();
		info.dataEntry = DataEntry.fromXml(In.child(e, "dataEntryBy"));
		info.dataGenerator = DataGenerator.fromXml(In.child(e,
				"dataGeneratorAndPublication"));
		info.fileAttributes = FileAttributes.fromXml(In.child(e,
				"fileAttributes"));
		return info;
	}

	Element toXml() {
		Element element = new Element("administrativeInformation", IO.NS);
		if (dataEntry != null)
			element.addContent(dataEntry.toXml());
		if (dataGenerator != null)
			element.addContent(dataGenerator.toXml());
		if (fileAttributes != null)
			element.addContent(fileAttributes.toXml());
		return element;
	}

}
