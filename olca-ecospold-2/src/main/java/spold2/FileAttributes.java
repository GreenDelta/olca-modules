package spold2;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class FileAttributes {

	@XmlAttribute
	public int majorRelease;

	@XmlAttribute
	public int minorRelease;

	@XmlAttribute
	public int majorRevision;

	@XmlAttribute
	public int minorRevision;

	@XmlAttribute
	public String defaultLanguage;

	@XmlAttribute
	public Date creationTimestamp;

	@XmlAttribute
	public Date lastEditTimestamp;

	@XmlAttribute
	public String internalSchemaVersion;

	@XmlAttribute
	public String fileGenerator;

	@XmlAttribute
	public Date fileTimestamp;

	@XmlAttribute
	public String contextId;

	@XmlElement
	public String contextName;

	@XmlElement
	public String requiredContext;

}
