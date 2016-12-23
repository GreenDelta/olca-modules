package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class AdminInfo {

	@XmlElement(name = "dataEntryBy")
	public DataEntry dataEntry;

	@XmlElement(name = "dataGeneratorAndPublication")
	public DataGenerator dataGenerator;

	@XmlElement
	public FileAttributes fileAttributes;

}
