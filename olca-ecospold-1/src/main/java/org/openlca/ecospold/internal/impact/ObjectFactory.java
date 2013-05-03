package org.openlca.ecospold.internal.impact;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ecospold.impact package.
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
			"http://www.EcoInvent.org/EcoSpold01Impact", "ecoSpold");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ecospold.impact
	 * 
	 */
	ObjectFactory() {
	}

	/**
	 * Create an instance of {@link ImpactEcoSpold }
	 * 
	 */
	public ImpactEcoSpold createImpactEcoSpold() {
		return new ImpactEcoSpold();
	}

	/**
	 * Create an instance of {@link ImpactDataEntryBy }
	 * 
	 */
	public ImpactDataEntryBy createImpactDataEntryBy() {
		return new ImpactDataEntryBy();
	}

	/**
	 * Create an instance of {@link ImpactModellingAndValidation }
	 * 
	 */
	public ImpactModellingAndValidation createImpactModellingAndValidation() {
		return new ImpactModellingAndValidation();
	}

	/**
	 * Create an instance of {@link ImpactAllocation }
	 * 
	 */
	public ImpactAllocation createImpactAllocation() {
		return new ImpactAllocation();
	}

	/**
	 * Create an instance of {@link ImpactTechnology }
	 * 
	 */
	public ImpactTechnology createImpactTechnology() {
		return new ImpactTechnology();
	}

	/**
	 * Create an instance of {@link ImpactPerson }
	 * 
	 */
	public ImpactPerson createImpactPerson() {
		return new ImpactPerson();
	}

	/**
	 * Create an instance of {@link ImpactFactor }
	 * 
	 */
	public ImpactFactor createImpactFactor() {
		return new ImpactFactor();
	}

	/**
	 * Create an instance of {@link ImpactReferenceFunction }
	 * 
	 */
	public ImpactReferenceFunction createImpactReferenceFunction() {
		return new ImpactReferenceFunction();
	}

	/**
	 * Create an instance of {@link ImpactProcessInformation }
	 * 
	 */
	public ImpactProcessInformation createImpactProcessInformation() {
		return new ImpactProcessInformation();
	}

	/**
	 * Create an instance of {@link ImpactValidation }
	 * 
	 */
	public ImpactValidation createImpactValidation() {
		return new ImpactValidation();
	}

	/**
	 * Create an instance of {@link ImpactAdministrativeInformation }
	 * 
	 */
	public ImpactAdministrativeInformation createImpactAdministrativeInformation() {
		return new ImpactAdministrativeInformation();
	}

	/**
	 * Create an instance of {@link ImpactDataGeneratorAndPublication }
	 * 
	 */
	public ImpactDataGeneratorAndPublication createImpactDataGeneratorAndPublication() {
		return new ImpactDataGeneratorAndPublication();
	}

	/**
	 * Create an instance of {@link ImpactDataSet }
	 * 
	 */
	public ImpactDataSet createImpactDataSet() {
		return new ImpactDataSet();
	}

	/**
	 * Create an instance of {@link ImpactDataSetInformation }
	 * 
	 */
	public ImpactDataSetInformation createImpactDataSetInformation() {
		return new ImpactDataSetInformation();
	}

	/**
	 * Create an instance of {@link ImpactSource }
	 * 
	 */
	public ImpactSource createImpactSource() {
		return new ImpactSource();
	}

	/**
	 * Create an instance of {@link ImpactMetaInformation }
	 * 
	 */
	public ImpactMetaInformation createImpactMetaInformation() {
		return new ImpactMetaInformation();
	}

	/**
	 * Create an instance of {@link ImpactTimePeriod }
	 * 
	 */
	public ImpactTimePeriod createImpactTimePeriod() {
		return new ImpactTimePeriod();
	}

	/**
	 * Create an instance of {@link ImpactRepresentativeness }
	 * 
	 */
	public ImpactRepresentativeness createImpactRepresentativeness() {
		return new ImpactRepresentativeness();
	}

	/**
	 * Create an instance of {@link ImpactGeography }
	 * 
	 */
	public ImpactGeography createImpactGeography() {
		return new ImpactGeography();
	}

	/**
	 * Create an instance of {@link ImpactFactors }
	 * 
	 */
	public ImpactFactors createImpactFactors() {
		return new ImpactFactors();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ImpactEcoSpold }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.EcoInvent.org/EcoSpold01Impact", name = "ecoSpold")
	public JAXBElement<ImpactEcoSpold> createEcoSpold(ImpactEcoSpold value) {
		return new JAXBElement<>(_EcoSpold_QNAME, ImpactEcoSpold.class, null,
				value);
	}

}
