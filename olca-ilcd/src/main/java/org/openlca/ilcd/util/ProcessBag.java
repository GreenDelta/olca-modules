package org.openlca.ilcd.util;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.Class;
import org.openlca.ilcd.commons.ClassificationInfo;
import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdministrativeInformation;
import org.openlca.ilcd.processes.Completeness;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.ComplianceDeclarationList;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.DataSetInformation;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.ExchangeList;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.LCIMethod;
import org.openlca.ilcd.processes.ModellingAndValidation;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterList;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInformation;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.processes.Validation;
import org.openlca.ilcd.productmodel.ProductModel;

public class ProcessBag implements IBag<Process> {

	private Process process;
	private IlcdConfig config;

	public ProcessBag(Process process, IlcdConfig config) {
		this.process = process;
		this.config = config;
	}

	@Override
	public Process getValue() {
		return process;
	}

	@Override
	public String getId() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getUUID();
		return null;
	}

	public String getName() {
		DataSetInformation info = getDataSetInformation();
		if (info == null || info.getName() == null)
			return null;
		ProcessName processName = info.getName();
		StringBuilder builder = new StringBuilder();
		appendNamePart(processName.getBaseName(), builder, null);
		appendNamePart(processName.getMixAndLocationTypes(), builder, ", ");
		appendNamePart(processName.getTreatmentStandardsRoutes(), builder, ", ");
		appendNamePart(processName.getFunctionalUnitFlowProperties(), builder,
				", ");
		return builder.toString();
	}

	private void appendNamePart(List<Label> parts, StringBuilder builder,
			String prefix) {
		if (parts != null) {
			String part = LangString.get(parts, config);
			if (part != null) {
				if (prefix != null) {
					builder.append(prefix);
				}
				builder.append(part);
			}
		}
	}

	public String getSynonyms() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getSynonyms(), config);
		return null;
	}

	public List<Class> getSortedClasses() {
		DataSetInformation info = getDataSetInformation();
		if (info != null) {
			ClassificationInfo classInfo = info
					.getClassificationInformation();
			return ClassList.sortedList(classInfo);
		}
		return Collections.emptyList();
	}

	public String getComment() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return LangString.get(info.getGeneralComment(), config);
		return null;
	}

	public Time getTime() {
		ProcessInformation info = process.getProcessInformation();
		if (info != null) {
			return info.getTime();
		}
		return null;
	}

	public Geography getGeography() {
		ProcessInformation info = process.getProcessInformation();
		if (info != null)
			return info.getGeography();
		return null;
	}

	public List<BigInteger> getReferenceFlowIds() {
		ProcessInformation info = process.getProcessInformation();
		if (info != null) {
			QuantitativeReference qRef = info.getQuantitativeReference();
			if (qRef != null)
				return qRef.getReferenceToReferenceFlow();
		}
		return Collections.emptyList();
	}

	public Technology getTechnology() {
		ProcessInformation info = process.getProcessInformation();
		if (info != null)
			return info.getTechnology();
		return null;
	}

	public List<Parameter> getParameters() {
		ProcessInformation info = process.getProcessInformation();
		if (info != null) {
			ParameterList list = info.getParameters();
			if (list != null && list.getParameters() != null) {
				return list.getParameters();
			}
		}
		return Collections.emptyList();
	}

	public ProcessType getProcessType() {
		ModellingAndValidation mav = process.getModellingAndValidation();
		if (mav != null) {
			LCIMethod method = mav.getLciMethod();
			if (method != null)
				return method.getProcessType();
		}
		return null;
	}

	public Representativeness getRepresentativeness() {
		ModellingAndValidation mav = process.getModellingAndValidation();
		if (mav != null)
			return mav.getRepresentativeness();
		return null;
	}

	public Completeness getCompleteness() {
		ModellingAndValidation mav = process.getModellingAndValidation();
		if (mav != null)
			return mav.getCompleteness();
		return null;
	}

	public List<Review> getReviews() {
		ModellingAndValidation mav = process.getModellingAndValidation();
		if (mav != null) {
			Validation validation = mav.getValidation();
			if (validation != null && validation.getReview() != null) {
				return validation.getReview();
			}
		}
		return Collections.emptyList();
	}

	public List<ComplianceDeclaration> getComplianceDeclarations() {
		ModellingAndValidation mav = process.getModellingAndValidation();
		if (mav != null) {
			ComplianceDeclarationList list = mav.getComplianceDeclarations();
			if (list != null && list.getComplianceDeclatations() != null) {
				return list.getComplianceDeclatations();
			}
		}
		return Collections.emptyList();
	}

	public CommissionerAndGoal getCommissionerAndGoal() {
		AdministrativeInformation info = process.getAdministrativeInformation();
		if (info != null)
			return info.getCommissionerAndGoal();
		return null;
	}

	public DataGenerator getDataGenerator() {
		AdministrativeInformation info = process.getAdministrativeInformation();
		if (info != null)
			return info.getDataGenerator();
		return null;
	}

	public DataEntry getDataEntry() {
		AdministrativeInformation info = process.getAdministrativeInformation();
		if (info != null)
			return info.getDataEntry();
		return null;
	}

	public Publication getPublication() {
		AdministrativeInformation info = process.getAdministrativeInformation();
		if (info != null)
			return info.getPublication();
		return null;
	}

	public List<Exchange> getExchanges() {
		ExchangeList list = process.getExchanges();
		if (list != null && list.getExchanges() != null)
			return list.getExchanges();
		return Collections.emptyList();
	}

	public LCIMethod getLciMethod() {
		ModellingAndValidation mav = process.getModellingAndValidation();
		if (mav != null)
			return mav.getLciMethod();
		return null;
	}

	private DataSetInformation getDataSetInformation() {
		if (process.getProcessInformation() != null)
			return process.getProcessInformation().getDataSetInformation();
		return null;
	}

	public ProductModel getProductModel() {
		if (process.getProcessInformation() == null)
			return null;
		Other other = process.getProcessInformation().getOther();
		if (other == null)
			return null;
		for (Object extension : other.getAny()) {
			if (extension instanceof ProductModel)
				return (ProductModel) extension;
		}
		return null;
	}

	public List<DataSetReference> getAllSources() {
		return SourceRefCollection.getAll(process, config);
	}

	public boolean hasProductModel() {
		// TODO: check at least one process as reference
		return getProductModel() != null;
	}

}
