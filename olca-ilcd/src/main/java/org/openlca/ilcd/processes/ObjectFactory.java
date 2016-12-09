package org.openlca.ilcd.processes;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ilcd.processes package.
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

	private final static QName _ProcessDataSet_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Process", "processDataSet");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ilcd.processes
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link Review }
	 * 
	 */
	public Review createReview() {
		return new Review();
	}

	/**
	 * Create an instance of {@link Review.Scope }
	 * 
	 */
	public Review.Scope createReviewScope() {
		return new Review.Scope();
	}

	/**
	 * Create an instance of {@link Process }
	 * 
	 */
	public Process createProcess() {
		return new Process();
	}

	/**
	 * Create an instance of {@link Parameter }
	 * 
	 */
	public Parameter createParameter() {
		return new Parameter();
	}

	/**
	 * Create an instance of {@link DataSetInfo }
	 * 
	 */
	public DataSetInfo createDataSetInformation() {
		return new DataSetInfo();
	}

	/**
	 * Create an instance of {@link ComplianceDeclaration }
	 * 
	 */
	public ComplianceDeclaration createComplianceDeclaration() {
		return new ComplianceDeclaration();
	}

	/**
	 * Create an instance of {@link Geography }
	 * 
	 */
	public Geography createGeography() {
		return new Geography();
	}

	/**
	 * Create an instance of {@link Completeness }
	 * 
	 */
	public Completeness createCompleteness() {
		return new Completeness();
	}

	/**
	 * Create an instance of {@link Representativeness }
	 * 
	 */
	public Representativeness createRepresentativeness() {
		return new Representativeness();
	}

	/**
	 * Create an instance of {@link DataGenerator }
	 * 
	 */
	public DataGenerator createDataGenerator() {
		return new DataGenerator();
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
	 * Create an instance of {@link Exchange }
	 * 
	 */
	public Exchange createExchange() {
		return new Exchange();
	}

	/**
	 * Create an instance of {@link Modelling }
	 * 
	 */
	public Modelling createModellingAndValidation() {
		return new Modelling();
	}

	/**
	 * Create an instance of {@link SubLocation }
	 * 
	 */
	public SubLocation createSubLocation() {
		return new SubLocation();
	}

	/**
	 * Create an instance of {@link Publication }
	 * 
	 */
	public Publication createPublication() {
		return new Publication();
	}

	/**
	 * Create an instance of {@link ParameterSection }
	 * 
	 */
	public ParameterSection createParameterList() {
		return new ParameterSection();
	}

	/**
	 * Create an instance of {@link AllocationFactor }
	 * 
	 */
	public AllocationFactor createAllocationFactor() {
		return new AllocationFactor();
	}

	/**
	 * Create an instance of {@link Validation }
	 * 
	 */
	public Validation createValidation() {
		return new Validation();
	}

	/**
	 * Create an instance of {@link ProcessName }
	 * 
	 */
	public ProcessName createProcessName() {
		return new ProcessName();
	}

	/**
	 * Create an instance of {@link DataEntry }
	 * 
	 */
	public DataEntry createDataEntry() {
		return new DataEntry();
	}

	/**
	 * Create an instance of {@link Method }
	 * 
	 */
	public Method createLCIMethod() {
		return new Method();
	}

	/**
	 * Create an instance of {@link ProcessInfo }
	 * 
	 */
	public ProcessInfo createProcessInformation() {
		return new ProcessInfo();
	}

	/**
	 * Create an instance of {@link Technology }
	 * 
	 */
	public Technology createTechnology() {
		return new Technology();
	}

	/**
	 * Create an instance of {@link LCIAResult }
	 * 
	 */
	public LCIAResult createLCIAResult() {
		return new LCIAResult();
	}

	/**
	 * Create an instance of {@link FlowCompletenessEntry }
	 * 
	 */
	public FlowCompletenessEntry createElementaryFlowCompleteness() {
		return new FlowCompletenessEntry();
	}

	/**
	 * Create an instance of {@link Location }
	 * 
	 */
	public Location createLocation() {
		return new Location();
	}

	/**
	 * Create an instance of {@link Review.Scope.Method }
	 * 
	 */
	public Review.Method createReviewScopeMethod() {
		return new Review.Method();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Process
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Process", name = "processDataSet")
	public JAXBElement<Process> createProcessDataSet(Process value) {
		return new JAXBElement<>(_ProcessDataSet_QNAME, Process.class, null,
				value);
	}

}
