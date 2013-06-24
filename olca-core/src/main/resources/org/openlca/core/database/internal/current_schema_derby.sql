-- Database definition for an openLCA database.
-- There is a schema for Derby and MySQL databases. The MySQL schema can be 
-- derived from the Derby schema by doing the following text replacements:
-- 1) 'CLOB(64 K)' with 'TEXT'
-- 2) 'SMALLINT default 0 ' with 'TINYINT default 0'
-- 3) 'BLOB(16 M)' with MEDIUMBLOB

-- DROP DATABASE IF EXISTS openlca;
-- CREATE DATABASE openlca;
-- USE openLCA;

-- current database version
CREATE TABLE openlca_version (

	id VARCHAR(36) NOT NULL,
	version VARCHAR(255),	
	name VARCHAR(255), 
	
	PRIMARY KEY (ID)
);

CREATE TABLE tbl_categories (

	id VARCHAR(36) NOT NULL, 
	name VARCHAR(255), 
	model_type VARCHAR(255), 
	f_parent_category VARCHAR(255),
		
	PRIMARY KEY (id)
);
CREATE INDEX idx_category_parent ON tbl_categories(f_parent_category);


CREATE TABLE tbl_actors (

	id VARCHAR(36) NOT NULL, 
	telefax VARCHAR(255), 
	website VARCHAR(255), 
	address VARCHAR(255), 
	description CLOB(64 K), 
	zipcode VARCHAR(255), 
	name VARCHAR(255), 
	f_category VARCHAR(36), 
	email VARCHAR(255), 
	telephone VARCHAR(255), 
	country VARCHAR(255), 
	city VARCHAR(255), 
	
	PRIMARY KEY (id)
);
CREATE INDEX idx_actor_category ON tbl_actors(f_category);

CREATE TABLE tbl_locations (

	id VARCHAR(36) NOT NULL, 
	description CLOB(64 K), 
	name VARCHAR(255), 
	longitude DOUBLE, 
	latitude DOUBLE, 
	code VARCHAR(255), 
	
	PRIMARY KEY (id)
);


