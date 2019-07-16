drop schema openLCA;
create schema openLCA;


create table if not exists SEQUENCE (
	SEQ_NAME VARCHAR(255) NOT NULL,
	SEQ_COUNT numeric
);
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) VALUES('entity_seq', 0);


create table if not exists openlca_version
(
	version smallint
);

INSERT INTO openlca_version (version) VALUES (8);



create table if not exists tbl_actors
(
	id numeric not null
		constraint idx_16396_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	telefax varchar(255) default NULL::character varying,
	website varchar(255) default NULL::character varying,
	address varchar(255) default NULL::character varying,
	zip_code varchar(255) default NULL::character varying,
	email varchar(255) default NULL::character varying,
	telephone varchar(255) default NULL::character varying,
	country varchar(255) default NULL::character varying,
	city varchar(255) default NULL::character varying
);



create index if not exists idx_16396_idx_actor_category
	on tbl_actors (f_category);

create index if not exists idx_16396_idx_actor_ref_id
	on tbl_actors (ref_id);

create table if not exists tbl_allocation_factors
(
	id numeric not null
		constraint idx_16412_primary
			primary key,
	allocation_type varchar(255) default NULL::character varying,
	value double precision,
	f_process numeric,
	f_product numeric,
	f_exchange numeric
);



create table if not exists tbl_categories
(
	id numeric not null
		constraint idx_16419_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	description text,
	version numeric,
	last_change numeric,
	model_type varchar(255) default NULL::character varying,
	f_category numeric
);



create index if not exists idx_16419_idx_category_ref_id
	on tbl_categories (ref_id);

create index if not exists idx_16419_idx_category_parent
	on tbl_categories (f_category);

create table if not exists tbl_currencies
(
	id numeric not null
		constraint idx_16428_primary
			primary key,
	name varchar(2048) default NULL::character varying,
	ref_id varchar(36) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	code varchar(255) default NULL::character varying,
	conversion_factor double precision,
	f_reference_currency numeric
);



create table if not exists tbl_dq_indicators
(
	id numeric not null
		constraint idx_16437_primary
			primary key,
	name varchar(2048) default NULL::character varying,
	position bigint not null,
	f_dq_system numeric
);



create table if not exists tbl_dq_scores
(
	id numeric not null
		constraint idx_16444_primary
			primary key,
	position bigint not null,
	description text,
	label varchar(255) default NULL::character varying,
	uncertainty double precision default '0'::double precision,
	f_dq_indicator numeric
);



create table if not exists tbl_dq_systems
(
	id numeric not null
		constraint idx_16452_primary
			primary key,
	name varchar(2048) default NULL::character varying,
	ref_id varchar(36) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	f_source numeric,
	description text,
	has_uncertainties smallint default '0'::smallint
);



create table if not exists tbl_exchanges
(
	id numeric not null
		constraint idx_16461_primary
			primary key,
	f_owner numeric,
	internal_id bigint,
	f_flow numeric,
	f_unit numeric,
	is_input smallint default '0'::smallint,
	f_flow_property_factor numeric,
	resulting_amount_value double precision,
	resulting_amount_formula varchar(1000) default NULL::character varying,
	avoided_product smallint default '0'::smallint,
	f_default_provider numeric,
	description text,
	cost_value double precision,
	cost_formula varchar(1000) default NULL::character varying,
	f_currency numeric,
	distribution_type bigint default '0'::bigint,
	parameter1_value double precision,
	parameter1_formula varchar(1000) default NULL::character varying,
	parameter2_value double precision,
	parameter2_formula varchar(1000) default NULL::character varying,
	parameter3_value double precision,
	parameter3_formula varchar(1000) default NULL::character varying,
	dq_entry varchar(50) default NULL::character varying,
	base_uncertainty double precision
);



create index if not exists idx_16461_idx_exchange_process
	on tbl_exchanges (f_owner);

create index if not exists idx_16461_idx_exchange_flow
	on tbl_exchanges (f_flow);

