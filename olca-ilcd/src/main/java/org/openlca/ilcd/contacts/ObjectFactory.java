package org.openlca.ilcd.contacts;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ilcd.contacts package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _ContactDataSet_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Contact", "contactDataSet");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ilcd.contacts
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link Contact }
	 * 
	 */
	public Contact createContact() {
		return new Contact();
	}

	/**
	 * Create an instance of {@link DataSetInfo }
	 * 
	 */
	public DataSetInfo createDataSetInformation() {
		return new DataSetInfo();
	}

	/**
	 * Create an instance of {@link Publication }
	 * 
	 */
	public Publication createPublication() {
		return new Publication();
	}

	/**
	 * Create an instance of {@link DataEntry }
	 * 
	 */
	public DataEntry createDataEntry() {
		return new DataEntry();
	}

	/**
	 * Create an instance of {@link ContactInfo }
	 * 
	 */
	public ContactInfo createContactInformation() {
		return new ContactInfo();
	}

	/**
	 * Create an instance of {@link AdminInfo }
	 * 
	 */
	public AdminInfo createAdministrativeInformation() {
		return new AdminInfo();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Contact
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Contact", name = "contactDataSet")
	public JAXBElement<Contact> createContactDataSet(Contact value) {
		return new JAXBElement<>(_ContactDataSet_QNAME, Contact.class, null,
				value);
	}

}