CREATE TABLE tbl_sources (

	id VARCHAR(36) NOT NULL, 
	description CLOB(64 K), 
	f_category VARCHAR(36), 
	name VARCHAR(255), 
	source_year SMALLINT, 
	text_reference CLOB(64 K), 
	doi VARCHAR(255), 
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_source_category ON tbl_sources(f_category);


CREATE TABLE tbl_units (

	id VARCHAR(36) NOT NULL,
	conversion_factor DOUBLE, 
	description CLOB(64 K), 
	name VARCHAR(255),
	synonyms VARCHAR(255),
	f_unit_group VARCHAR(36),
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_unit_unit_group ON tbl_units(f_unit_group);


CREATE TABLE tbl_unit_groups (

	id VARCHAR(36) NOT NULL, 
	name VARCHAR(255), 
	f_category VARCHAR(36), 
	description CLOB(64 K), 
	f_reference_unit VARCHAR(36),
	f_default_flow_property VARCHAR(36), 
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_unit_group_category ON tbl_unit_groups(f_category);
CREATE INDEX idx_unit_group_refunit ON tbl_unit_groups(f_reference_unit);
CREATE INDEX idx_unit_group_flowprop ON tbl_unit_groups(f_default_flow_property);


CREATE TABLE tbl_flow_properties (

	id VARCHAR(36) NOT NULL, 
	name VARCHAR(255), 
	f_category VARCHAR(36), 
	description CLOB(64 K), 
	flow_property_type VARCHAR(255), 
	f_unit_group VARCHAR(36), 
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_flowprop_category ON tbl_flow_properties(f_category);
CREATE INDEX idx_flowprop_unti_group ON tbl_flow_properties(f_unit_group);

CREATE TABLE tbl_flows (

	id VARCHAR(36) NOT NULL, 
	name VARCHAR(255),
	f_category VARCHAR(36), 
	description CLOB(64 K),
	flow_type VARCHAR(255), 
	
	infrastructure_flow SMALLINT default 0, 
	cas_number VARCHAR(255), 
	formula VARCHAR(255), 
	f_reference_flow_property VARCHAR(36), 
	f_location VARCHAR(36), 
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_flow_category ON tbl_flows(f_category);
CREATE INDEX idx_flow_flow_property ON tbl_flows(f_reference_flow_property);
CREATE INDEX idx_flow_location ON tbl_flows(f_location);


CREATE TABLE tbl_flow_property_factors (

	id VARCHAR(36) NOT NULL, 
	conversion_factor DOUBLE, 
	f_flow VARCHAR(36),
	f_flow_property VARCHAR(36),
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_flow_factor_flow ON tbl_flow_property_factors(f_flow);
CREATE INDEX idx_flow_factor_property ON tbl_flow_property_factors(f_flow_property);


CREATE TABLE tbl_processes (

	id VARCHAR(36) NOT NULL, 
	name VARCHAR(255), 
	f_category VARCHAR(36), 
	description CLOB(64 K), 
	process_type VARCHAR(255), 
	allocation_method VARCHAR(255), 	
	f_quantitative_reference VARCHAR(36), 
	f_location VARCHAR(36), 
	f_process_doc VARCHAR(36), 
	
	PRIMARY KEY (id)	

);
CREATE INDEX idx_process_category ON tbl_processes(f_category);
CREATE INDEX idx_process_qref ON tbl_processes(f_quantitative_reference);
CREATE INDEX idx_process_location ON tbl_processes(f_location);


CREATE TABLE tbl_process_docs (
	
	id VARCHAR(36) NOT NULL,
	infrastructure_process SMALLINT default 0, 
	geography CLOB(64 K), 
	technology CLOB(64 K),
	
	time CLOB(64 K),
	valid_from DATE, 
	valid_until DATE, 
	
	modeling_constants CLOB(64 K),
	data_treatment CLOB(64 K), 
	sampling CLOB(64 K), 
	completeness CLOB(64 K),
	review_details CLOB(64 K),
	inventory_method CLOB(64 K), 
	data_collection_period CLOB(64 K), 
	data_selection CLOB(64 K), 
	f_reviewer VARCHAR(36), 
	
	project VARCHAR(255), 
	creation_date DATE, 
	intended_application CLOB(64 K), 
	restrictions CLOB(64 K),
	copyright SMALLINT default 0, 
	last_change DATE, 
	version VARCHAR(255), 
	f_data_generator VARCHAR(36),
	f_dataset_owner VARCHAR(36), 
	f_data_documentor VARCHAR(36), 
	f_publication VARCHAR(36), 
		
	PRIMARY KEY (id)

);


CREATE TABLE tbl_process_sources (

	f_process_doc VARCHAR(36) NOT NULL, 
	f_source VARCHAR(36) NOT NULL,
	
	PRIMARY KEY (f_process_doc, f_source)

);


CREATE TABLE tbl_exchanges (

	id VARCHAR(36) NOT NULL, 
	avoidedproduct SMALLINT default 0,
	distributionType INTEGER default 0, 
	is_input SMALLINT default 0, 
	f_flow_property_factor VARCHAR(36), 
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

CREATE TABLE tbl_product_systems (

	id VARCHAR(36) NOT NULL,
	name VARCHAR(255), 
	description CLOB(64 K), 
	f_category VARCHAR(36),  
	target_amount DOUBLE, 
	f_reference_process VARCHAR(36), 
	f_reference_exchange VARCHAR(36), 
	f_target_flow_property_factor VARCHAR(36), 
	f_target_unit VARCHAR(36), 
		
	PRIMARY KEY (id)
	
);


-- processes in a product system

CREATE TABLE tbl_product_system_processes (

	f_product_system VARCHAR(36) NOT NULL, 
	f_process VARCHAR(36) NOT NULL, 
	
	PRIMARY KEY (f_product_system, f_process)

);


-- process links of product systems

CREATE TABLE tbl_process_links (

	id VARCHAR(36) NOT NULL, 
	f_recipient_process VARCHAR(36), 
	f_recipient_input VARCHAR(36), 
	f_provider_process VARCHAR(36), 
	f_provider_output VARCHAR(36), 
	f_product_system VARCHAR(36), 
	
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

CREATE TABLE tbl_impact_results (

	id VARCHAR(36) NOT NULL, 
	targetamount DOUBLE, 
	product VARCHAR(255),
	productsystem VARCHAR(255), 
	unit VARCHAR(255), 
	impact_method VARCHAR(255), 
	nwset VARCHAR(255), 
	weightingunit VARCHAR(255), 
	description CLOB(64 K), 
	categoryid VARCHAR(255), 
	name VARCHAR(255), 
	
	PRIMARY KEY (id)

);


-- a stored LCIA result

CREATE TABLE tbl_impact_category_results (

	id VARCHAR(36) NOT NULL, 
	category VARCHAR(255), 
	unit VARCHAR(255), 
	weightingunit VARCHAR(255), 
	value double, 
	standarddeviation double, 
	normalizationfactor double, 
	weightingfactor double, 
	f_impact_result VARCHAR(36), 
	
	PRIMARY KEY (id)
);


-- LCIA methods

CREATE TABLE tbl_impact_methods (

	id VARCHAR(36) NOT NULL, 
	description CLOB(64 K), 
	f_category VARCHAR(36), 
	name VARCHAR(255), 
	PRIMARY KEY (id)
	
);


-- LCIA categories

CREATE TABLE tbl_impact_categories (

	id VARCHAR(36) NOT NULL, 
	description CLOB(64 K), 
	name VARCHAR(255), 
	reference_unit VARCHAR(255),
	f_impact_method VARCHAR(36), 
	
	PRIMARY KEY (id)	

);


-- LCIA factors

CREATE TABLE tbl_impact_factors (

	id VARCHAR(36) NOT NULL, 
	f_flow_property_factor VARCHAR(36), 
	f_flow VARCHAR(36), 
	f_unit VARCHAR(36), 
	value DOUBLE, 
	f_impact_category VARCHAR(36), 
	uncertainy_type VARCHAR(50),
	uncertainty_parameter_1 DOUBLE,
	uncertainty_parameter_2 DOUBLE,
	uncertainty_parameter_3 DOUBLE,
	
	PRIMARY KEY (id)

);


-- normalisation and weighting sets of LCIA methods

CREATE TABLE tbl_normalisation_weighting_sets (

	id VARCHAR(255) NOT NULL, 
	reference_system VARCHAR(255),
	f_impact_method VARCHAR(36), 
	unit VARCHAR(255),
	
	PRIMARY KEY (id)

);


-- factors of normalisation and weighting sets of LCIA methods

CREATE TABLE tbl_normalizationweightingfactors (

	id VARCHAR(255) NOT NULL, 
	weightingfactor DOUBLE, 
	normalizationfactor DOUBLE,
	f_impact_category VARCHAR(36),
	f_normalisation_weighting_set VARCHAR(255), 
	
	PRIMARY KEY (id)

);


-- parameters

CREATE TABLE tbl_parameters (

	id VARCHAR(36) NOT NULL, 
	description CLOB(64 K), 
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
	product_systems CLOB(64 K), 
	creation_date DATE, 
	description CLOB(64 K), 
	f_category VARCHAR(36), 
	functional_unit CLOB(64 K), 
	name VARCHAR(255), 
	last_modification_date DATE,
	goal CLOB(64 K), 
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
	description CLOB(64 K),
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
	groups_blob BLOB(16 M),		
	PRIMARY KEY (id)	
) ;

-- the version entry
INSERT INTO openlca_version(id, version, name) 
	VALUES('b3dae112-8c6f-4c0e-9843-4758af2441cc', '1.4.0', 'openLCA');