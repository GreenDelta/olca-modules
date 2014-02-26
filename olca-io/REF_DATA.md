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


Categories
----------
File:       `categories.csv`      
Content:    all categories
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
Content:    all reference units
Columns:

0. reference ID (UUID; required)
1. name (string; required)
2. description (string; optional)
3. conversion factor (double; required)
4. synonyms (string: list separated by semicolon; optional)
5. reference ID of unit group (UUID, required)





