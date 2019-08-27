/*Schema generated with Eclipse link @see persistence.xml for details
Manual changes required
  - add semicolons
  - change VARCHAR(50) to VARCHAR(255) for SEQUENCE table
  - add openlca_version table with insert
  - remove all CONSTRAINTs
    @TODO maybe we could add the indexes to the model
  - copy all indexes from current_schema_derby.sql
  - drop primary key on tbl_source_links
  - replace all "boolean" columns with "smallint default 0"

*/
create table SEQUENCE (
	SEQ_NAME varchar(255) not null,
	SEQ_COUNT bigint
);
insert into SEQUENCE(SEQ_NAME, SEQ_COUNT) values('entity_seq', 0);


create table openlca_version(version smallint);
insert into openlca_version (version) values (8);

create table tbl_actors (id bigint not null, address varchar(255), city varchar(255), country varchar(255), description text, email varchar(255), last_change bigint, name VARCHAR(2048), ref_id varchar(36), telefax varchar(255), telephone varchar(255), version bigint, website varchar(255), zip_code varchar(255), f_category bigint, primary key (id));
create index idx_actor_category on tbl_actors(f_category);
create index idx_actor_ref_id on tbl_actors(ref_id);

create table tbl_allocation_factors (id bigint not null, allocation_type varchar(255), f_product bigint, value FLOAT, f_exchange bigint, f_process bigint, primary key (id));
create table tbl_categories (id bigint not null, description text, last_change bigint, model_type varchar(255), name VARCHAR(255), ref_id varchar(255), version bigint, f_category bigint, primary key (id));
create index idx_category_parent on tbl_categories(f_category);
create index idx_category_ref_id on tbl_categories(ref_id);

create table tbl_currencies (id bigint not null, code varchar(255), conversion_factor FLOAT, description text, last_change bigint, name VARCHAR(2048), ref_id varchar(36), version bigint, f_category bigint, f_reference_currency bigint, primary key (id));
create table tbl_exchanges (id bigint not null, resulting_amount_value FLOAT, resulting_amount_formula varchar(1000), base_uncertainty FLOAT, cost_formula varchar(1000), cost_value FLOAT, f_default_provider bigint, description TEXT, dq_entry varchar(50), internal_id integer, avoided_product smallint default 0, is_input smallint default 0, distribution_type integer, parameter1_formula varchar(1000), parameter2_formula varchar(1000), parameter3_formula varchar(1000), parameter1_value FLOAT, parameter2_value FLOAT, parameter3_value FLOAT, f_currency bigint, f_flow bigint, f_flow_property_factor bigint, f_unit bigint, f_owner bigint, primary key (id));
create index idx_exchange_process on tbl_exchanges(f_owner);
create index idx_exchange_flow on tbl_exchanges(f_flow);

create table tbl_flows (id bigint not null, cas_number varchar(255), description text, flow_type varchar(255), formula varchar(255), infrastructure_flow smallint default 0, last_change bigint, name VARCHAR(2048), ref_id varchar(36), synonyms varchar(32672), version bigint, f_category bigint, f_location bigint, f_reference_flow_property bigint, primary key (id));
create index idx_flow_category on tbl_flows(f_category);
create index idx_flow_flow_property on tbl_flows(f_reference_flow_property);
create index idx_flow_location on tbl_flows(f_location);
create index idx_flow_ref_id on tbl_flows(ref_id);

create table tbl_flow_properties (id bigint not null, description text, flow_property_type varchar(255), last_change bigint, name VARCHAR(2048), ref_id varchar(36), version bigint, f_category bigint, f_unit_group bigint, primary key (id));

create index idx_flowprop_category on tbl_flow_properties(f_category);
create index idx_flowprop_unti_group on tbl_flow_properties(f_unit_group);
create index idx_flowprop_ref_id on tbl_flow_properties(ref_id);

