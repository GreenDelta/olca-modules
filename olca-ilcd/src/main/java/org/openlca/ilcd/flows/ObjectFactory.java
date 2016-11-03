package org.openlca.ilcd.flows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Publication;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ilcd.flows package.
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

	private final static QName _FlowDataSet_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Flow", "flowDataSet");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ilcd.flows
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link Flow }
	 * 
	 */
	public Flow createFlow() {
		return new Flow();
	}

	/**
	 * Create an instance of {@link LCIMethod }
	 * 
	 */
	public LCIMethod createLCIMethod() {
		return new LCIMethod();
	}

	/**
	 * Create an instance of {@link ComplianceDeclaration }
	 * 
	 */
	public ComplianceDeclaration createComplianceDeclaration() {
		return new ComplianceDeclaration();
	}

	/**
	 * Create an instance of {@link DataSetInfo }
	 * 
	 */
	public DataSetInfo createDataSetInformation() {
		return new DataSetInfo();
	}

	/**
	 * Create an instance of {@link Geography }
	 * 
	 */
	public Geography createGeography() {
		return new Geography();
	}

	/**
	 * Create an instance of {@link FlowName }
	 * 
	 */
	public FlowName createFlowName() {
		return new FlowName();
	}

	/**
	 * Create an instance of {@link FlowInfo }
	 * 
	 */
	public FlowInfo createFlowInformation() {
		return new FlowInfo();
	}

	/**
	 * Create an instance of {@link DataEntry }
	 * 
	 */
	public DataEntry createDataEntry() {
		return new DataEntry();
	}

	/**
	 * Create an instance of {@link FlowPropertyRef }
	 * 
	 */
	public FlowPropertyRef createFlowPropertyReference() {
		return new FlowPropertyRef();
	}

	/**
	 * Create an instance of {@link ImpactFactorAvailability }
	 * 
	 */
	public ImpactFactorAvailability createImpactFactorAvailability() {
		return new ImpactFactorAvailability();
	}

	/**
	 * Create an instance of {@link Technology }
	 * 
	 */
	public Technology createTechnology() {
		return new Technology();
	}

	/**
	 * Create an instance of {@link AdminInfo }
	 * 
	 */
	public AdminInfo createAdministrativeInformation() {
		return new AdminInfo();
	}

	/**
	 * Create an instance of {@link QuantitativeReference }
	 * 
	 */
	public QuantitativeReference createQuantitativeReference() {
		return new QuantitativeReference();
	}

	/**
	 * Create an instance of {@link Modelling }
	 * 
	 */
	public Modelling createModellingAndValidation() {
		return new Modelling();
	}

	/**
	 * Create an instance of {@link Publication }
	 * 
	 */
	public Publication createPublication() {
		return new Publication();
	}

	/**
	 * Create an instance of {@link ComplianceDeclarationList }
	 * 
	 */
	public ComplianceDeclarationList createComplianceDeclarationList() {
		return new ComplianceDeclarationList();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Flow }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Flow", name = "flowDataSet")
	public JAXBElement<Flow> createFlowDataSet(Flow value) {
		return new JAXBElement<>(_FlowDataSet_QNAME, Flow.class, null, value);
	}

}
