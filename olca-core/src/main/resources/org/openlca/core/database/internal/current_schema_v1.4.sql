
-- DROP DATABASE IF EXISTS openlca;
-- CREATE DATABASE openlca;
-- USE openLCA;

-- the version of the openLCA client

CREATE TABLE openlca_version (

	id VARCHAR(36) NOT NULL,
	version VARCHAR(255),	
	name VARCHAR(255), 
	eclipse VARCHAR(255), 
	PRIMARY KEY (ID)
	
);


-- the category tree of the database

CREATE TABLE tbl_categories (

	id VARCHAR(255) NOT NULL, 
	name VARCHAR(255), 
	model_type VARCHAR(255), 
	f_parentcategory VARCHAR(255), 
	
	PRIMARY KEY (id)

);


-- actors (= contact data sets) for administrative information

CREATE TABLE tbl_actors (

	id VARCHAR(36) NOT NULL, 
	telefax VARCHAR(255), 
	website VARCHAR(255), 
	address VARCHAR(255), 
	description VARCHAR(32000), 
	zipcode VARCHAR(255), 
	name VARCHAR(255), 
	categoryid VARCHAR(255), 
	email VARCHAR(255), 
	telephone VARCHAR(255), 
	country VARCHAR(255), 
	city VARCHAR(255), 
	
	
	PRIMARY KEY (id)
	
);


-- geographical locations

CREATE TABLE tbl_locations (

	id VARCHAR(36) NOT NULL, 
	description VARCHAR(32000), 
	name VARCHAR(255), 
	longitude DOUBLE, 
	code VARCHAR(255), 
	latitude DOUBLE, 
	
	PRIMARY KEY (id)
	
);


-- data sources for modelling and administrative information of processes

CREATE TABLE tbl_sources (

	id VARCHAR(36) NOT NULL, 
	description VARCHAR(32000), 
	categoryid VARCHAR(36), 
	name VARCHAR(255), 
	source_year SMALLINT, 
	textreference VARCHAR(32000), 
	doi VARCHAR(255), 
	
	PRIMARY KEY (id)
	
);


-- units

CREATE TABLE tbl_units (

	id VARCHAR(36) NOT NULL,
	conversionfactor DOUBLE, 
	description VARCHAR(32000), 
	name VARCHAR(255),
	synonyms VARCHAR(255),
	f_unitgroup VARCHAR(36),
	
	PRIMARY KEY (id)
	
);


-- unit groups 

CREATE TABLE tbl_unitgroups (

	id VARCHAR(36) NOT NULL, 
	description VARCHAR(32000), 
	categoryid VARCHAR(36), 
	name VARCHAR(255), 
	f_referenceunit VARCHAR(36),
	f_defaultflowproperty VARCHAR(36), 
	
	PRIMARY KEY (id),
	CONSTRAINT FK_tbl_unitgroups_f_referenceunit 
		FOREIGN KEY (f_referenceunit) REFERENCES tbl_units (id)
	
);


-- set the reference between units and unit group
ALTER TABLE tbl_units ADD CONSTRAINT FK_tbl_units_f_unitgroup 
	FOREIGN KEY (f_unitgroup) REFERENCES tbl_unitgroups (id);

	
-- flow properties

CREATE TABLE tbl_flowproperties (

	id VARCHAR(36) NOT NULL, 
	flowpropertytype INTEGER, 
	description VARCHAR(32000), 
	unitgroupid VARCHAR(36), 
	categoryid VARCHAR(36), 
	name VARCHAR(255), 
	
	PRIMARY KEY (id)
	
);


-- reference for a default flow property of a unit group
ALTER TABLE tbl_unitgroups ADD CONSTRAINT FK_tbl_unitgroups_f_defaultflowproperty 
	FOREIGN KEY (f_defaultflowproperty) REFERENCES tbl_flowproperties (id);



-- flows (elementary, product, or waste flows)