create table tbl_flow_property_factors (id bigint not null, conversion_factor FLOAT, f_flow_property bigint, f_flow bigint, primary key (id));
create index idx_flow_factor_flow on tbl_flow_property_factors(f_flow);
create index idx_flow_factor_property on tbl_flow_property_factors(f_flow_property);

create table tbl_impact_categories (id bigint not null, description text, last_change bigint, name VARCHAR(2048), ref_id varchar(36), reference_unit varchar(255), version bigint, f_impact_method bigint, primary key (id));
create table tbl_impact_factors (id bigint not null, formula varchar(1000), value FLOAT, distribution_type integer, parameter1_formula varchar(1000), parameter2_formula varchar(1000), parameter3_formula varchar(1000), parameter1_value FLOAT, parameter2_value FLOAT, parameter3_value FLOAT, f_flow bigint, f_flow_property_factor bigint, f_unit bigint, f_impact_category bigint, primary key (id));
create index idx_impact_factor_flow on tbl_impact_factors(f_flow);

create table tbl_impact_methods (id bigint not null, description text, last_change bigint, name VARCHAR(2048), parameter_mean varchar(255), ref_id varchar(36), version bigint, f_author bigint, f_category bigint, f_generator bigint, primary key (id));
create table tbl_locations (id bigint not null, code varchar(255), description text, kmz bytea, last_change bigint, latitude FLOAT, longitude FLOAT, name VARCHAR(2048), ref_id varchar(36), version bigint, f_category bigint, primary key (id));
create index idx_location_category on tbl_locations(f_category);
create index idx_location_ref_id on tbl_locations(ref_id);

create table tbl_nw_sets (id bigint not null, description text, last_change bigint, name VARCHAR(2048), ref_id varchar(36), version bigint, weighted_score_unit varchar(255), f_impact_method bigint, primary key (id));
create table tbl_nw_factors (id bigint not null, normalisation_factor FLOAT, weighting_factor FLOAT, f_impact_category bigint, f_nw_set bigint, primary key (id));
create table tbl_parameters (id bigint not null, description text, external_source varchar(255), formula varchar(1000), is_input_param smallint default 0, last_change bigint, name VARCHAR(2048), ref_id varchar(36), scope varchar(255), source_type varchar(255), value FLOAT, version bigint, distribution_type integer, parameter1_formula varchar(1000), parameter2_formula varchar(1000), parameter3_formula varchar(1000), parameter1_value FLOAT, parameter2_value FLOAT, parameter3_value FLOAT, f_category bigint, f_owner bigint, primary key (id));
create index idx_parameter_category on tbl_parameters(f_category);

create table tbl_parameter_redefs (id bigint not null, f_context bigint, context_type varchar(255), name VARCHAR(2048), value FLOAT, distribution_type integer, parameter1_formula varchar(1000), parameter2_formula varchar(1000), parameter3_formula varchar(1000), parameter1_value FLOAT, parameter2_value FLOAT, parameter3_value FLOAT, f_owner bigint, primary key (id));
create table tbl_processes (id bigint not null, default_allocation_method varchar(255), description text, dq_entry varchar(50), infrastructure_process smallint default 0, last_change bigint, last_internal_id integer, name VARCHAR(2048), process_type varchar(255), ref_id varchar(36), version bigint, f_category bigint, f_currency bigint, f_process_doc bigint, f_dq_system bigint, f_exchange_dq_system bigint, f_location bigint, f_quantitative_reference bigint, f_social_dq_system bigint, primary key (id));
create index idx_process_category on tbl_processes(f_category);
create index idx_process_qref on tbl_processes(f_quantitative_reference);
create index idx_process_location on tbl_processes(f_location);
create index idx_process_ref_id on tbl_processes(ref_id);

