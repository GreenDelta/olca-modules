package org.openlca.ecospold.io;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ecospold.IAdministrativeInformation;
import org.openlca.ecospold.IAllocation;
import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IFlowData;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.IModellingAndValidation;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IProcessInformation;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.IRepresentativeness;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.ITechnology;
import org.openlca.ecospold.ITimePeriod;
import org.openlca.ecospold.IValidation;

/**
 * An adapter class for EcoSpold 01 data sets which provides an easy access to
 * internal elements of data sets and ensures that 'path' elements are created
 * when such internal elements are set.
 */
public class DataSet {

	private final IDataSet dataSet;
	private final IEcoSpoldFactory factory;

	public DataSet(IDataSet dataSet, IEcoSpoldFactory factory) {
		this.dataSet = dataSet;
		this.factory = factory;
		if (dataSet.getFlowData().size() > 1)
			aggregateFlowData();
		if (dataSet.getFlowData().size() == 0)
			dataSet.getFlowData().add(factory.createFlowData());
	}

	// to avoid different lists when aggregating all exchange lists from the
	// different flow data, all exchanges (and allocations) will be appended to
	// one new flow data object (this is just to ensure safe modifications on
	// the lists, since no file is known where several flow data instances are
	// used anyway)
	private void aggregateFlowData() {
		IFlowData flowData = factory.createFlowData();
		for (IFlowData fd : dataSet.getFlowData()) {
			flowData.getExchange().addAll(fd.getExchange());
			flowData.getAllocation().addAll(fd.getAllocation());
		}
		dataSet.getFlowData().clear();
		dataSet.getFlowData().add(flowData);
	}

	private void ensureAdministrativeInformationExists() {
		ensureMetaInformationExists();
		if (getAdministrativeInformation() == null) {
			dataSet.getMetaInformation().setAdministrativeInformation(
					factory.createAdministrativeInformation());
		}
	}

	private void ensureModellingAndValidationExists() {
		ensureMetaInformationExists();
		if (getModellingAndValidation() == null) {
			dataSet.getMetaInformation().setModellingAndValidation(
					factory.createModellingAndValidation());
		}
	}

	private void ensureProcessInformationExists() {
		ensureMetaInformationExists();
		if (getProcessInformation() == null) {
			dataSet.getMetaInformation().setProcessInformation(
					factory.createProcessInformation());
		}
	}

	private void ensureMetaInformationExists() {
		if (dataSet.getMetaInformation() == null) {
			dataSet.setMetaInformation(factory.createMetaInformation());
		}
	}

	private IAdministrativeInformation getAdministrativeInformation() {
		IAdministrativeInformation administrativeInformation = null;
		if (dataSet.getMetaInformation() != null) {
			administrativeInformation = dataSet.getMetaInformation()
					.getAdministrativeInformation();
		}
		return administrativeInformation;
	}

	private IModellingAndValidation getModellingAndValidation() {
		IModellingAndValidation modellingAndValidation = null;
		if (dataSet.getMetaInformation() != null) {
			modellingAndValidation = dataSet.getMetaInformation()
					.getModellingAndValidation();
		}
		return modellingAndValidation;
	}

	private IProcessInformation getProcessInformation() {
		IProcessInformation processInformation = null;
		if (dataSet.getMetaInformation() != null) {
			processInformation = dataSet.getMetaInformation()
					.getProcessInformation();
		}
		return processInformation;
	}

	IDataSet getDataSet() {
		return dataSet;
	}

	public IDataEntryBy getDataEntryBy() {
		IDataEntryBy dataEntryBy = null;
		if (getAdministrativeInformation() != null) {
			dataEntryBy = getAdministrativeInformation().getDataEntryBy();
		}
		return dataEntryBy;
	}

	public void setDataEntryBy(IDataEntryBy value) {
		ensureAdministrativeInformationExists();
		getAdministrativeInformation().setDataEntryBy(value);
	}

	public IDataGeneratorAndPublication getDataGeneratorAndPublication() {
		IDataGeneratorAndPublication dataGeneratorAndPublication = null;
		if (getAdministrativeInformation() != null) {
			dataGeneratorAndPublication = getAdministrativeInformation()
					.getDataGeneratorAndPublication();
		}
		return dataGeneratorAndPublication;
	}

	public void setDataGeneratorAndPublication(
			IDataGeneratorAndPublication value) {
		ensureAdministrativeInformationExists();
		getAdministrativeInformation().setDataGeneratorAndPublication(value);
	}

	public IDataSetInformation getDataSetInformation() {
		IDataSetInformation dataSetInformation = null;
		if (getProcessInformation() != null) {
			dataSetInformation = getProcessInformation()
					.getDataSetInformation();
		}
		return dataSetInformation;
	}

