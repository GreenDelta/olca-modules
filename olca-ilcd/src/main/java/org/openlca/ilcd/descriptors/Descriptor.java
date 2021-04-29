package org.openlca.ilcd.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * A descriptor contains only some meta-information of a data set.
 */
@XmlTransient
public abstract class Descriptor {

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String uuid;

	@XmlElement(name = "permanentUri", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	@XmlSchemaType(name = "anyURI")
	public String uri;

	@XmlElement(name = "dataSetVersion", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String version;

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<LangString> name = new ArrayList<>();

	@XmlElement(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<Classification> classification = new ArrayList<>();

	@XmlElement(name = "generalComment", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<LangString> comment = new ArrayList<>();

	protected abstract DataSetType getType();

	public final Ref toRef() {
		Ref ref = new Ref();
		LangString.copy(name, ref.name);
		ref.type = getType();
		ref.uri = uri;
		ref.uuid = uuid;
		ref.version = version;
		return ref;
	}

}
