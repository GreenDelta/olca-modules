package org.openlca.simapro.csv.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openlca.simapro.csv.model.enums.BoundaryWithNature;
import org.openlca.simapro.csv.model.enums.CutOffRule;
import org.openlca.simapro.csv.model.enums.Geography;
import org.openlca.simapro.csv.model.enums.ProcessAllocation;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.enums.ProcessType;
import org.openlca.simapro.csv.model.enums.Representativeness;
import org.openlca.simapro.csv.model.enums.Status;
import org.openlca.simapro.csv.model.enums.Substitution;
import org.openlca.simapro.csv.model.enums.SystemBoundary;
import org.openlca.simapro.csv.model.enums.Technology;
import org.openlca.simapro.csv.model.enums.TimePeriod;
import org.openlca.simapro.csv.model.enums.WasteTreatmentAllocation;

/**
 * This class represents the process information of a SimaPro process
 */
public class SPDocumentation {

	/**
	 * The Process identifier
	 */
	private String identifier;

	/**
	 * The allocation rules of the data set
	 */
	private String allocationRules;

	/**
	 * The boundary with nature of the data set
	 */
	private BoundaryWithNature boundaryWithNature;

	/**
	 * The process category of the data set
	 */
	private ProcessCategory category;

	/**
	 * The collection method of the data set
	 */
	private String collectionMethod;

	/**
	 * A comment to the data set
	 */
	private String comment;

	/**
	 * The creation date of the data set
	 */
	SimpleDateFormat simpleDate = new SimpleDateFormat("dd.MM.yyyy");
	private String creationDate = simpleDate.format(new Date());

	/**
	 * The cut off rule of the data set
	 */
	private CutOffRule cutOffRule;

	/**
	 * The data treatment of the data set
	 */
	private String dataTreatment;

	/**
	 * The generator of the data set
	 */
	private String generator;

	/**
	 * The geography of the data set
	 */
	private Geography geography;

	/**
	 * The name of the data set
	 */
	private String name;

	/**
	 * The process allocation method if this data set is a process
	 */
	private ProcessAllocation processAllocation;

	/**
	 * The process type
	 */
	private ProcessType processType;

	/**
	 * The record of the data set
	 */
	private String record;

	/**
	 * The representativeness of the data set
	 */
	private Representativeness representativeness;

	/**
	 * The status of the data set
	 */
	private Status status;

	/**
	 * The substitution method if this data set is a process
	 */
	private Substitution substitution;

	/**
	 * The system boundary of the data set
	 */
	private SystemBoundary systemBoundary;

	/**
	 * A system description entry
	 */
	private SPSystemDescriptionEntry systemDescriptionEntry;

	/**
	 * The technology used by the data set
	 */
	private Technology technology;

	/**
	 * The time period of the data set
	 */
	private TimePeriod timePeriod;

	/**
	 * The verification of the data set
	 */
	private String verification;

	/**
	 * The waste treatment allocation method if this data set is a waste
	 * treatement
	 */
	private WasteTreatmentAllocation wasteTreatmentAllocation;

	/**
	 * Indicates if the data set is an infrastructure data set
	 */
	private boolean infrastructureProcess;

	/**
	 * The literature references of the data set
	 */
	private List<SPLiteratureReferenceEntry> literatureReferenceEntries = new ArrayList<SPLiteratureReferenceEntry>();

	/**
	 * Creates a new documentation
	 * 
	 * @param name
	 *            The name of the data set
	 * @param category
	 *            The category of the data set
	 * @param type
	 *            The type of process
	 */
	public SPDocumentation(String name, ProcessCategory category,
			ProcessType type) {
		this.name = name;
		this.category = category;
		this.processType = type;
	}

	/**
	 * Adds a literature reference entry to the documentation
	 * 
	 * @param entry
	 *            The entry to add
	 */
	public void add(SPLiteratureReferenceEntry entry) {
		this.literatureReferenceEntries.add(entry);
	}

	/**
	 * Getter of the allocation rules
	 * 
	 * @return The allocation rules of the process
	 */
	public String getAllocationRules() {
		return allocationRules;
	}

	/**
	 * Getter of the boundary with nature
	 * 
	 * @see BoundaryWithNature
	 * @return The boundary with nature of the process
	 */
	public BoundaryWithNature getBoundaryWithNature() {
		return boundaryWithNature;
	}

