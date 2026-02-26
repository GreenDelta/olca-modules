package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiHeader {

	@XmlElement(name = "smile", namespace = Xmile.NS)
	XmiSmile smile;

	@XmlElement(name = "name", namespace = Xmile.NS)
	String name;

	@XmlElement(name = "uuid", namespace = Xmile.NS)
	String uuid;

	@XmlElement(name = "vendor", namespace = Xmile.NS)
	String vendor;

	@XmlElement(name = "product", namespace = Xmile.NS)
	XmiProduct product;

	public XmiSmile smile() {
		return smile;
	}

	public void setSmile(XmiSmile smile) {
		this.smile = smile;
	}

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String uuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String vendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public XmiProduct product() {
		return product;
	}

	public void setProduct(XmiProduct product) {
		this.product = product;
	}
}
