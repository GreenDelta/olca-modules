package org.openlca.ilcd.sources;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.Publication;

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
	 * Create an instance of {@link FileRef }
	 * 
	 */
	public FileRef createDigitalFileReference() {
		return new FileRef();
	}

	/**
	 * Create an instance of {@link SourceInfo }
	 * 
	 */
	public SourceInfo createSourceInformation() {
		return new SourceInfo();
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
	 * Create an instance of {@link AdminInfo }
	 * 
	 */
	public AdminInfo createAdministrativeInformation() {
		return new AdminInfo();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Source
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Source", name = "sourceDataSet")
	public JAXBElement<Source> createSourceDataSet(Source value) {
		return new JAXBElement<>(_SourceDataSet_QNAME, Source.class, null,
				value);
	}

}