	/**
	 * Getter of the category
	 * 
	 * @see ProcessCategory
	 * @return The category of the process
	 */
	public ProcessCategory getCategory() {
		return category;
	}

	/**
	 * Getter of the collection method
	 * 
	 * @return The collection method of the process data
	 */
	public String getCollectionMethod() {
		return collectionMethod;
	}

	/**
	 * Getter of the comment
	 * 
	 * @return A general comment on the process data
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Getter of the creation date
	 * 
	 * @return The date of creation of the process
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * Getter of the cut off rule
	 * 
	 * @see CutOffRule
	 * @return The cut off rule of the process
	 */
	public CutOffRule getCutOffRule() {
		return cutOffRule;
	}

	/**
	 * Getter of the data treatment
	 * 
	 * @return The data treatment of the process
	 */
	public String getDataTreatment() {
		return dataTreatment;
	}

	/**
	 * Getter of the generator
	 * 
	 * @return A string containing information about the person who
	 *         generated/publicated the data
	 */
	public String getGenerator() {
		return generator;
	}

	/**
	 * Getter of the geography
	 * 
	 * @see Geography
	 * @return The geography of the process
	 */
	public Geography getGeography() {
		return geography;
	}

	/**
	 * Getter of the literature references entries
	 * 
	 * @see SPLiteratureReferenceEntry
	 * @return An array of literature references entries, containting the
	 *         literature reference and an optional additional comment
	 */
	public SPLiteratureReferenceEntry[] getLiteratureReferencesEntries() {
		return literatureReferenceEntries
				.toArray(new SPLiteratureReferenceEntry[literatureReferenceEntries
						.size()]);
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the process
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter of the process allocation
	 * 
	 * @see ProcessAllocation
	 * @return The allocation of the process
	 */
	public ProcessAllocation getProcessAllocation() {
		return processAllocation;
	}

	/**
	 * Getter of the process type
	 * 
	 * @see ProcessType
	 * @return The type of the process
	 */
	public ProcessType getProcessType() {
		return processType;
	}

	/**
	 * Getter of the record
	 * 
	 * @return A string containing information about the person who made the
	 *         data entry
	 */
	public String getRecord() {
		return record;
	}

	/**
	 * Getter of the representativeness
	 * 
	 * @see Representativeness
	 * @return The representativeness of the process
	 */
	public Representativeness getRepresentativeness() {
		return representativeness;
	}

	/**
	 * Getter of the status
	 * 
	 * @see Status
	 * @return The status of the process
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Getter of the substitution
	 * 
	 * @see Substitution
	 * @return The substitution method of the process
	 */
	public Substitution getSubstitution() {
		return substitution;
	}

	/**
	 * Getter of the system boundary
	 * 
	 * @see SystemBoundary
	 * @return The system boundary of the process
	 */
	public SystemBoundary getSystemBoundary() {
		return systemBoundary;
	}

	/**
	 * Getter of the system description entry
	 * 
	 * @see ISystemDescriptionEntry
	 * @return The system description of the data entry wrapped with a general
	 *         comment
	 */
	public SPSystemDescriptionEntry getSystemDescriptionEntry() {
		return systemDescriptionEntry;
	}

	/**
	 * Getter of the technology
	 * 
	 * @see Technology
	 * @return The technology used by the process
	 */
	public Technology getTechnology() {
		return technology;
	}

	/**
	 * Getter of the time period
	 * 
	 * @see TimePeriod
	 * @return The time period of the process
	 */
	public TimePeriod getTimePeriod() {
		return timePeriod;
	}

	/**
	 * Getter of the verification
	 * 
	 * @return The type of verification of the process
	 */
	public String getVerification() {
		return verification;
	}

	/**
	 * Getter of the waste treatment alloctation
	 * 
	 * @see WasteTreatmentAllocation
	 * @return The allocation of the waste treatment
	 */
	public WasteTreatmentAllocation getWasteTreatmentAllocation() {
		return wasteTreatmentAllocation;
	}

	/**
	 * Getter of the infrastructure process value
	 * 
	 * @return true if the process is an infrastructure process, false otherwise
	 */
	public boolean isInfrastructureProcess() {
		return infrastructureProcess;
	}

	/**
	 * Setter of the allocation rules
	 * 
	 * @param allocationRules
	 *            The new allocation rules
	 */
	public void setAllocationRules(String allocationRules) {
		this.allocationRules = allocationRules;
	}

	/**
	 * Setter of the boundary with nature
	 * 
	 * @param boundaryWithNature
	 *            The new boundary with nature
	 */
	public void setBoundaryWithNature(BoundaryWithNature boundaryWithNature) {
		this.boundaryWithNature = boundaryWithNature;
	}

	/**
	 * Setter of the process category
	 * 
	 * @param category
	 *            The new category
	 */
	public void setCategory(ProcessCategory category) {
		this.category = category;
	}

	/**
	 * Setter of the collection method
	 * 
	 * @param collectionMethod
	 *            The new collection method
	 */
	public void setCollectionMethod(String collectionMethod) {
		this.collectionMethod = collectionMethod;
	}

	/**
	 * Setter of the comment
	 * 
	 * @param comment
	 *            The new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Setter of the creation date
	 * 
	 * @param creationDate
	 *            The new creation date
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Setter of the cut off rule
	 * 
	 * @param cutOffRule
	 *            The new cut off rule
	 */
	public void setCutOffRule(CutOffRule cutOffRule) {
		this.cutOffRule = cutOffRule;
	}

	/**
	 * Setter of the data treatment
	 * 
	 * @param dataTreatment
	 *            The new data treatment
	 */
	public void setDataTreatment(String dataTreatment) {
		this.dataTreatment = dataTreatment;
	}

	/**
	 * Setter of the generator
	 * 
	 * @param generator
	 *            The new generator
	 */
	public void setGenerator(String generator) {
		this.generator = generator;
	}

	/**
	 * Setter of the geography
	 * 
	 * @param geography
	 *            The new geography
	 */
	public void setGeography(Geography geography) {
		this.geography = geography;
	}

	/**
	 * Setter of the infrastructure process value
	 * 
	 * @param infrastructureProcess
	 *            The new value of infrastructure process
	 */
	public void setInfrastructureProcess(boolean infrastructureProcess) {
		this.infrastructureProcess = infrastructureProcess;
	}

	/**
	 * Setter of the name
	 * 
	 * @param name
	 *            The new name of the data set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter of the process allocation
	 * 
	 * @param processAllocation
	 *            The new process allocation method
	 */
	public void setProcessAllocation(ProcessAllocation processAllocation) {
		this.processAllocation = processAllocation;
	}

	/**
	 * Setter of the process type
	 * 
	 * @param processType
	 *            The new process type
	 */
	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}

