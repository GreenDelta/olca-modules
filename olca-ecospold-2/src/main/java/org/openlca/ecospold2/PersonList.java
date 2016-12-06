package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Represents a list of persons in a master data file.
 */
public class PersonList {

	public final List<Person> persons = new ArrayList<>();

	static PersonList fromXml(Document doc) {
		PersonList list = new PersonList();
		if (doc == null)
			return list;
		List<Element> elements = In.childs(doc.getRootElement(), "person");
		for (Element element : elements) {
			Person person = Person.fromXml(element);
			if (person != null)
				list.persons.add(person);
		}
		return list;
	}

	Document toXml() {
		Element root = new Element("validPersons", IO.NS);
		Document document = new Document(root);
		for (Person p : persons)
			root.addContent(p.toXml(IO.NS));
		return document;
	}

}
