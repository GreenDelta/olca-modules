package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiSmile {

	@XmlAttribute(name = "version")
	String version;

	@XmlAttribute(name = "namespace")
	String namespace;

	@XmlAttribute(name = "uses_arrays")
	String usesArrays;

	public String version() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String namespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String usesArrays() {
		return usesArrays;
	}

	public void setUsesArrays(String usesArrays) {
		this.usesArrays = usesArrays;
	}
}