create table if not exists tbl_flows
(
	id numeric not null
		constraint idx_16476_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	synonyms varchar(32672) default NULL::character varying,
	description text,
	flow_type varchar(255) default NULL::character varying,
	infrastructure_flow smallint default '0'::smallint,
	cas_number varchar(255) default NULL::character varying,
	formula varchar(255) default NULL::character varying,
	f_reference_flow_property numeric,
	f_location numeric
);



create index if not exists idx_16476_idx_flow_location
	on tbl_flows (f_location);

create index if not exists idx_16476_idx_flow_ref_id
	on tbl_flows (ref_id);

create index if not exists idx_16476_idx_flow_flow_property
	on tbl_flows (f_reference_flow_property);

create index if not exists idx_16476_idx_flow_category
	on tbl_flows (f_category);

create table if not exists tbl_flow_properties
(
	id numeric not null
		constraint idx_16489_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	flow_property_type varchar(255) default NULL::character varying,
	f_unit_group numeric
);



create index if not exists idx_16489_idx_flowprop_unti_group
	on tbl_flow_properties (f_unit_group);

create index if not exists idx_16489_idx_flowprop_ref_id
	on tbl_flow_properties (ref_id);

create index if not exists idx_16489_idx_flowprop_category
	on tbl_flow_properties (f_category);

create table if not exists tbl_flow_property_factors
(
	id numeric not null
		constraint idx_16498_primary
			primary key,
	conversion_factor double precision,
	f_flow numeric,
	f_flow_property numeric
);



create index if not exists idx_16498_idx_flow_factor_flow
	on tbl_flow_property_factors (f_flow);

create index if not exists idx_16498_idx_flow_factor_property
	on tbl_flow_property_factors (f_flow_property);

create table if not exists tbl_impact_categories
(
	id numeric not null
		constraint idx_16504_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	description text,
	version numeric,
	last_change numeric,
	reference_unit varchar(255) default NULL::character varying,
	f_impact_method numeric
);



create table if not exists tbl_impact_factors
(
	id numeric not null
		constraint idx_16513_primary
			primary key,
	f_impact_category numeric,
	f_flow numeric,
	f_flow_property_factor numeric,
	f_unit numeric,
	value double precision,
	formula varchar(1000) default NULL::character varying,
	distribution_type bigint default '0'::bigint,
	parameter1_value double precision,
	parameter1_formula varchar(1000) default NULL::character varying,
	parameter2_value double precision,
	parameter2_formula varchar(1000) default NULL::character varying,
	parameter3_value double precision,
	parameter3_formula varchar(1000) default NULL::character varying
);



create index if not exists idx_16513_idx_impact_factor_flow
	on tbl_impact_factors (f_flow);

create table if not exists tbl_impact_methods
(
	id numeric not null
		constraint idx_16524_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	parameter_mean varchar(255) default NULL::character varying,
	f_author numeric,
	f_generator numeric
);



create table if not exists tbl_locations
(
	id numeric not null
		constraint idx_16533_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	description text,
	version numeric,
	last_change numeric,
	f_category numeric,
	longitude double precision,
	latitude double precision,
	code varchar(255) default NULL::character varying,
	kmz bytea
);



create index if not exists idx_16533_idx_location_ref_id
	on tbl_locations (ref_id);

create index if not exists idx_16533_idx_location_category
	on tbl_locations (f_category);

create table if not exists tbl_mapping_files
(
	id numeric not null
		constraint idx_16542_primary
			primary key,
	file_name varchar(255) default NULL::character varying,
	content bytea
);



create table if not exists tbl_nw_factors
(
	id numeric not null
		constraint idx_16549_primary
			primary key,
	weighting_factor double precision,
	normalisation_factor double precision,
	f_impact_category numeric,
	f_nw_set numeric
);



create table if not exists tbl_nw_sets
(
	id numeric not null
		constraint idx_16555_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	description text,
	version numeric,
	last_change numeric,
	f_impact_method numeric,
	weighted_score_unit varchar(255) default NULL::character varying
);