CREATE TABLE tbl_flows (

	id VARCHAR(36) NOT NULL, 
	flowtype INTEGER, 
	description VARCHAR(32000),
	categoryid VARCHAR(36), 
	name VARCHAR(255),
	
	infrastructure_flow SMALLINT default 0, 
	cas_number VARCHAR(255), 
	formula VARCHAR(255), 
	f_reference_flow_property VARCHAR(36), 
	f_location VARCHAR(36), 
	
	PRIMARY KEY (id)
	
);

-- conversion factors between flow properties related to a flow

CREATE TABLE tbl_flowpropertyfactors (

	id VARCHAR(36) NOT NULL, 
	conversionfactor DOUBLE, 
	f_flowproperty VARCHAR(36),
	f_flowinformation VARCHAR(36),
	
	PRIMARY KEY (id)
	
);


-- processes

CREATE TABLE tbl_processes (

	id VARCHAR(36) NOT NULL, 
	processtype INTEGER, 
	allocationmethod INTEGER, 
	infrastructureprocess SMALLINT default 0, 
	geographycomment VARCHAR(32000), 
	description VARCHAR(32000), 
	name VARCHAR(255), 
	categoryid VARCHAR(36), 
	f_quantitativereference VARCHAR(36), 
	f_location VARCHAR(36), 
	
	PRIMARY KEY (id)	

);

-- process technologies

CREATE TABLE tbl_technologies (
	
	id VARCHAR(36) NOT NULL,
	description VARCHAR(32000),
	
	PRIMARY KEY (id)
	
);


-- valid time spans of processes

CREATE TABLE tbl_times (

	id VARCHAR(36) NOT NULL, 
	startdate DATE, 
	enddate DATE, 
	comment VARCHAR(32000),
	
	PRIMARY KEY (id)
	
);


-- modelling and validation entries of processes

CREATE TABLE tbl_modelingandvalidations (

	id VARCHAR(36) NOT NULL,
	modelingconstants VARCHAR(32000),
	datatreatment VARCHAR(32000), 
	sampling VARCHAR(32000), 
	datacompleteness VARCHAR(32000),
	datasetotherevaluation VARCHAR(32000),
	lcimethod VARCHAR(32000), 
	datacollectionperiod VARCHAR(32000), 
	dataselection VARCHAR(32000), 
	f_reviewer VARCHAR(36), 
	
	PRIMARY KEY (id)
);


-- sources referenced by the modelling and validation section of a process

CREATE TABLE tbl_modelingandvalidation_source (

	f_modelingandvalidation VARCHAR(36) NOT NULL, 
	f_source VARCHAR(36) NOT NULL,
	
	PRIMARY KEY (f_modelingandvalidation, f_source)

);


-- administrative information of processes

CREATE TABLE tbl_admininfos (

	id VARCHAR(36) NOT NULL, 
	project VARCHAR(255), 
	creationdate DATE, 
	intendedapplication VARCHAR(32000), 
	accessanduserestrictions VARCHAR(32000),
	copyright SMALLINT default 0, 
	lastchange DATE, 
	version VARCHAR(255), 
	f_datagenerator VARCHAR(36),
	f_datasetowner VARCHAR(36), 
	f_datadocumentor VARCHAR(36), 
	f_publication VARCHAR(36), 
	
	
	PRIMARY KEY (id)
		
);


-- process / product system inputs and outputs

CREATE TABLE tbl_exchanges (

	id VARCHAR(36) NOT NULL, 
	avoidedproduct SMALLINT default 0,
	distributionType INTEGER default 0, 
	is_input SMALLINT default 0, 
	f_flowpropertyfactor VARCHAR(36), 
	f_unit VARCHAR(36), 
	f_flow VARCHAR(36), 
	parametrized SMALLINT default 0, 
	resultingamount_value DOUBLE, 
	resultingamount_formula VARCHAR(255), 
	parameter1_value DOUBLE, 
	parameter1_formula VARCHAR(255), 
	parameter2_value DOUBLE, 
	parameter2_formula VARCHAR(255), 
	parameter3_value DOUBLE, 
	parameter3_formula VARCHAR(255), 
	f_owner VARCHAR(36), 
	pedigree_uncertainty VARCHAR(50),
	base_uncertainty DOUBLE,
	f_default_provider VARCHAR(36),
	
	PRIMARY KEY (id)
	
);


