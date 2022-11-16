# openLCA Reference Data and Mapping Format

The openLCA reference data format defines a simple CSV format for exchanging
reference data like flows, units, categories etc. and mappings to entities of
other formats and databases. In general all CSV files should have the following
properties:

* files should be utf-8 encoded
* columns should be separated by semicolons: `;`
* strings should be enclosed in double quotes if it is necessary: `"`
* the decimal separator of numbers should be a decimal point: `.`
* the files should __not__ contain column headers


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


## Units

File: `units.csv`

```
0 | uuid               | required | uuid
1 | name               | required | string
2 | description        | optional | string
3 | conversion factor  | required | double
4 | synonyms           | optional | string list separated by semicolons
5 | unit group         | required | uuid or name
```


## Unit groups

File: `unit_groups.csv`

```
0  | uuid                  | required | uuid
1  | name                  | required | string
2  | description           | optional | string
3  | category              | optional | path
4  | default flow property | optional | uuid or name
5  | reference unit        | required | uuid or name
```


## Flow properties

File: `flow_properties.csv`

```
0  | uuid          | required | uuid
1  | name          | required | string
2  | description   | optional | string
3  | category      | optional | path
4  | unit group    | required | uuid or name
5  | property type | optional | "economic" else "physical"
```


## Flows

File: `flows.csv`

```
0  | uuid                    | required | uuid
1  | name                    | required | string
2  | description             | optional | string
3  | category                | optional | path
4  | flow type               | required | "elementary" or "product" or "waste"
5  | CAS number              | optional | string
6  | chem. formula           | optional | string
7  | reference flow property | required | uuid or name
```


## Flow property factors

File: `flow_property_factors.csv`

This file is optional and only required, if additional flow properties than the
reference flow properties should be added to a flow.

```
0 | flow              | required | uuid
1 | flow property     | required | uuid or name
2 | conversion factor | required | double
```


## Currencies

File: `currencies.csv`

Note that all currencies should have the same reference currency.

```
0  | uuid               | required | uuid
1  | name               | required | string
2  | description        | optional | string
3  | category           | optional | path
4  | reference currency | required | name or uuid
5  | currency code      | required | string
6  | conversion factor  | required | double
```

---------

**the things below are outdated and we need to rethink these**

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
2. reference ID of the flow property of the factor (UUID, required)
3. reference ID of the unit of the factor (UUID, required)
4. value of the factor (double, required)
5. formula of the factor (string, optional)


Normalisation and weighting sets
--------------------------------
File:		`nw_sets.csv`

Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. weighted score unit (string, optional)
4. LCIA method ID (UUID, required)


Normalisation and weighting set factors
---------------------------------------
File:		`nw_set_factors.csv`

Columns:

0. reference ID of the normalisation and weighting set (UUID, required)
1. reference ID of the LCIA category (UUID, required)
2. nomalisation factor (double, optional)
3. weighting factor (double, optional)


KML for locations
-----------------
File:		`Geographies.xml`

The reference data import supports the import of geometries for locations. The
supported file format is the same as for the ecoinvent 3 database (see
http://www.ecoinvent.org/data-providers/how-to-submit-data/ecospold2). The
locations are mapped via the respective location code. So in principle you just
need to copy the `Geographies.xml` file from ecoinvent 3 to the reference data
folder and the KML data from this file will be imported.