create table if not exists tbl_parameters
(
	id numeric not null
		constraint idx_16564_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	description text,
	version numeric,
	last_change numeric,
	f_category numeric,
	is_input_param smallint default '0'::smallint,
	f_owner numeric,
	scope varchar(255) default NULL::character varying,
	value double precision,
	formula varchar(1000) default NULL::character varying,
	external_source varchar(255) default NULL::character varying,
	source_type varchar(255) default NULL::character varying,
	distribution_type bigint default '0'::bigint,
	parameter1_value double precision,
	parameter1_formula varchar(1000) default NULL::character varying,
	parameter2_value double precision,
	parameter2_formula varchar(1000) default NULL::character varying,
	parameter3_value double precision,
	parameter3_formula varchar(1000) default NULL::character varying
);



create index if not exists idx_16564_idx_parameter_category
	on tbl_parameters (f_category);

create table if not exists tbl_parameter_redefs
(
	id numeric not null
		constraint idx_16581_primary
			primary key,
	name varchar(2048) default NULL::character varying,
	f_owner numeric,
	f_context numeric,
	context_type varchar(255) default NULL::character varying,
	value double precision,
	distribution_type bigint default '0'::bigint,
	parameter1_value double precision,
	parameter1_formula varchar(1000) default NULL::character varying,
	parameter2_value double precision,
	parameter2_formula varchar(1000) default NULL::character varying,
	parameter3_value double precision,
	parameter3_formula varchar(1000) default NULL::character varying
);



create table if not exists tbl_processes
(
	id numeric not null
		constraint idx_16593_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	process_type varchar(255) default NULL::character varying,
	default_allocation_method varchar(255) default NULL::character varying,
	infrastructure_process smallint default '0'::smallint,
	f_quantitative_reference numeric,
	f_location numeric,
	f_process_doc numeric,
	f_currency numeric,
	f_dq_system numeric,
	dq_entry varchar(50) default NULL::character varying,
	f_exchange_dq_system numeric,
	f_social_dq_system numeric,
	last_internal_id bigint
);



create index if not exists idx_16593_idx_process_ref_id
	on tbl_processes (ref_id);

create index if not exists idx_16593_idx_process_location
	on tbl_processes (f_location);

create index if not exists idx_16593_idx_process_qref
	on tbl_processes (f_quantitative_reference);

create index if not exists idx_16593_idx_process_category
	on tbl_processes (f_category);

create table if not exists tbl_process_docs
(
	id numeric not null
		constraint idx_16605_primary
			primary key,
	geography text,
	technology text,
	time text,
	valid_from date,
	valid_until date,
	modeling_constants text,
	data_treatment text,
	sampling text,
	completeness text,
	review_details text,
	inventory_method text,
	data_collection_period text,
	data_selection text,
	f_reviewer numeric,
	project text,
	creation_date timestamp with time zone,
	intended_application text,
	restrictions text,
	copyright smallint default '0'::smallint,
	f_data_generator numeric,
	f_dataset_owner numeric,
	f_data_documentor numeric,
	f_publication numeric,
	preceding_dataset varchar(255) default NULL::character varying
);



create table if not exists tbl_process_group_sets
(
	id numeric not null
		constraint idx_16613_primary
			primary key,
	name varchar(2048) default NULL::character varying,
	groups_blob bytea
);



create table if not exists tbl_process_links
(
	f_product_system numeric,
	f_provider numeric,
	f_flow numeric,
	f_process numeric,
	f_exchange numeric,
	is_system_link smallint default '0'::smallint
);



create index if not exists idx_16620_idx_process_link_system
	on tbl_process_links (f_product_system);

create table if not exists tbl_product_systems
(
	id numeric not null
		constraint idx_16627_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	cutoff double precision,
	target_amount double precision,
	f_reference_process numeric,
	f_reference_exchange numeric,
	f_target_flow_property_factor numeric,
	f_target_unit numeric
);