-- an allocation factor of an allocated process

CREATE TABLE tbl_allocationfactors (

	id VARCHAR(36) NOT NULL, 
	value DOUBLE, 
	productid VARCHAR(36), 
	f_exchange VARCHAR(36), 
	
	PRIMARY KEY (id)
	
);


-- product systems

CREATE TABLE tbl_productsystems (

	id VARCHAR(36) NOT NULL,
	name VARCHAR(255), 
	description VARCHAR(32000), 
	categoryid VARCHAR(36), 
	marked VARCHAR(32000), 
	targetamount DOUBLE, 
	f_referenceprocess VARCHAR(36), 
	f_referenceexchange VARCHAR(36), 
	f_targetflowpropertyfactor VARCHAR(36), 
	f_targetunit VARCHAR(36), 
	
	
	PRIMARY KEY (id)
	
);


-- processes in a product system

CREATE TABLE tbl_productsystem_process (

	f_productsystem VARCHAR(36) NOT NULL, 
	f_process VARCHAR(36) NOT NULL, 
	
	PRIMARY KEY (f_productsystem, f_process)

);


-- process links of product systems

CREATE TABLE tbl_processlinks (

	id VARCHAR(36) NOT NULL, 
	f_recipientprocess VARCHAR(36), 
	f_recipientinput VARCHAR(36), 
	f_providerprocess VARCHAR(36), 
	f_provideroutput VARCHAR(36), 
	f_productsystem VARCHAR(36), 
	
	PRIMARY KEY (id)
	
);


-- the scaling factors of processes of a calculated product system

CREATE TABLE tbl_scalingfactors (

	id VARCHAR(36) NOT NULL, 
	processid VARCHAR(36), 
	factor DOUBLE,
	uncertainty DOUBLE, 
	productid VARCHAR(36),
	f_productsystem VARCHAR(36), 
	
	PRIMARY KEY (id)

);


-- LCI results of product systems

CREATE TABLE tbl_lciresults (

	id VARCHAR(36) NOT NULL, 
	targetamount DOUBLE, 
	product VARCHAR(255),
	productsystem VARCHAR(255), 
	calculationmethod VARCHAR(255), 
	unit VARCHAR(255), 
	PRIMARY KEY (id)
	
);


-- LCIA results of product systems

CREATE TABLE tbl_lciaresults (

	id VARCHAR(36) NOT NULL, 
	targetamount DOUBLE, 
	product VARCHAR(255),
	productsystem VARCHAR(255), 
	unit VARCHAR(255), 
	lciamethod VARCHAR(255), 
	nwset VARCHAR(255), 
	weightingunit VARCHAR(255), 
	description VARCHAR(32000), 
	categoryid VARCHAR(255), 
	name VARCHAR(255), 
	
	PRIMARY KEY (id)

);


-- a stored LCIA result

CREATE TABLE tbl_lciacategoryresults (

	id VARCHAR(36) NOT NULL, 
	category VARCHAR(255), 
	unit VARCHAR(255), 
	weightingunit VARCHAR(255), 
	value double, 
	standarddeviation double, 
	normalizationfactor double, 
	weightingfactor double, 
	f_lciaresult VARCHAR(36), 
	
	PRIMARY KEY (id)
);


-- LCIA methods

CREATE TABLE tbl_lciamethods (

	id VARCHAR(36) NOT NULL, 
	description VARCHAR(32000), 
	categoryid VARCHAR(36), 
	name VARCHAR(255), 
	PRIMARY KEY (id)
	
);


