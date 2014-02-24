package org.openlca.simapro.csv.model;

import java.util.ArrayList;
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

public class SPProcessDocumentation {

	private String identifier;
	private String allocationRules;
	private BoundaryWithNature boundaryWithNature;
	private ProcessCategory category;
	private String collectionMethod;
	private String comment;
	private String creationDate;
	private CutOffRule cutOffRule;
	private String dataTreatment;
	private String generator;
	private Geography geography;
	private String name;
	private ProcessAllocation processAllocation;
	private ProcessType processType;
	private String record;
	private Representativeness representativeness;
	private Status status;
	private Substitution substitution;
	private SystemBoundary systemBoundary;
	private SPSystemDescriptionEntry systemDescriptionEntry;
	private Technology technology;
	private TimePeriod timePeriod;
	private String verification;
	private WasteTreatmentAllocation wasteTreatmentAllocation;
	private boolean infrastructureProcess;
	private List<SPLiteratureReferenceEntry> literatureReferenceEntries = new ArrayList<>();

	public SPProcessDocumentation(String name, ProcessCategory category,
			ProcessType type) {
		this.name = name;
		this.category = category;
		this.processType = type;
	}

	public String getAllocationRules() {
		return allocationRules;
	}

	public BoundaryWithNature getBoundaryWithNature() {
		return boundaryWithNature;
	}

	public ProcessCategory getCategory() {
		return category;
	}

	public String getCollectionMethod() {
		return collectionMethod;
	}

	public String getComment() {
		return comment;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public CutOffRule getCutOffRule() {
		return cutOffRule;
	}

	public String getDataTreatment() {
		return dataTreatment;
	}

	public String getGenerator() {
		return generator;
	}

	public Geography getGeography() {
		return geography;
	}

	public List<SPLiteratureReferenceEntry> getLiteratureReferenceEntries() {
		return literatureReferenceEntries;
	}

	public String getName() {
		return name;
	}

	public ProcessAllocation getProcessAllocation() {
		return processAllocation;
	}

	public ProcessType getProcessType() {
		return processType;
	}

	public String getRecord() {
		return record;
	}

	public Representativeness getRepresentativeness() {
		return representativeness;
	}

	public Status getStatus() {
		return status;
	}

	public Substitution getSubstitution() {
		return substitution;
	}

	public SystemBoundary getSystemBoundary() {
		return systemBoundary;
	}

	public SPSystemDescriptionEntry getSystemDescriptionEntry() {
		return systemDescriptionEntry;
	}

	public Technology getTechnology() {
		return technology;
	}

	public TimePeriod getTimePeriod() {
		return timePeriod;
	}

	public String getVerification() {
		return verification;
	}

	public WasteTreatmentAllocation getWasteTreatmentAllocation() {
		return wasteTreatmentAllocation;
	}

	public boolean isInfrastructureProcess() {
		return infrastructureProcess;
	}

	public void setAllocationRules(String allocationRules) {
		this.allocationRules = allocationRules;
	}

	public void setBoundaryWithNature(BoundaryWithNature boundaryWithNature) {
		this.boundaryWithNature = boundaryWithNature;
	}

	public void setCategory(ProcessCategory category) {
		this.category = category;
	}

	public void setCollectionMethod(String collectionMethod) {
		this.collectionMethod = collectionMethod;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public void setCutOffRule(CutOffRule cutOffRule) {
		this.cutOffRule = cutOffRule;
	}

	public void setDataTreatment(String dataTreatment) {
		this.dataTreatment = dataTreatment;
	}

	public void setGenerator(String generator) {
		this.generator = generator;
	}

	public void setGeography(Geography geography) {
		this.geography = geography;
	}

	public void setInfrastructureProcess(boolean infrastructureProcess) {
		this.infrastructureProcess = infrastructureProcess;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProcessAllocation(ProcessAllocation processAllocation) {
		this.processAllocation = processAllocation;
	}

	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public void setRepresentativeness(Representativeness representativeness) {
		this.representativeness = representativeness;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setSubstitution(Substitution substitution) {
		this.substitution = substitution;
	}

	public void setSystemBoundary(SystemBoundary systemBoundary) {
		this.systemBoundary = systemBoundary;
	}

	public void setSystemDescriptionEntry(
			SPSystemDescriptionEntry systemDescriptionEntry) {
		this.systemDescriptionEntry = systemDescriptionEntry;
	}

	public void setTechnology(Technology technology) {
		this.technology = technology;
	}

	public void setTimePeriod(TimePeriod timePeriod) {
		this.timePeriod = timePeriod;
	}

	public void setVerification(String verification) {
		this.verification = verification;
	}

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
