package org.openlca.ecospold;

import javax.xml.transform.Source;

import org.openlca.ecospold.io.DataSet;

public interface IEcoSpoldFactory {

	/**
	 * Create an instance of {@link EcoSpold }
	 * 
	 */
	IEcoSpold createEcoSpold();

	/**
	 * Create an instance of {@link DataEntryBy }
	 * 
	 */
	IDataEntryBy createDataEntryBy();

	/**
	 * Create an instance of {@link ModellingAndValidation }
	 * 
	 */
	IModellingAndValidation createModellingAndValidation();

	/**
	 * Create an instance of {@link Allocation }
	 * 
	 */
	IAllocation createAllocation();

	/**
	 * Create an instance of {@link Technology }
	 * 
	 */
	ITechnology createTechnology();

	/**
	 * Create an instance of {@link Person }
	 * 
	 */
	IPerson createPerson();

	/**
	 * Create an instance of {@link Exchange }
	 * 
	 */
	IExchange createExchange();

	/**
	 * Create an instance of {@link ReferenceFunction }
	 * 
	 */
	IReferenceFunction createReferenceFunction();

	/**
	 * Create an instance of {@link ProcessInformation }
	 * 
	 */
	IProcessInformation createProcessInformation();

	/**
	 * Create an instance of {@link Validation }
	 * 
	 */
	IValidation createValidation();

	/**
	 * Create an instance of {@link AdministrativeInformation }
	 * 
	 */
	IAdministrativeInformation createAdministrativeInformation();

	/**
	 * Create an instance of {@link DataGeneratorAndPublication }
	 * 
	 */
	IDataGeneratorAndPublication createDataGeneratorAndPublication();

	/**
	 * Create an instance of {@link DataSet }
	 * 
	 */
	IDataSet createDataSet();

	/**
	 * Create an instance of {@link DataSetInformation }
	 * 
	 */
	IDataSetInformation createDataSetInformation();

	/**
	 * Create an instance of {@link Source }
	 * 
	 */
	ISource createSource();

	/**
	 * Create an instance of {@link MetaInformation }
	 * 
	 */
	IMetaInformation createMetaInformation();

	/**
	 * Create an instance of {@link TimePeriod }
	 * 
	 */
	ITimePeriod createTimePeriod();

	/**
	 * Create an instance of {@link Representativeness }
	 * 
	 */
	IRepresentativeness createRepresentativeness();

	/**
	 * Create an instance of {@link Geography }
	 * 
	 */
	IGeography createGeography();

	/**
	 * Create an instance of {@link FlowData }
	 * 
	 */
	IFlowData createFlowData();

	/**
	 * Returns an instance of country code for the given string if this country
	 * code is defined. Otherwise null is returned.
	 */
	ICountryCode getCountryCode(String code);

	/**
	 * Returns an instance of language code for the given string if this
	 * language code is defined. Otherwise null is returned.
	 */
	ILanguageCode getLanguageCode(String code);
}
