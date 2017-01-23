package org.openlca.ilcd.methods;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ilcd.methods package.
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

	private final static QName _LCIAMethodDataSet_QNAME = new QName(
			"http://lca.jrc.it/ILCD/LCIAMethod", "LCIAMethodDataSet");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ilcd.methods
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
	 * Create an instance of {@link LCIAMethod }
	 * 
	 */
	public LCIAMethod createLCIAMethod() {
		return new LCIAMethod();
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
	 * Create an instance of {@link FactorList }
	 * 
	 */
	public FactorList createFactorList() {
		return new FactorList();
	}

	/**
	 * Create an instance of {@link MethodInfo }
	 * 
	 */
	public MethodInfo createLCIAMethodInformation() {
		return new MethodInfo();
	}

	/**
	 * Create an instance of {@link ImpactModel }
	 * 
	 */
	public ImpactModel createImpactModel() {
		return new ImpactModel();
	}

	/**
	 * Create an instance of {@link Validation }
	 * 
	 */
	public Validation createValidation() {
		return new Validation();
	}

	/**
	 * Create an instance of {@link Factor }
	 * 
	 */
	public Factor createFactor() {
		return new Factor();
	}

	/**
	 * Create an instance of {@link DataEntry }
	 * 
	 */
	public DataEntry createDataEntry() {
		return new DataEntry();
	}

	/**
	 * Create an instance of {@link NormalisationAndWeighting }
	 * 
	 */
	public NormalisationAndWeighting createNormalisationAndWeighting() {
		return new NormalisationAndWeighting();
	}

	/**
	 * Create an instance of {@link Recommendation }
	 * 
	 */
	public Recommendation createRecommendation() {
		return new Recommendation();
	}

	/**
	 * Create an instance of {@link Time }
	 * 
	 */
	public Time createTime() {
		return new Time();
	}

	/**
	 * Create an instance of {@link Review.Scope.Method }
	 * 
	 */
	public Review.Scope.Method createReviewScopeMethod() {
		return new Review.Scope.Method();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LCIAMethod }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/LCIAMethod", name = "LCIAMethodDataSet")
	public JAXBElement<LCIAMethod> createLCIAMethodDataSet(LCIAMethod value) {
		return new JAXBElement<>(_LCIAMethodDataSet_QNAME, LCIAMethod.class,
				null, value);
	}

}
