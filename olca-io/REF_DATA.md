openLCA Reference Data Format
=============================
The openLCA reference data format defines a simple CSV format for exchanging
reference data like flows, units, categories etc. and mappings to entities of
other formats and databases. In general the CSV files should have the following
properties:

* files should be utf-8 encoded
* columns should be separated by semicolon: ;
* strings should be enclosed in double quotes if it is necessary: "
* the decimal separator of numbers should be a point: .
* the files should not contain column headers


Locations
---------
File:       `locations.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional) 
3. code (string, required)
4. latitude (double, required)
5. longitude (double, required)

Categories
----------
File:       `categories.csv`      
Columns:

0. reference ID (UUID; required)
1. name (string; required)
2. description (string; optional)
3. model type of the category (enumeration: "PROJECT", "PRODUCT_SYSTEM", 
   "IMPACT_METHOD", "PROCESS", "FLOW", "FLOW_PROPERTY", "UNIT_GROUP"; required)
4. reference ID of the parent category (UUID, optional)


Units
-----
File:       `units.csv`
Columns:

0. reference ID (UUID; required)
1. name (string; required)
2. description (string; optional)
3. conversion factor (double; required)
4. synonyms (string: list separated by semicolon; optional)
5. reference ID of unit group (UUID, required)


Unit groups
-----------
File:       `unit_groups.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)
4. default flow property ID (UUID, optional)
5. reference unit ID (UUID, required)


Flow properties
---------------
File:       `flow_properties.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)
4. unit group ID (UUID, required)
5. flow property type (integer enum: 0=economic, 1=physical; required)


Flows
-----
File:       `flows.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)
4. flow type (enumeration: 'ELEMENTARY_FLOW', 'PRODUCT_FLOW', 'WASTE_FLOW'; required)
5. CAS number (string, optional)
6. formula (string, optional)
7. reference flow property (UUID, required)


Flow property factors 
---------------------
(relations between flows and flow properties)
File:       `flow_property_factors.csv`

0. flow ID (UUID, required)
1. flow property ID (UUID, required)
2. factor (double, required) (1 if it is the reference flow property)


LCIA methods
------------
File:       `lcia_methods.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)


LCIA categories
---------------
File:       `lcia_categories.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. reference unit (string, required)
4. LCIA method ID (UUID, required)


LCIA factors
------------

File:       `lcia_factors.csv`

Columns:

0. reference ID of the LCIA category (UUID, required)
1. reference ID of the flow (UUID, required)
2. reference ID of the flow's property (UUID, required)
3. reference ID of the flow's unit (UUID, required)
4. value of the factor (double, required)


