package org.openlca.ilcd.descriptors;

import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.QuantitativeReferenceType;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ilcd.descriptors package.
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

	private final static QName _UseAdvice_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process", "useAdvice");
	private final static QName _Reviewer_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process", "reviewer");
	private final static QName _Name_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "name");
	private final static QName _ReviewDetails_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"reviewDetails");
	private final static QName _LicenseType_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"licenseType");
	private final static QName _Phone_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", "phone");
	private final static QName _DataSetVersion_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "dataSetVersion");
	private final static QName _GeneralComment_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "generalComment");
	private final static QName _File_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Source", "file");
	private final static QName _Synonyms_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "synonyms");
	private final static QName _Parameterized_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"parameterized");
	private final static QName _Location_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process", "location");
	private final static QName _ValidUntil_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"validUntil");
	private final static QName _ApprovedBy_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"approvedBy");
	private final static QName _CasNumber_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Flow", "casNumber");
	private final static QName _Www_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", "www");
	private final static QName _HasResults_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"hasResults");
	private final static QName _SumFormula_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Flow", "sumFormula");
	private final static QName _OtherReviewDetails_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"otherReviewDetails");
	private final static QName _ShortName_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "shortName");
	private final static QName _Citation_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Source", "citation");
	private final static QName _Uuid_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "uuid");
	private final static QName _Ownership_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process", "ownership");
	private final static QName _Email_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", "email");
	private final static QName _TechnicalPurpose_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"technicalPurpose");
	private final static QName _ShortDescription_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "shortDescription");
	private final static QName _Approach_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process", "approach");
	private final static QName _ReferenceYear_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"referenceYear");
	private final static QName _Category_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "category");
	private final static QName _BelongsTo_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Source", "belongsTo");
	private final static QName _OverallQuality_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"overallQuality");
	private final static QName _Fax_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", "fax");
	private final static QName _Reference_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "reference");
	private final static QName _ReferenceUnit_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/UnitGroup",
			"referenceUnit");
	private final static QName _CentralContactPoint_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Contact",
			"centralContactPoint");
	private final static QName _PublicationType_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Source",
			"publicationType");
	private final static QName _MethodPrinciple_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"methodPrinciple");
	private final static QName _Class_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "class");
	private final static QName _Format_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process", "format");
	private final static QName _Copyright_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process", "copyright");
	private final static QName _PermanentUri_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "permanentUri");
	private final static QName _CompletenessProductModel_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"completenessProductModel");
	private final static QName _UseRestrictions_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"useRestrictions");
	private final static QName _DataSetList_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI", "dataSetList");
	private final static QName _QuantitativeReference_QNAME = new QName(
			"http://www.ilcd-network.org/ILCD/ServiceAPI/Process",
			"quantitativeReference");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ilcd.descriptors
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link ContactDescriptor }
	 * 
	 */
	public ContactDescriptor createContactDescriptor() {
		return new ContactDescriptor();
	}

	/**
	 * Create an instance of {@link LangString }
	 * 
	 */
	public LangString createLangString() {
		return new LangString();
	}

	/**
	 * Create an instance of {@link Classification }
	 * 
	 */
	public Classification createClassification() {
		return new Classification();
	}

	/**
	 * Create an instance of {@link ClassType }
	 * 
	 */
	public ClassType createClassType() {
		return new ClassType();
	}

	/**
	 * Create an instance of {@link DescriptorList }
	 * 
	 */
	public DescriptorList createDescriptorList() {
		return new DescriptorList();
	}

	/**
	 * Create an instance of {@link DataSetReference }
	 * 
	 */
	public DataSetReference createDataSetReference() {
		return new DataSetReference();
	}

	/**
	 * Create an instance of {@link FlowPropertyDescriptor }
	 * 
	 */
	public FlowPropertyDescriptor createFlowPropertyDescriptor() {
		return new FlowPropertyDescriptor();
	}

	/**
	 * Create an instance of {@link UnitGroupReference }
	 * 
	 */
	public UnitGroupReference createUnitGroupReference() {
		return new UnitGroupReference();
	}

	/**
	 * Create an instance of {@link MethodDescriptor }
	 * 
	 */
	public MethodDescriptor createMethodDescriptor() {
		return new MethodDescriptor();
	}

	/**
	 * Create an instance of {@link UnitGroupDescriptor }
	 * 
	 */
	public UnitGroupDescriptor createUnitGroupDescriptor() {
		return new UnitGroupDescriptor();
	}

	/**
	 * Create an instance of {@link SourceDescriptor }
	 * 
	 */
	public SourceDescriptor createSourceDescriptor() {
		return new SourceDescriptor();
	}

	/**
	 * Create an instance of {@link Scope }
	 * 
	 */
	public Scope createScope() {
		return new Scope();
	}

	/**
	 * Create an instance of {@link Method }
	 * 
	 */
	public Method createMethod() {
		return new Method();
	}

	/**
	 * Create an instance of {@link AccessInfo }
	 * 
	 */
	public AccessInfo createAccessInformation() {
		return new AccessInfo();
	}

	/**
	 * Create an instance of {@link Time }
	 * 
	 */
	public Time createTime() {
		return new Time();
	}

	/**
	 * Create an instance of {@link DataQualityIndicators }
	 * 
	 */
	public DataQualityIndicators createDataQualityIndicators() {
		return new DataQualityIndicators();
	}

	/**
	 * Create an instance of {@link ComplianceSystem }
	 * 
	 */
	public ComplianceSystem createComplianceSystem() {
		return new ComplianceSystem();
	}

	/**
	 * Create an instance of {@link ProcessDescriptor }
	 * 
	 */
	public ProcessDescriptor createProcessDescriptor() {
		return new ProcessDescriptor();
	}

	/**
	 * Create an instance of {@link LciMethodInformation }
	 * 
	 */
	public LciMethodInformation createLciMethodInformation() {
		return new LciMethodInformation();
	}

	/**
	 * Create an instance of {@link Review }
	 * 
	 */
	public Review createReview() {
		return new Review();
	}

	/**
	 * Create an instance of {@link ProcessFlowCategorization }
	 * 
	 */
	public ProcessFlowCategorization createProcessFlowCategorization() {
		return new ProcessFlowCategorization();
	}

	/**
	 * Create an instance of {@link ReferenceFlowType }
	 * 
	 */
	public ReferenceFlowType createReferenceFlowType() {
		return new ReferenceFlowType();
	}

	/**
	 * Create an instance of {@link ReferenceFlowProperty }
	 * 
	 */
	public ReferenceFlowProperty createReferenceFlowProperty() {
		return new ReferenceFlowProperty();
	}

	/**
	 * Create an instance of {@link FlowDescriptor }
	 * 
	 */
	public FlowDescriptor createFlowDescriptor() {
		return new FlowDescriptor();
	}

	/**
	 * Create an instance of {@link FlowCategorization }
	 * 
	 */
	public FlowCategorization createFlowCategorization() {
		return new FlowCategorization();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "useAdvice")
	public JAXBElement<LangString> createUseAdvice(LangString value) {
		return new JAXBElement<>(_UseAdvice_QNAME, LangString.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link DataSetReference }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "reviewer")
	public JAXBElement<DataSetReference> createReviewer(
			DataSetReference value) {
		return new JAXBElement<>(_Reviewer_QNAME, DataSetReference.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "name")
	public JAXBElement<LangString> createName(LangString value) {
		return new JAXBElement<>(_Name_QNAME, LangString.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "reviewDetails")
	public JAXBElement<LangString> createReviewDetails(LangString value) {
		return new JAXBElement<>(_ReviewDetails_QNAME, LangString.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "licenseType")
	public JAXBElement<String> createLicenseType(String value) {
		return new JAXBElement<>(_LicenseType_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", name = "phone")
	public JAXBElement<String> createPhone(String value) {
		return new JAXBElement<>(_Phone_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "dataSetVersion")
	public JAXBElement<String> createDataSetVersion(String value) {
		return new JAXBElement<>(_DataSetVersion_QNAME, String.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "generalComment")
	public JAXBElement<LangString> createGeneralComment(LangString value) {
		return new JAXBElement<>(_GeneralComment_QNAME, LangString.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link DataSetReference }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source", name = "file")
	public JAXBElement<DataSetReference> createFile(DataSetReference value) {
		return new JAXBElement<>(_File_QNAME, DataSetReference.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "synonyms")
	public JAXBElement<LangString> createSynonyms(LangString value) {
		return new JAXBElement<>(_Synonyms_QNAME, LangString.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Boolean
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "parameterized")
	public JAXBElement<Boolean> createParameterized(Boolean value) {
		return new JAXBElement<>(_Parameterized_QNAME, Boolean.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "location")
	public JAXBElement<String> createLocation(String value) {
		return new JAXBElement<>(_Location_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "validUntil")
	public JAXBElement<BigInteger> createValidUntil(BigInteger value) {
		return new JAXBElement<>(_ValidUntil_QNAME, BigInteger.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link DataSetReference }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "approvedBy")
	public JAXBElement<DataSetReference> createApprovedBy(
			DataSetReference value) {
		return new JAXBElement<>(_ApprovedBy_QNAME, DataSetReference.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow", name = "casNumber")
	public JAXBElement<String> createCasNumber(String value) {
		return new JAXBElement<>(_CasNumber_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", name = "www")
	public JAXBElement<String> createWww(String value) {
		return new JAXBElement<>(_Www_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Boolean
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "hasResults")
	public JAXBElement<Boolean> createHasResults(Boolean value) {
		return new JAXBElement<>(_HasResults_QNAME, Boolean.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Flow", name = "sumFormula")
	public JAXBElement<String> createSumFormula(String value) {
		return new JAXBElement<>(_SumFormula_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "otherReviewDetails")
	public JAXBElement<LangString> createOtherReviewDetails(LangString value) {
		return new JAXBElement<>(_OtherReviewDetails_QNAME, LangString.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "shortName")
	public JAXBElement<String> createShortName(String value) {
		return new JAXBElement<>(_ShortName_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source", name = "citation")
	public JAXBElement<LangString> createCitation(LangString value) {
		return new JAXBElement<>(_Citation_QNAME, LangString.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "uuid")
	public JAXBElement<String> createUuid(String value) {
		return new JAXBElement<>(_Uuid_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link DataSetReference }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "ownership")
	public JAXBElement<DataSetReference> createOwnership(
			DataSetReference value) {
		return new JAXBElement<>(_Ownership_QNAME, DataSetReference.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", name = "email")
	public JAXBElement<String> createEmail(String value) {
		return new JAXBElement<>(_Email_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "technicalPurpose")
	public JAXBElement<String> createTechnicalPurpose(String value) {
		return new JAXBElement<>(_TechnicalPurpose_QNAME, String.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "shortDescription")
	public JAXBElement<LangString> createShortDescription(LangString value) {
		return new JAXBElement<>(_ShortDescription_QNAME, LangString.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "referenceYear")
	public JAXBElement<BigInteger> createReferenceYear(BigInteger value) {
		return new JAXBElement<>(_ReferenceYear_QNAME, BigInteger.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ClassType }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "category")
	public JAXBElement<ClassType> createCategory(ClassType value) {
		return new JAXBElement<>(_Category_QNAME, ClassType.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link DataSetReference }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Source", name = "belongsTo")
	public JAXBElement<DataSetReference> createBelongsTo(
			DataSetReference value) {
		return new JAXBElement<>(_BelongsTo_QNAME, DataSetReference.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "overallQuality")
	public JAXBElement<String> createOverallQuality(String value) {
		return new JAXBElement<>(_OverallQuality_QNAME, String.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", name = "fax")
	public JAXBElement<String> createFax(String value) {
		return new JAXBElement<>(_Fax_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link DataSetReference }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "reference")
	public JAXBElement<DataSetReference> createReference(
			DataSetReference value) {
		return new JAXBElement<>(_Reference_QNAME, DataSetReference.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/UnitGroup", name = "referenceUnit")
	public JAXBElement<String> createReferenceUnit(String value) {
		return new JAXBElement<>(_ReferenceUnit_QNAME, String.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Contact", name = "centralContactPoint")
	public JAXBElement<String> createCentralContactPoint(String value) {
		return new JAXBElement<>(_CentralContactPoint_QNAME, String.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ClassType }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "class")
	public JAXBElement<ClassType> createClass(ClassType value) {
		return new JAXBElement<>(_Class_QNAME, ClassType.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "format")
	public JAXBElement<String> createFormat(String value) {
		return new JAXBElement<>(_Format_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Boolean
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "copyright")
	public JAXBElement<Boolean> createCopyright(Boolean value) {
		return new JAXBElement<>(_Copyright_QNAME, Boolean.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "permanentUri")
	public JAXBElement<String> createPermanentUri(String value) {
		return new JAXBElement<>(_PermanentUri_QNAME, String.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LangString }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "useRestrictions")
	public JAXBElement<LangString> createUseRestrictions(LangString value) {
		return new JAXBElement<>(_UseRestrictions_QNAME, LangString.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link DescriptorList
	 * } {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", name = "dataSetList")
	public JAXBElement<DescriptorList> createDataSetList(DescriptorList value) {
		return new JAXBElement<>(_DataSetList_QNAME, DescriptorList.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link QuantitativeReferenceType }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI/Process", name = "quantitativeReference")
	public JAXBElement<QuantitativeReferenceType> createQuantitativeReference(
			QuantitativeReferenceType value) {
		return new JAXBElement<>(_QuantitativeReference_QNAME,
				QuantitativeReferenceType.class, null, value);
	}

}
