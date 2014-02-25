openLCA Reference Data Format
=============================
The openLCA reference data format defines a simple CSV format for exchanging
reference data like flows, units, categories etc. and mappings to entities of
other formats and databases. In general the CSV files should have the following
properties:

* files should be utf-8 encoded
* columns should be separated by semicolon: ;
* strings should be enclosed in double quotes: "
* the decimal separator of numbers should be a point: .
* the files should not contain column headers

Categories
----------
Categories are written and read from the file `categories.csv` which has the
following columns:

1) the reference ID of the category (UUID; required)
2) the name of the category (string; required)
3) a description of the category (string; optional)
4) the model type of the category (enumeration: "PROJECT", "PRODUCT_SYSTEM", 
   "IMPACT_METHOD", "PROCESS", "FLOW", "FLOW_PROPERTY", "UNIT_GROUP"; required)
5) the reference ID of the parent category (UUID, optional)





