
package org.openlca.ilcd.sources;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceToDigitalFileType")
public class FileRef implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlAttribute(name = "uri")
	@XmlSchemaType(name = "anyURI")
	public String uri;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof FileRef))
			return false;
		FileRef other = (FileRef) obj;
		if (this.uri == null && other.uri == null)
			return true;
		if (this.uri == null || other.uri == null)
			return false;
		return this.uri.trim().equalsIgnoreCase(other.uri.trim());
	}

	@Override
	public int hashCode() {
		return uri == null ? super.hashCode() : uri.hashCode();
	}

	@Override
	public FileRef clone() {
		FileRef clone = new FileRef();
		clone.uri = uri;
		return clone;
	}
}