create table tbl_process_docs (id bigint not null, completeness text, copyright smallint default 0, creation_date timestamp, data_collection_period text, data_selection text, data_treatment text, geography text, intended_application text, inventory_method text, modeling_constants text, preceding_dataset varchar(255), project varchar(255), restrictions text, review_details text, sampling text, technology text, time TEXT, valid_from date, valid_until date, f_data_documentor bigint, f_data_generator bigint, f_dataset_owner bigint, f_publication bigint, f_reviewer bigint, primary key (id));
create table tbl_process_group_sets (id bigint not null, groups_blob bytea, name VARCHAR(2048), primary key (id));
create table tbl_product_systems (id bigint not null, CUTOFF FLOAT, description text, last_change bigint, name VARCHAR(2048), ref_id varchar(36), target_amount FLOAT, version bigint, f_category bigint, f_reference_exchange bigint, f_reference_process bigint, f_target_flow_property_factor bigint, f_target_unit bigint, primary key (id));
create table tbl_projects (id bigint not null, creation_date timestamp, description text, functional_unit text, goal text, f_impact_method bigint, last_change bigint, last_modification_date timestamp, name VARCHAR(2048), f_nwset bigint, ref_id varchar(36), version bigint, f_author bigint, f_category bigint, primary key (id));
create table tbl_project_variants (id bigint not null, allocation_method varchar(255), amount FLOAT, is_disabled smallint default 0, name VARCHAR(2048), f_flow_property_factor bigint, f_product_system bigint, f_unit bigint, f_project bigint, primary key (id));
create table tbl_social_aspects (id bigint not null, activity_value FLOAT, comment text, quality varchar(255), raw_amount varchar(255), risk_level varchar(255), f_indicator bigint, f_source bigint, f_process bigint, primary key (id));
create table tbl_sources (id bigint not null, description text, external_file varchar(255), last_change bigint, name VARCHAR(2048), ref_id varchar(36), text_reference text, url varchar(255), version bigint, source_year smallint default 0, f_category bigint, primary key (id));

create index idx_source_category on tbl_sources(f_category);
create index idx_source_ref_id on tbl_sources(ref_id);

create table tbl_units (id bigint not null, conversion_factor FLOAT, description text, last_change bigint, name VARCHAR(2048), ref_id varchar(36), synonyms varchar(255), version bigint, f_unit_group bigint, primary key (id));
create index idx_unit_unit_group on tbl_units(f_unit_group);
create index idx_unit_ref_id on tbl_units(ref_id);

create table tbl_unit_groups (id bigint not null, description text, last_change bigint, name VARCHAR(255), ref_id varchar(255), version bigint, f_category bigint, f_default_flow_property bigint, f_reference_unit bigint, primary key (id));
create index idx_unit_group_category on tbl_unit_groups(f_category);
create index idx_unit_group_refunit on tbl_unit_groups(f_reference_unit);
create index idx_unit_group_flowprop on tbl_unit_groups(f_default_flow_property);
create index idx_unit_group_ref_id on tbl_unit_groups(ref_id);

create table tbl_social_indicators (id bigint not null, activity_variable varchar(255), description text, evaluation_scheme text, last_change bigint, name VARCHAR(2048), ref_id varchar(36), unit_of_measurement varchar(255), version bigint, f_activity_quantity bigint, f_activity_unit bigint, f_category bigint, primary key (id));
create table tbl_dq_systems (id bigint not null, description text, has_uncertainties smallint default 0, last_change bigint, name VARCHAR(2048), ref_id varchar(36), version bigint, f_category bigint, f_source bigint, primary key (id));
create table tbl_dq_indicators (id bigint not null, name VARCHAR(255), position integer, f_dq_system bigint, primary key (id));
create table tbl_dq_scores (id bigint not null, description text, label varchar(255), position integer, uncertainty FLOAT, f_dq_indicator bigint, primary key (id));
create table tbl_mapping_files (id bigint not null, content bytea, file_name varchar(255), primary key (id));
create table tbl_source_links (f_owner bigint not null, f_source bigint not null);
create table tbl_process_links (f_exchange bigint, f_flow bigint, is_system_link smallint default 0, f_process bigint, f_provider bigint, f_product_system bigint);
create index idx_process_link_system on tbl_process_links(f_product_system);

create table tbl_product_system_processes (f_product_system bigint, f_process bigint);
