package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.annotations.ShortText;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

/**
 * Ref describes an ILCD data set reference (GlobalReferenceType).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalReferenceType", propOrder = { "name" })
public class Ref implements Serializable {

	private final static long serialVersionUID = 1L;

	@ShortText
	@XmlElement(name = "shortDescription")
	public final List<LangString> name = new ArrayList<>();

	@XmlAttribute(name = "type", required = true)
	public DataSetType type;

	@XmlAttribute(name = "refObjectId")
	public String uuid;

	@XmlAttribute(name = "version")
	public String version;

	@XmlAttribute(name = "uri")
	@XmlSchemaType(name = "anyURI")
	public String uri;

	@Override
	public String toString() {
		return "Ref [type=" + type + ", uuid=" + uuid + "]";
	}

	public boolean isValid() {
		return uuid != null && type != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Ref))
			return false;
		Ref other = (Ref) obj;
		return Objects.equals(this.type, other.type)
				&& Objects.equals(this.uuid, other.uuid);
	}

	@Override
	public Ref clone() {
		Ref clone = new Ref();
		LangString.copy(name, clone.name);
		clone.type = type;
		clone.uuid = uuid;
		clone.version = version;
		clone.uri = uri;
		return clone;
	}

	/**
	 * Copies all data set references from the given source list to the given
	 * target list.
	 */
	public static void copy(List<Ref> source, List<Ref> target) {
		if (source == null || target == null)
			return;
		for (Ref ref : source) {
			if (ref == null)
				continue;
			target.add(ref.clone());
		}
	}

	public static Ref[] copy(Ref[] refs) {
		if (refs == null)
			return null;
		Ref[] copy = new Ref[refs.length];
		for (int i = 0; i < refs.length; i++) {
			if (refs[i] == null)
				continue;
			copy[i] = refs[i].clone();
		}
		return copy;
	}

	public Class<? extends IDataSet> getDataSetClass() {
		if (type == null)
			return null;
		switch (type) {
		case CONTACT:
			return Contact.class;
		case SOURCE:
			return Source.class;
		case UNIT_GROUP:
			return UnitGroup.class;
		case FLOW_PROPERTY:
			return FlowProperty.class;
		case FLOW:
			return Flow.class;
		case PROCESS:
			return Process.class;
		case LCIA_METHOD:
			return LCIAMethod.class;
		default:
			return null;
		}
	}

	public static Ref of(IDataSet dataSet) {
		if (dataSet == null)
			return new Ref();
		Ref ref = new Ref();
		ref.uri = dataSet.getURI();
		ref.uuid = dataSet.getUUID();
		ref.type = dataSet.getDataSetType();
		ref.version = dataSet.getVersion();
		LangString.copy(dataSet.getName(), ref.name);
		return ref;
	}
}
