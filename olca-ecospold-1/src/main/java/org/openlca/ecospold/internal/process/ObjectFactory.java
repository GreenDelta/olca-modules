package org.openlca.ecospold.internal.process;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ecospold.process package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
class ObjectFactory {

	private final static QName _EcoSpold_QNAME = new QName(
			"http://www.EcoInvent.org/EcoSpold01", "ecoSpold");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ecospold.process
	 * 
	 */
	ObjectFactory() {
	}

	/**
	 * Create an instance of {@link EcoSpold }
	 * 
	 */
	public EcoSpold createEcoSpold() {
		return new EcoSpold();
	}

	/**
	 * Create an instance of {@link DataEntryBy }
	 * 
	 */
	public DataEntryBy createDataEntryBy() {
		return new DataEntryBy();
	}

	/**
	 * Create an instance of {@link ModellingAndValidation }
	 * 
	 */
	public ModellingAndValidation createModellingAndValidation() {
		return new ModellingAndValidation();
	}

	/**
	 * Create an instance of {@link Allocation }
	 * 
	 */
	public Allocation createAllocation() {
		return new Allocation();
	}

	/**
	 * Create an instance of {@link Technology }
	 * 
	 */
	public Technology createTechnology() {
		return new Technology();
	}

	/**
	 * Create an instance of {@link Person }
	 * 
	 */
	public Person createPerson() {
		return new Person();
	}

	/**
	 * Create an instance of {@link Exchange }
	 * 
	 */
	public Exchange createExchange() {
		return new Exchange();
	}

	/**
	 * Create an instance of {@link ReferenceFunction }
	 * 
	 */
	public ReferenceFunction createReferenceFunction() {
		return new ReferenceFunction();
	}

	/**
	 * Create an instance of {@link ProcessInformation }
	 * 
	 */
	public ProcessInformation createProcessInformation() {
		return new ProcessInformation();
	}

	/**
	 * Create an instance of {@link Validation }
	 * 
	 */
	public Validation createValidation() {
		return new Validation();
	}

	/**
	 * Create an instance of {@link AdministrativeInformation }
	 * 
	 */
	public AdministrativeInformation createAdministrativeInformation() {
		return new AdministrativeInformation();
	}

	/**
	 * Create an instance of {@link DataGeneratorAndPublication }
	 * 
	 */
	public DataGeneratorAndPublication createDataGeneratorAndPublication() {
		return new DataGeneratorAndPublication();
	}

	/**
	 * Create an instance of {@link DataSet }
	 * 
	 */
	public DataSet createDataSet() {
		return new DataSet();
	}

	/**
	 * Create an instance of {@link DataSetInformation }
	 * 
	 */
	public DataSetInformation createDataSetInformation() {
		return new DataSetInformation();
	}

	/**
	 * Create an instance of {@link Source }
	 * 
	 */
	public Source createSource() {
		return new Source();
	}

	/**
	 * Create an instance of {@link MetaInformation }
	 * 
	 */
	public MetaInformation createMetaInformation() {
		return new MetaInformation();
	}

	/**
	 * Create an instance of {@link TimePeriod }
	 * 
	 */
	public TimePeriod createTimePeriod() {
		return new TimePeriod();
	}

	/**
	 * Create an instance of {@link Representativeness }
	 * 
	 */
	public Representativeness createRepresentativeness() {
		return new Representativeness();
	}

	/**
	 * Create an instance of {@link Geography }
	 * 
	 */
	public Geography createGeography() {
		return new Geography();
	}

	/**
	 * Create an instance of {@link FlowData }
	 * 
	 */
	public FlowData createFlowData() {
		return new FlowData();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link EcoSpold }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.EcoInvent.org/EcoSpold01", name = "ecoSpold")
	public JAXBElement<EcoSpold> createEcoSpold(EcoSpold value) {
		return new JAXBElement<>(_EcoSpold_QNAME, EcoSpold.class, null, value);
	}

}