	/**
	 * Setter of the record
	 * 
	 * @param record
	 *            The new record
	 */
	public void setRecord(String record) {
		this.record = record;
	}

	/**
	 * Setter of the representativeness
	 * 
	 * @param representativeness
	 *            The new representativeness
	 */
	public void setRepresentativeness(Representativeness representativeness) {
		this.representativeness = representativeness;
	}

	/**
	 * Setter of the status
	 * 
	 * @param status
	 *            The new status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Setter of the substitution
	 * 
	 * @param substitution
	 *            The new substitution
	 */
	public void setSubstitution(Substitution substitution) {
		this.substitution = substitution;
	}

	/**
	 * Setter of the system boundary
	 * 
	 * @param systemBoundary
	 *            The new system boundary
	 */
	public void setSystemBoundary(SystemBoundary systemBoundary) {
		this.systemBoundary = systemBoundary;
	}

	/**
	 * Setter of the system description entry
	 * 
	 * @param systemDescriptionEntry
	 *            The new system description entry
	 */
	public void setSystemDescriptionEntry(
			SPSystemDescriptionEntry systemDescriptionEntry) {
		this.systemDescriptionEntry = systemDescriptionEntry;
	}

	/**
	 * Setter of the technology
	 * 
	 * @param technology
	 *            The new technology
	 */
	public void setTechnology(Technology technology) {
		this.technology = technology;
	}

	/**
	 * Setter of the time period
	 * 
	 * @param timePeriod
	 *            The new time period
	 */
	public void setTimePeriod(TimePeriod timePeriod) {
		this.timePeriod = timePeriod;
	}

	/**
	 * Setter of the verification
	 * 
	 * @param verification
	 *            The new verification
	 */
	public void setVerification(String verification) {
		this.verification = verification;
	}

	/**
	 * Setter of the waste treatment allocation
	 * 
	 * @param wasteTreatmentAllocation
	 *            The new waste treatment allocation method
	 */
	public void setWasteTreatmentAllocation(
			WasteTreatmentAllocation wasteTreatmentAllocation) {
		this.wasteTreatmentAllocation = wasteTreatmentAllocation;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}
