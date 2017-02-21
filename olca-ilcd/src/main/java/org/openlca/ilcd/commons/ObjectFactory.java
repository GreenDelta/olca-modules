package org.openlca.ilcd.commons;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.flows.Compartment;
import org.openlca.ilcd.flows.CompartmentList;
import org.openlca.ilcd.flows.FlowCategoryInfo;
import org.openlca.ilcd.methods.Location;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ilcd.commons package.
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

	private final static QName _ReviewDetails_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "reviewDetails");
	private final static QName _NomenclatureCompliance_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "nomenclatureCompliance");
	private final static QName _ReferenceToOwnershipOfDataSet_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "referenceToOwnershipOfDataSet");
	private final static QName _ReferenceToRegistrationAuthority_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "referenceToRegistrationAuthority");
	private final static QName _ReferenceToConvertedOriginalDataSetFrom_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common",
			"referenceToConvertedOriginalDataSetFrom");
	private final static QName _DocumentationCompliance_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "documentationCompliance");
	private final static QName _Name_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "name");
	private final static QName _GeneralComment_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "generalComment");
	private final static QName _Synonyms_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "synonyms");
	private final static QName _ReferenceToRawDataDocumentation_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "referenceToRawDataDocumentation");
	private final static QName _UUID_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "UUID");
	private final static QName _ReferenceToPersonOrEntityGeneratingTheDataSet_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common",
			"referenceToPersonOrEntityGeneratingTheDataSet");
	private final static QName _ILCD_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Wrapper", "ILCD");
	private final static QName _ReviewCompliance_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "reviewCompliance");
	private final static QName _DateOfLastRevision_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "dateOfLastRevision");
	private final static QName _ShortName_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "shortName");
	private final static QName _AccessRestrictions_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "accessRestrictions");
	private final static QName _MethodologicalCompliance_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "methodologicalCompliance");
	private final static QName _CommissionerAndGoal_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "commissionerAndGoal");
	private final static QName _RegistrationNumber_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "registrationNumber");
	private final static QName _QualityCompliance_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "qualityCompliance");
	private final static QName _Copyright_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "copyright");
	private final static QName _ReferenceToDataSetUseApproval_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Common", "referenceToDataSetUseApproval");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ilcd.commons
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link Other }
	 * 
	 */
	public Other createOther() {
		return new Other();
	}

	/**
	 * Create an instance of {@link Ref }
	 * 
	 */
	public Ref createDataSetReference() {
		return new Ref();
	}

	/**
	 * Create an instance of {@link CommissionerAndGoal }
	 * 
	 */
	public CommissionerAndGoal createCommissionerAndGoal() {
		return new CommissionerAndGoal();
	}

	/**
	 * Create an instance of {@link FlowCategoryInfo }
	 * 
	 */
	public FlowCategoryInfo createFlowCategoryInformation() {
		return new FlowCategoryInfo();
	}

	/**
	 * Create an instance of {@link Compartment }
	 * 
	 */
	public Compartment createCategory() {
		return new Compartment();
	}

	/**
	 * Create an instance of {@link Location }
	 * 
	 */
	public Location createLocationType() {
		return new Location();
	}

	/**
	 * Create an instance of {@link Classification }
	 * 
	 */
	public Classification createClassification() {
		return new Classification();
	}

	/**
	 * Create an instance of {@link CompartmentList }
	 * 
	 */
	public CompartmentList createFlowCategorization() {
		return new CompartmentList();
	}

	/**
	 * Create an instance of {@link DataQualityIndicator }
	 * 
	 */
	public DataQualityIndicator createDataQualityIndicator() {
		return new DataQualityIndicator();
	}

	/**
	 * Create an instance of {@link Category }
	 * 
	 */
	public Category createClass() {
		return new Category();
	}

	/**
	 * Create an instance of {@link Time }
	 * 
	 */
	public Time createTime() {
		return new Time();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link FreeText }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "reviewDetails")
	public JAXBElement<LangString> createReviewDetails(LangString value) {
		return new JAXBElement<>(_ReviewDetails_QNAME, LangString.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Compliance }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "nomenclatureCompliance")
	public JAXBElement<Compliance> createNomenclatureCompliance(Compliance value) {
		return new JAXBElement<>(_NomenclatureCompliance_QNAME,
				Compliance.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <} {@link Ref }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToOwnershipOfDataSet")
	public JAXBElement<Ref> createReferenceToOwnershipOfDataSet(
			Ref value) {
		return new JAXBElement<>(_ReferenceToOwnershipOfDataSet_QNAME,
				Ref.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <} {@link Ref }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToRegistrationAuthority")
	public JAXBElement<Ref> createReferenceToRegistrationAuthority(
			Ref value) {
		return new JAXBElement<>(_ReferenceToRegistrationAuthority_QNAME,
				Ref.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <} {@link Ref }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToConvertedOriginalDataSetFrom")
	public JAXBElement<Ref> createReferenceToConvertedOriginalDataSetFrom(
			Ref value) {
		return new JAXBElement<>(
				_ReferenceToConvertedOriginalDataSetFrom_QNAME,
				Ref.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Compliance }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "documentationCompliance")
	public JAXBElement<Compliance> createDocumentationCompliance(
			Compliance value) {
		return new JAXBElement<>(_DocumentationCompliance_QNAME,
				Compliance.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Label
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "name")
	public JAXBElement<LangString> createName(LangString value) {
		return new JAXBElement<>(_Name_QNAME, LangString.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link FreeText }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "generalComment")
	public JAXBElement<LangString> createGeneralComment(LangString value) {
		return new JAXBElement<>(_GeneralComment_QNAME, LangString.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link FreeText }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "synonyms")
	public JAXBElement<LangString> createSynonyms(LangString value) {
		return new JAXBElement<>(_Synonyms_QNAME, LangString.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <} {@link Ref }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToRawDataDocumentation")
	public JAXBElement<Ref> createReferenceToRawDataDocumentation(
			Ref value) {
		return new JAXBElement<>(_ReferenceToRawDataDocumentation_QNAME,
				Ref.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "UUID")
	public JAXBElement<String> createUUID(String value) {
		return new JAXBElement<>(_UUID_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <} {@link Ref }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToPersonOrEntityGeneratingTheDataSet")
	public JAXBElement<Ref> createReferenceToPersonOrEntityGeneratingTheDataSet(
			Ref value) {
		return new JAXBElement<>(
				_ReferenceToPersonOrEntityGeneratingTheDataSet_QNAME,
				Ref.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Compliance }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "reviewCompliance")
	public JAXBElement<Compliance> createReviewCompliance(Compliance value) {
		return new JAXBElement<>(_ReviewCompliance_QNAME, Compliance.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link XMLGregorianCalendar }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "dateOfLastRevision")
	public JAXBElement<XMLGregorianCalendar> createDateOfLastRevision(
			XMLGregorianCalendar value) {
		return new JAXBElement<>(_DateOfLastRevision_QNAME,
				XMLGregorianCalendar.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Label
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "shortName")
	public JAXBElement<LangString> createShortName(LangString value) {
		return new JAXBElement<>(_ShortName_QNAME, LangString.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link FreeText }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "accessRestrictions")
	public JAXBElement<LangString> createAccessRestrictions(LangString value) {
		return new JAXBElement<>(_AccessRestrictions_QNAME, LangString.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Compliance }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "methodologicalCompliance")
	public JAXBElement<Compliance> createMethodologicalCompliance(
			Compliance value) {
		return new JAXBElement<>(_MethodologicalCompliance_QNAME,
				Compliance.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link CommissionerAndGoal }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "commissionerAndGoal")
	public JAXBElement<CommissionerAndGoal> createCommissionerAndGoal(
			CommissionerAndGoal value) {
		return new JAXBElement<>(_CommissionerAndGoal_QNAME,
				CommissionerAndGoal.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "registrationNumber")
	public JAXBElement<String> createRegistrationNumber(String value) {
		return new JAXBElement<>(_RegistrationNumber_QNAME, String.class, null,
				value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Compliance }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "qualityCompliance")
	public JAXBElement<Compliance> createQualityCompliance(Compliance value) {
		return new JAXBElement<>(_QualityCompliance_QNAME, Compliance.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Boolean
	 * }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "copyright")
	public JAXBElement<Boolean> createCopyright(Boolean value) {
		return new JAXBElement<>(_Copyright_QNAME, Boolean.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <} {@link Ref }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Common", name = "referenceToDataSetUseApproval")
	public JAXBElement<Ref> createReferenceToDataSetUseApproval(
			Ref value) {
		return new JAXBElement<>(_ReferenceToDataSetUseApproval_QNAME,
				Ref.class, null, value);
	}

}
