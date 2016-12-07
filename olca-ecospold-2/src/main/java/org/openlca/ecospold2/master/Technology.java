package org.openlca.ecospold2.master;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.openlca.ecospold2.RichText;

@XmlAccessorType(XmlAccessType.FIELD)
public class Technology {

	@XmlAttribute(name = "technologyLevel")
	public Integer level;

	public RichText comment;

}
