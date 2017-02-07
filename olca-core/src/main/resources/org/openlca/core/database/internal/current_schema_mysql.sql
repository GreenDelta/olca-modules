-- Database definition for an openLCA database.
-- There is a schema for Derby and MySQL databases. The MySQL schema can be 
-- derived from the Derby schema by doing the following text replacements:
-- 1) 'CLOB(64 K)' with 'TEXT'
-- 2) 'SMALLINT default 0' with 'TINYINT default 0'
-- 3) 'BLOB(16 M)' with MEDIUMBLOB

-- DROP DATABASE IF EXISTS openlca;
-- CREATE DATABASE openlca;
-- USE openlca;


CREATE TABLE SEQUENCE (	
	SEQ_NAME VARCHAR(255) NOT NULL,
	SEQ_COUNT BIGINT
);
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) VALUES('entity_seq', 0);


CREATE TABLE openlca_version (
	
	version SMALLINT	
	
);
INSERT INTO openlca_version (version) VALUES (6);


CREATE TABLE tbl_categories (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36), 
	name VARCHAR(255),
	description TEXT,
	version BIGINT,
	last_change BIGINT,
	
	model_type VARCHAR(255), 
	f_category BIGINT,
		
	PRIMARY KEY (id)
);
CREATE INDEX idx_category_parent ON tbl_categories(f_category);
CREATE INDEX idx_category_ref_id ON tbl_categories(ref_id);

CREATE TABLE tbl_actors (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36), 
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,

	telefax VARCHAR(255),
	website VARCHAR(255),
	address VARCHAR(255),
	zip_code VARCHAR(255),
	email VARCHAR(255),
	telephone VARCHAR(255), 
	country VARCHAR(255), 
	city VARCHAR(255), 
	
	PRIMARY KEY (id)
);
CREATE INDEX idx_actor_category ON tbl_actors(f_category);
CREATE INDEX idx_actor_ref_id ON tbl_actors(ref_id);

CREATE TABLE tbl_locations (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	description TEXT,
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,

	longitude DOUBLE,
	latitude DOUBLE, 
	code VARCHAR(255),
    kmz MEDIUMBLOB,
	
	PRIMARY KEY (id)
);
CREATE INDEX idx_location_category ON tbl_locations(f_category);
CREATE INDEX idx_location_ref_id ON tbl_locations(ref_id);

