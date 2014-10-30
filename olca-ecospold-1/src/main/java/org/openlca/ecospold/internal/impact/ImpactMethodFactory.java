package org.openlca.ecospold.internal.impact;

import org.openlca.ecospold.IAdministrativeInformation;
import org.openlca.ecospold.IAllocation;
import org.openlca.ecospold.ICountryCode;
import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IFlowData;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.ILanguageCode;
import org.openlca.ecospold.IMetaInformation;
import org.openlca.ecospold.IModellingAndValidation;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IProcessInformation;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.IRepresentativeness;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.ITechnology;
import org.openlca.ecospold.ITimePeriod;
import org.openlca.ecospold.IValidation;

public class ImpactMethodFactory implements IEcoSpoldFactory {

	private ObjectFactory objectFactory = new ObjectFactory();

	@Override
	public IEcoSpold createEcoSpold() {
		return objectFactory.createImpactEcoSpold();
	}

	@Override
	public IDataEntryBy createDataEntryBy() {
		return objectFactory.createImpactDataEntryBy();
	}

	@Override
	public IModellingAndValidation createModellingAndValidation() {
		return objectFactory.createImpactModellingAndValidation();
	}

	@Override
	public IAllocation createAllocation() {
		return objectFactory.createImpactAllocation();
	}

	@Override
	public ITechnology createTechnology() {
		return objectFactory.createImpactTechnology();
	}

	@Override
	public IPerson createPerson() {
		return objectFactory.createImpactPerson();
	}

	@Override
	public IExchange createExchange() {
		return objectFactory.createImpactFactor();
	}

	@Override
	public IReferenceFunction createReferenceFunction() {
		return objectFactory.createImpactReferenceFunction();
	}

	@Override
	public IProcessInformation createProcessInformation() {
		return objectFactory.createImpactProcessInformation();
	}

	@Override
	public IValidation createValidation() {
		return objectFactory.createImpactValidation();
	}

	@Override
	public IAdministrativeInformation createAdministrativeInformation() {
		return objectFactory.createImpactAdministrativeInformation();
	}

	@Override
	public IDataGeneratorAndPublication createDataGeneratorAndPublication() {
		return objectFactory.createImpactDataGeneratorAndPublication();
	}

	@Override
	public IDataSet createDataSet() {
		return objectFactory.createImpactDataSet();
	}

	@Override
	public IDataSetInformation createDataSetInformation() {
		return objectFactory.createImpactDataSetInformation();
	}

	@Override
	public ISource createSource() {
		return objectFactory.createImpactSource();
	}

	@Override
	public IMetaInformation createMetaInformation() {
		return objectFactory.createImpactMetaInformation();
	}

	@Override
	public ITimePeriod createTimePeriod() {
		return objectFactory.createImpactTimePeriod();
	}

	@Override
	public IRepresentativeness createRepresentativeness() {
		return objectFactory.createImpactRepresentativeness();
	}

	@Override
	public IGeography createGeography() {
		return objectFactory.createImpactGeography();
	}

	@Override
	public IFlowData createFlowData() {
		return objectFactory.createImpactFactors();
	}

	@Override
	public ICountryCode getCountryCode(String code) {
		if (code == null)
			return null;
		for (ImpactCountryCode countryCode : ImpactCountryCode.values()) {
			if (countryCode.name().equalsIgnoreCase(code))
				return countryCode;
		}
		return null;
	}

	@Override
	public ILanguageCode getLanguageCode(String code) {
		return ImpactLanguageCode.fromValue(code);
	}

}