create table if not exists tbl_product_system_processes
(
	f_product_system numeric not null,
	f_process numeric not null,
	constraint idx_16635_primary
		primary key (f_product_system, f_process)
);



create table if not exists tbl_projects
(
	id numeric not null
		constraint idx_16641_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	creation_date timestamp with time zone,
	functional_unit text,
	last_modification_date timestamp with time zone,
	goal text,
	f_author numeric,
	f_impact_method numeric,
	f_nwset numeric
);



create table if not exists tbl_project_variants
(
	id numeric not null
		constraint idx_16649_primary
			primary key,
	f_project numeric,
	name varchar(2048) default NULL::character varying,
	f_product_system numeric,
	f_unit numeric,
	f_flow_property_factor numeric,
	amount double precision,
	allocation_method varchar(255) default NULL::character varying,
	is_disabled smallint default '0'::smallint
);



create table if not exists tbl_social_aspects
(
	id numeric not null
		constraint idx_16658_primary
			primary key,
	f_process numeric,
	f_indicator numeric,
	activity_value double precision,
	raw_amount varchar(255) default NULL::character varying,
	risk_level varchar(255) default NULL::character varying,
	comment text,
	f_source numeric,
	quality varchar(255) default NULL::character varying
);



create table if not exists tbl_social_indicators
(
	id numeric not null
		constraint idx_16667_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	activity_variable varchar(255) default NULL::character varying,
	f_activity_quantity numeric,
	f_activity_unit numeric,
	unit_of_measurement varchar(255) default NULL::character varying,
	evaluation_scheme text
);



create table if not exists tbl_sources
(
	id numeric not null
		constraint idx_16677_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	source_year smallint,
	text_reference text,
	url varchar(255) default NULL::character varying,
	external_file varchar(255) default NULL::character varying
);



create index if not exists idx_16677_idx_source_ref_id
	on tbl_sources (ref_id);

create index if not exists idx_16677_idx_source_category
	on tbl_sources (f_category);

create table if not exists tbl_source_links
(
	f_owner numeric,
	f_source numeric
);



create table if not exists tbl_units
(
	id numeric not null
		constraint idx_16693_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	description text,
	version numeric,
	last_change numeric,
	conversion_factor double precision,
	synonyms varchar(255) default NULL::character varying,
	f_unit_group numeric
);



create index if not exists idx_16693_idx_unit_ref_id
	on tbl_units (ref_id);

create index if not exists idx_16693_idx_unit_unit_group
	on tbl_units (f_unit_group);

create table if not exists tbl_unit_groups
(
	id numeric not null
		constraint idx_16702_primary
			primary key,
	ref_id varchar(36) default NULL::character varying,
	name varchar(2048) default NULL::character varying,
	version numeric,
	last_change numeric,
	f_category numeric,
	description text,
	f_reference_unit numeric,
	f_default_flow_property numeric
);



create index if not exists idx_16702_idx_unit_group_flowprop
	on tbl_unit_groups (f_default_flow_property);

create index if not exists idx_16702_idx_unit_group_ref_id
	on tbl_unit_groups (ref_id);

create index if not exists idx_16702_idx_unit_group_refunit
	on tbl_unit_groups (f_reference_unit);

create index if not exists idx_16702_idx_unit_group_category
	on tbl_unit_groups (f_category);

create or replace function on_update_current_timestamp_tbl_process_docs() returns trigger
	language plpgsql
as $$
BEGIN
   NEW.creation_date = now();
   RETURN NEW;
END;
$$;



create trigger on_update_current_timestamp
	before update
	on tbl_process_docs
	for each row
	execute procedure on_update_current_timestamp_tbl_process_docs();

create or replace function on_update_current_timestamp_tbl_projects() returns trigger
	language plpgsql
as $$
BEGIN
   NEW.creation_date = now();
   RETURN NEW;
END;
$$;



create trigger on_update_current_timestamp
	before update
	on tbl_projects
	for each row
	execute procedure on_update_current_timestamp_tbl_projects();

