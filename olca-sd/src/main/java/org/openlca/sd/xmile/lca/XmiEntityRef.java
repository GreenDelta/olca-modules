package org.openlca.sd.xmile.lca;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import org.openlca.core.model.ModelType;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiEntityRef {

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "id")
	private String id;

	@XmlAttribute(name = "type")
	private ModelType type;

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String id() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ModelType type() {
		return type;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

}