CREATE TABLE tbl_sources (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category  BIGINT,
	description TEXT,

	source_year SMALLINT,
	text_reference TEXT, 
	url VARCHAR(255),
	external_file VARCHAR(255),
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_source_category ON tbl_sources(f_category);
CREATE INDEX idx_source_ref_id ON tbl_sources(ref_id);

CREATE TABLE tbl_units (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	description TEXT,
	version BIGINT,
	last_change BIGINT,

	conversion_factor DOUBLE,
	synonyms VARCHAR(255),
	f_unit_group BIGINT,
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_unit_unit_group ON tbl_units(f_unit_group);
CREATE INDEX idx_unit_ref_id ON tbl_units(ref_id);


CREATE TABLE tbl_unit_groups (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,

	f_reference_unit BIGINT,
	f_default_flow_property BIGINT, 
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_unit_group_category ON tbl_unit_groups(f_category);
CREATE INDEX idx_unit_group_refunit ON tbl_unit_groups(f_reference_unit);
CREATE INDEX idx_unit_group_flowprop ON tbl_unit_groups(f_default_flow_property);
CREATE INDEX idx_unit_group_ref_id ON tbl_unit_groups(ref_id);

CREATE TABLE tbl_flow_properties (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,

	flow_property_type VARCHAR(255), 
	f_unit_group BIGINT, 
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_flowprop_category ON tbl_flow_properties(f_category);
CREATE INDEX idx_flowprop_unti_group ON tbl_flow_properties(f_unit_group);
CREATE INDEX idx_flowprop_ref_id ON tbl_flow_properties(ref_id);

CREATE TABLE tbl_flows (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	synonyms VARCHAR(32672),
	description TEXT,

	flow_type VARCHAR(255), 
	infrastructure_flow TINYINT default 0,
	cas_number VARCHAR(255), 
	formula VARCHAR(255), 
	f_reference_flow_property BIGINT, 
	f_location BIGINT, 
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_flow_category ON tbl_flows(f_category);
CREATE INDEX idx_flow_flow_property ON tbl_flows(f_reference_flow_property);
CREATE INDEX idx_flow_location ON tbl_flows(f_location);
CREATE INDEX idx_flow_ref_id ON tbl_flows(ref_id);

CREATE TABLE tbl_flow_property_factors (

	id BIGINT NOT NULL, 
	conversion_factor DOUBLE, 
	f_flow BIGINT,
	f_flow_property BIGINT,
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_flow_factor_flow ON tbl_flow_property_factors(f_flow);
CREATE INDEX idx_flow_factor_property ON tbl_flow_property_factors(f_flow_property);


CREATE TABLE tbl_processes (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,

	process_type VARCHAR(255), 
	default_allocation_method VARCHAR(255), 	
	infrastructure_process TINYINT default 0, 
	f_quantitative_reference BIGINT, 
	f_location BIGINT, 
	f_process_doc BIGINT, 
	f_currency BIGINT,
	f_dq_system BIGINT,
	dq_entry VARCHAR(50),
	f_exchange_dq_system BIGINT,
	f_social_dq_system BIGINT,

	PRIMARY KEY (id)	

);
CREATE INDEX idx_process_category ON tbl_processes(f_category);
CREATE INDEX idx_process_qref ON tbl_processes(f_quantitative_reference);
CREATE INDEX idx_process_location ON tbl_processes(f_location);
CREATE INDEX idx_process_ref_id ON tbl_processes(ref_id);


CREATE TABLE tbl_process_docs (
	
	id BIGINT NOT NULL,
	geography TEXT, 
	technology TEXT,
	
	time TEXT,
	valid_from DATE, 
	valid_until DATE, 
	
	modeling_constants TEXT,
	data_treatment TEXT, 
	sampling TEXT, 
	completeness TEXT,
	review_details TEXT,
	inventory_method TEXT, 
	data_collection_period TEXT, 
	data_selection TEXT, 
	f_reviewer BIGINT, 
	
	project TEXT, 
	creation_date TIMESTAMP, 
	intended_application TEXT, 
	restrictions TEXT,
	copyright TINYINT default 0, 
	f_data_generator BIGINT,
	f_dataset_owner BIGINT, 
	f_data_documentor BIGINT, 
	f_publication BIGINT, 
		
	PRIMARY KEY (id)

);


CREATE TABLE tbl_process_sources (
	f_process_doc BIGINT, 
	f_source BIGINT
);


CREATE TABLE tbl_exchanges (

	id BIGINT NOT NULL, 
	f_owner BIGINT, 
	f_flow BIGINT, 
	f_unit BIGINT, 
	is_input TINYINT default 0, 
	f_flow_property_factor BIGINT, 
	resulting_amount_value DOUBLE, 
	resulting_amount_formula VARCHAR(1000), 
	avoided_product TINYINT default 0,
	f_default_provider BIGINT,
	description TEXT,
	
	cost_value DOUBLE,
	cost_formula VARCHAR(1000),
	f_currency BIGINT,
	
	distribution_type INTEGER default 0, 
	parameter1_value DOUBLE, 
	parameter1_formula VARCHAR(1000), 
	parameter2_value DOUBLE, 
	parameter2_formula VARCHAR(1000), 
	parameter3_value DOUBLE, 
	parameter3_formula VARCHAR(1000), 
	
	dq_entry VARCHAR(50),
	base_uncertainty DOUBLE,
	
	PRIMARY KEY (id)
	
);
CREATE INDEX idx_exchange_process ON tbl_exchanges(f_owner);
CREATE INDEX idx_exchange_flow ON tbl_exchanges(f_flow);


CREATE TABLE tbl_allocation_factors (

	id BIGINT NOT NULL, 
	allocation_type VARCHAR(255),
	value DOUBLE, 
	f_process BIGINT,
	f_product BIGINT, 
	f_exchange BIGINT, 
	
	PRIMARY KEY (id)
	
);


CREATE TABLE tbl_product_systems (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,

	cutoff DOUBLE,
	target_amount DOUBLE,
	f_reference_process BIGINT, 
	f_reference_exchange BIGINT, 
	f_target_flow_property_factor BIGINT, 
	f_target_unit BIGINT, 
		
	PRIMARY KEY (id)
	
);


CREATE TABLE tbl_product_system_processes (

	f_product_system BIGINT NOT NULL, 
	f_process BIGINT NOT NULL, 
	
	PRIMARY KEY (f_product_system, f_process)

);


CREATE TABLE tbl_process_links (

	f_product_system  BIGINT, 
	f_provider        BIGINT, 
	f_flow            BIGINT, 
	f_process         BIGINT,
	f_exchange        BIGINT
	
);
CREATE INDEX idx_process_link_system ON tbl_process_links(f_product_system);


CREATE TABLE tbl_impact_methods (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,

	PRIMARY KEY (id)
	
);


CREATE TABLE tbl_impact_categories (

	id BIGINT NOT NULL, 
	ref_id VARCHAR(36),
	name VARCHAR(255),
	description TEXT,
	version BIGINT,
	last_change BIGINT,

	reference_unit VARCHAR(255),
	f_impact_method BIGINT, 
	
	PRIMARY KEY (id)	

);


CREATE TABLE tbl_impact_factors (

	id BIGINT NOT NULL, 
	f_impact_category BIGINT, 
	f_flow BIGINT, 
	f_flow_property_factor BIGINT, 
	f_unit BIGINT, 
	value DOUBLE,
	formula VARCHAR(1000),
	
	distribution_type INTEGER default 0, 
	parameter1_value DOUBLE, 
	parameter1_formula VARCHAR(1000), 
	parameter2_value DOUBLE, 
	parameter2_formula VARCHAR(1000), 
	parameter3_value DOUBLE, 
	parameter3_formula VARCHAR(1000), 
	
	PRIMARY KEY (id)

);
CREATE INDEX idx_impact_factor_flow ON tbl_impact_factors(f_flow);


CREATE TABLE tbl_nw_sets (

	id BIGINT NOT NULL,
    ref_id VARCHAR(36),
    name VARCHAR(255),
    description TEXT,
    version BIGINT,
	last_change BIGINT,

	f_impact_method BIGINT,
	weighted_score_unit VARCHAR(255),
	
	PRIMARY KEY (id)

);


CREATE TABLE tbl_nw_factors (

	id BIGINT NOT NULL, 
	weighting_factor DOUBLE, 
	normalisation_factor DOUBLE,
	f_impact_category BIGINT,
	f_nw_set BIGINT,
	
	PRIMARY KEY (id)

);


CREATE TABLE tbl_parameters (

	id BIGINT NOT NULL, 
	ref_id VARCHAR(36),
	name VARCHAR(255), 
	description TEXT, 
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT, 
	
	is_input_param TINYINT default 0,
	f_owner BIGINT, 
	scope VARCHAR(255), 
	value DOUBLE, 
	formula VARCHAR(1000),
	external_source VARCHAR(255),
	source_type VARCHAR(255),
	
	distribution_type INTEGER default 0, 
	parameter1_value DOUBLE, 
	parameter1_formula VARCHAR(1000), 
	parameter2_value DOUBLE, 
	parameter2_formula VARCHAR(1000), 
	parameter3_value DOUBLE, 
	parameter3_formula VARCHAR(1000), 
	
	PRIMARY KEY (id)
);
CREATE INDEX idx_parameter_category ON tbl_parameters(f_category);

CREATE TABLE tbl_parameter_redefs (

	id BIGINT NOT NULL, 
	name VARCHAR(255), 
	f_owner BIGINT, 
	f_context BIGINT,
	context_type VARCHAR(255),
	value DOUBLE,
	
	distribution_type INTEGER default 0, 
	parameter1_value DOUBLE, 
	parameter1_formula VARCHAR(1000), 
	parameter2_value DOUBLE, 
	parameter2_formula VARCHAR(1000), 
	parameter3_value DOUBLE, 
	parameter3_formula VARCHAR(1000), 
	
	PRIMARY KEY (id)
);


CREATE TABLE tbl_projects (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,

	creation_date TIMESTAMP,
	functional_unit TEXT, 
	last_modification_date TIMESTAMP,
	goal TEXT, 
	f_author BIGINT, 
	f_impact_method BIGINT,
	f_nwset BIGINT,
	
	PRIMARY KEY (id)	
);


CREATE TABLE tbl_project_variants (
	
	id BIGINT NOT NULL,
	f_project BIGINT,
	name VARCHAR(255), 
	f_product_system BIGINT,
	f_unit BIGINT,
	f_flow_property_factor BIGINT,
	amount DOUBLE,
	allocation_method VARCHAR(255),

	PRIMARY KEY (id)	
);


CREATE TABLE tbl_mapping_files (
	id BIGINT NOT NULL,
	file_name VARCHAR(255),
    content MEDIUMBLOB,
	PRIMARY KEY (id)
);


CREATE TABLE tbl_currencies (

	id BIGINT NOT NULL,
	name VARCHAR(255),
	ref_id VARCHAR(36),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,
	
	code VARCHAR(255),
	conversion_factor DOUBLE,
	f_reference_currency BIGINT,	
	
	PRIMARY KEY (id)

);


CREATE TABLE tbl_process_group_sets (
	id BIGINT NOT NULL,
	name VARCHAR(255), 
	groups_blob MEDIUMBLOB,		
	PRIMARY KEY (id)	
) ;


CREATE TABLE tbl_social_indicators (

	id BIGINT NOT NULL,
	ref_id VARCHAR(36),
	name VARCHAR(255),
	version BIGINT,
	last_change BIGINT,
	f_category BIGINT,
	description TEXT,
	
	activity_variable VARCHAR(255),
	f_activity_quantity BIGINT,
	f_activity_unit BIGINT,
	unit_of_measurement VARCHAR(255),
	evaluation_scheme TEXT,
	
	PRIMARY KEY (id)
);


CREATE TABLE tbl_social_aspects (

	id BIGINT NOT NULL,
	f_process BIGINT,
	f_indicator BIGINT,
	activity_value DOUBLE,
	raw_amount VARCHAR(255),
	risk_level VARCHAR(255),
	comment TEXT,
	f_source BIGINT,
	quality VARCHAR(255),

	PRIMARY KEY (id)
);

CREATE TABLE tbl_dq_systems (

	id BIGINT NOT NULL,
	name VARCHAR(255), 
	ref_id VARCHAR(36),
	version BIGINT, 
	last_change BIGINT,
	f_category BIGINT, 
	f_source BIGINT, 
	description TEXT,
	has_uncertainties TINYINT default 0,

	PRIMARY KEY (id)
);

CREATE TABLE tbl_dq_indicators ( 
	id BIGINT NOT NULL, 
	name VARCHAR(255), 
	position INTEGER NOT NULL, 
	f_dq_system BIGINT, 

	PRIMARY KEY (id)
);

CREATE TABLE tbl_dq_scores ( 
	id BIGINT NOT NULL, 
	position INTEGER NOT NULL, 
	description TEXT, 
	label VARCHAR(255), 
	uncertainty DOUBLE default 0, 
	f_dq_indicator BIGINT,
				
	PRIMARY KEY (id)
);