	public void setDataSetInformation(IDataSetInformation value) {
		ensureProcessInformationExists();
		getProcessInformation().setDataSetInformation(value);
	}

	public List<IExchange> getExchanges() {
		// list size of 1 is ensured and contains all exchanges
		// see #initialize
		return dataSet.getFlowData().get(0).getExchange();
	}

	public List<IAllocation> getAllocations() {
		// list size of 1 is ensured and contains all allocations
		// see #initialize
		return dataSet.getFlowData().get(0).getAllocation();
	}

	public String getGenerator() {
		return dataSet.getGenerator();
	}

	public IGeography getGeography() {
		IGeography geography = null;
		if (getProcessInformation() != null) {
			geography = getProcessInformation().getGeography();
		}
		return geography;
	}

	public void setGeography(IGeography value) {
		ensureProcessInformationExists();
		getProcessInformation().setGeography(value);
	}

	public String getInternalSchemaVersion() {
		return dataSet.getInternalSchemaVersion();
	}

	public int getNumber() {
		return dataSet.getNumber();
	}

	public List<IPerson> getPersons() {
		ensureAdministrativeInformationExists();

		List<IPerson> persons = null;
		if (getAdministrativeInformation() != null) {
			persons = getAdministrativeInformation().getPerson();
		}
		return persons;
	}

	public IReferenceFunction getReferenceFunction() {
		IReferenceFunction referenceFunction = null;
		if (getProcessInformation() != null) {
			referenceFunction = getProcessInformation().getReferenceFunction();
		}
		return referenceFunction;
	}

	public void setReferenceFunction(IReferenceFunction value) {
		ensureProcessInformationExists();
		getProcessInformation().setReferenceFunction(value);
	}

	public IRepresentativeness getRepresentativeness() {
		IRepresentativeness representativeness = null;
		if (getModellingAndValidation() != null) {
			representativeness = getModellingAndValidation()
					.getRepresentativeness();
		}
		return representativeness;
	}

	public void setRepresentativeness(IRepresentativeness value) {
		ensureModellingAndValidationExists();
		getModellingAndValidation().setRepresentativeness(value);
	}

	/**
	 * Returns the sources under modelling and validation. The returned list is
	 * guaranteed to be never NULL.
	 */
	public List<ISource> getSources() {
		ensureModellingAndValidationExists();
		return getModellingAndValidation().getSource();
	}

	public ITechnology getTechnology() {
		ITechnology technology = null;
		if (getProcessInformation() != null
				&& getProcessInformation().getTechnology() != null) {
			technology = getProcessInformation().getTechnology();
		}
		return technology;
	}

	public void setTechnology(ITechnology value) {
		ensureProcessInformationExists();
		getProcessInformation().setTechnology(value);
	}

	public ITimePeriod getTimePeriod() {
		ITimePeriod timePeriod = null;
		if (getProcessInformation() != null) {
			timePeriod = getProcessInformation().getTimePeriod();
		}
		return timePeriod;
	}

	public void setTimePeriod(ITimePeriod value) {
		ensureProcessInformationExists();
		getProcessInformation().setTimePeriod(value);
	}

	public XMLGregorianCalendar getTimestamp() {
		return dataSet.getTimestamp();
	}

	public IValidation getValidation() {
		IValidation validation = null;
		if (getModellingAndValidation() != null) {
			validation = getModellingAndValidation().getValidation();
		}
		return validation;
	}

	public void setValidation(IValidation value) {
		ensureModellingAndValidationExists();
		getModellingAndValidation().setValidation(value);
	}

	public String getValidCategories() {
		return dataSet.getValidCategories();
	}

	public String getValidCompanyCodes() {
		return dataSet.getValidCompanyCodes();
	}

	public String getValidRegionalCodes() {
		return dataSet.getValidRegionalCodes();
	}

	public String getValidUnits() {
		return dataSet.getValidUnits();
	}

	public void setGenerator(final String value) {
		dataSet.setGenerator(value);
	}

	public void setInternalSchemaVersion(final String value) {
		dataSet.setInternalSchemaVersion(value);
	}

	public void setNumber(final int value) {
		dataSet.setNumber(value);
	}

	public void setTimestamp(final XMLGregorianCalendar value) {
		dataSet.setTimestamp(value);
	}

	public void setValidCategories(final String value) {
		dataSet.setValidCategories(value);
	}

	public void setValidCompanyCodes(final String value) {
		dataSet.setValidCompanyCodes(value);
	}

	public void setValidRegionalCodes(final String value) {
		dataSet.setValidRegionalCodes(value);
	}

	public void setValidUnits(final String value) {
		dataSet.setValidUnits(value);
	}
}
