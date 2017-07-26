package org.openlca.ilcd.descriptors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains the list of category systems that are available in a data
 * repository.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "categorySystems", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
public class CategorySystemList {

	@XmlElement(name = "categorySystem", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<Entry> systems = new ArrayList<>();

	public static class Entry {

		@XmlAttribute
		public String name;

	}

	/**
	 * Convenience method to get the names of all category systems that are
	 * contained in this list.
	 */
	public List<String> getNames() {
		List<String> names = new ArrayList<>();
		for (Entry e : systems) {
			if (e.name != null) {
				names.add(e.name);
			}
		}
		return names;
	}

}