-- LCIA categories

CREATE TABLE tbl_lciacategories (

	id VARCHAR(36) NOT NULL, 
	description VARCHAR(32000), 
	name VARCHAR(255), 
	referenceunit VARCHAR(255),
	f_lciamethod VARCHAR(36), 
	
	PRIMARY KEY (id)	

);


-- LCIA factors

CREATE TABLE tbl_lciafactors (

	id VARCHAR(36) NOT NULL, 
	f_flowpropertyfactor VARCHAR(36), 
	f_flow VARCHAR(36), 
	f_unit VARCHAR(36), 
	value DOUBLE, 
	f_lciacategory VARCHAR(36), 
	uncertainy_type VARCHAR(50),
	uncertainty_parameter_1 DOUBLE,
	uncertainty_parameter_2 DOUBLE,
	uncertainty_parameter_3 DOUBLE,
	
	PRIMARY KEY (id)

);


-- normalisation and weighting sets of LCIA methods

CREATE TABLE tbl_normalizationweightingsets (

	id VARCHAR(255) NOT NULL, 
	referencesystem VARCHAR(255),
	f_lciamethod VARCHAR(36), 
	unit VARCHAR(255),
	
	PRIMARY KEY (id)

);


-- factors of normalisation and weighting sets of LCIA methods

CREATE TABLE tbl_normalizationweightingfactors (

	id VARCHAR(255) NOT NULL, 
	weightingfactor DOUBLE, 
	normalizationfactor DOUBLE,
	f_lciacategory VARCHAR(36),
	f_normalizationweightingset VARCHAR(255), 
	
	PRIMARY KEY (id)

);


-- parameters

CREATE TABLE tbl_parameters (

	id VARCHAR(36) NOT NULL, 
	description VARCHAR(32000), 
	name VARCHAR(255), 
	f_owner VARCHAR(36), 
	type INTEGER, 
	expression_parametrized SMALLINT default 0, 
	expression_value DOUBLE, 
	expression_formula VARCHAR(255),
	
	PRIMARY KEY (id)
);


-- projects

CREATE TABLE tbl_projects (

	id VARCHAR(36) NOT NULL, 
	productsystems VARCHAR(32000), 
	creationdate DATE, 
	description VARCHAR(32000), 
	categoryid VARCHAR(36), 
	functionalunit VARCHAR(32000), 
	name VARCHAR(255), 
	lastmodificationdate DATE,
	goal VARCHAR(32000), 
	f_author VARCHAR(36), 
	
	PRIMARY KEY (id)	
);

CREATE TABLE tbl_mappings (
	id VARCHAR(50) NOT NULL,
	map_type VARCHAR(50),	
	format VARCHAR(50),	
	external_key VARCHAR(255),	
	external_name VARCHAR(255),	
	olca_id VARCHAR(36),	
	factor DOUBLE,		
	PRIMARY KEY (id)	
);

CREATE TABLE tbl_cost_categories (	
	id VARCHAR(36) NOT NULL,
	name VARCHAR(255),
	description VARCHAR(32000),
	fix BOOLEAN DEFAULT FALSE,
	PRIMARY KEY (id)
) ;

CREATE TABLE tbl_product_cost_entries (
	id VARCHAR(36) NOT NULL,
	f_process VARCHAR(36),
	f_exchange VARCHAR(36),
	f_cost_category VARCHAR(36),
	amount DOUBLE,	
	PRIMARY KEY (id)
) ;

CREATE TABLE tbl_process_group_sets (
	id VARCHAR(36) NOT NULL,
	name VARCHAR(255), 
	groups_blob BLOB,		
	PRIMARY KEY (id)	
) ;

-- the version entry
INSERT INTO openlca_version(id, version, name, eclipse) 
	VALUES('b3dae112-8c6f-4c0e-9843-4758af2441cc', 
	'1.3.0', 'openLCA', 'Juno');