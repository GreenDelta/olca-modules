openLCA Reference Data and Mapping Format
=========================================
The openLCA reference data format defines a simple CSV format for exchanging
reference data like flows, units, categories etc. and mappings to entities of
other formats and databases. In general all CSV files should have the following
properties:

* files should be utf-8 encoded
* columns should be separated by semicolons: `;`
* strings should be enclosed in double quotes if it is necessary: `"`
* the decimal separator of numbers should be a decimal point: `.`
* the files should __not__ contain column headers

Below is a small code example for reading and writing this format in Python:

```py
import csv
from typing import Any, Iterator, List


def each_row_of(path: str) -> Iterator[List[str]]:
    with open(path, 'r', encoding='utf-8-sig') as stream:
        reader = csv.reader(stream, delimiter=';')
        for row in reader:
            yield row

def write_to(path: str, rows: Iterator[List[Any]]):
    with open(path, 'w', encoding='utf-8', newline='\n') as stream:
        writer = csv.writer(stream, delimiter=';', quoting=csv.QUOTE_MINIMAL)
        for row in rows:
            writer.writerow(row)

def convert(rows: Iterator[List[str]]) -> Iterator[List[str]]:
    for row in rows:
        yield row


if __name__ == '__main__':
    write_to('to.csv', convert(each_row_of('from.csv')))
```


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
Columns:

0. flow ID (UUID, required)
1. flow property ID (UUID, required)
2. factor (double, required) (1 if it is the reference flow property)


Currencies
----------
Every currency must have a link to a reference currency. The reference currency
has a link to itself. For a correct import, currently the reference currency
must be the first line of the file.

File:       `currencies.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)
4. reference currency ID (UUID, required)
5. currency code (string, required)
6. conversion factor (double, required) (1 if it is the reference currency)


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


Default mappings
================

SimaPro CSV: Flow import mapping
--------------------------------
File:		`sp_flow_import_map.csv`

Columns:

0. SimaPro name of the flow (string)
1. SimaPro compartment of the flow (string)
2. SimaPro sub-compartment of the flow (string)
3. SimaPro unit of the flow (string)
4. openLCA reference ID of the flow (UUID)
5. openLCA name of the flow (string)
6. openLCA reference ID of the reference flow property of the flow (UUID)
7. openLCA name of the reference flow property of the flow (string)
8. openLCA reference ID of the reference unit of the flow (UUID)
9. openLCA name of the reference unit of the flow (string)
10. conversion factor: amount_simapro * factor = amount_openlca (double)

