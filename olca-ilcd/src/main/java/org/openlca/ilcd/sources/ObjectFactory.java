package org.openlca.ilcd.sources;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ilcd.sources package.
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

	private final static QName _SourceDataSet_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Source", "sourceDataSet");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ilcd.sources
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link Source }
	 * 
	 */
	public Source createSource() {
		return new Source();
	}

	/**
	 * Create an instance of {@link DigitalFileReference }
	 * 
	 */
	public DigitalFileReference createDigitalFileReference() {
		return new DigitalFileReference();
	}

	/**
	 * Create an instance of {@link SourceInformation }
	 * 
	 */
	public SourceInformation createSourceInformation() {
		return new SourceInformation();
	}

	/**
	 * Create an instance of {@link DataSetInformation }
	 * 
	 */
	public DataSetInformation createDataSetInformation() {
		return new DataSetInformation();
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
	 * Create an instance of {@link AdministrativeInformation }
	 * 
	 */
	public AdministrativeInformation createAdministrativeInformation() {
		return new AdministrativeInformation();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Source }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Source", name = "sourceDataSet")
	public JAXBElement<Source> createSourceDataSet(Source value) {
		return new JAXBElement<>(_SourceDataSet_QNAME, Source.class, null,
				value);
	}

}
