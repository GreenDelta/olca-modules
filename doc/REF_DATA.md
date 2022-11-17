# CSV format for openLCA reference data

Reference data are common units, flow properties, flows, locations, LCIA
categories etc. for different databases. We store these data in simple CSV
files in order to create reference data packages from scratch; it shouldn't be
used as a general data exchange format. All CSV files need to have the following
format:

* the file encoding must be [utf-8](https://en.wikipedia.org/wiki/UTF-8)
* the column separator is a comma: `,`
* strings should be enclosed in double quotes, but only if necessary: `"`
* the decimal separator of numbers is a decimal point: `.`
* the files should __not__ contain column headers


## Locations

**File**:`locations.csv`

```
0  | uuid        | required | uuid
1  | name        | required | string
2  | description | optional | string
3  | category    | optional | path
4  | code        | required | string
5  | latitude    | required | double
6  | longitude   | required | double
```

## Unit groups

**File**:`unit_groups.csv`

```
0  | uuid                  | required | uuid
1  | name                  | required | string
2  | description           | optional | string
3  | category              | optional | path
4  | default flow property | optional | uuid or name
5  | reference unit        | required | uuid or name
```

**File**:`units.csv`

```
0 | uuid               | required | uuid
1 | name               | required | string
2 | description        | optional | string
3 | conversion factor  | required | double
4 | synonyms           | optional | string list separated by semicolons
5 | unit group         | required | uuid or name
```

## Flow properties

**File**:`flow_properties.csv`

```
0  | uuid          | required | uuid
1  | name          | required | string
2  | description   | optional | string
3  | category      | optional | path
4  | unit group    | required | uuid or name
5  | property type | optional | "economic" else "physical"
```


## Flows

**File**:`flows.csv`

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

**File**:`flow_property_factors.csv`

This file is optional and only required, if additional flow properties than the
reference flow properties should be added to a flow.

```
0 | flow              | required | uuid
1 | flow property     | required | uuid or name
2 | conversion factor | required | double
```


## Currencies

**File**:`currencies.csv`

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

## LCIA categories

**File**: `lcia_categories.csv`

```
0  | uuid               | required | uuid
1  | name               | required | string
2  | description        | optional | string
3  | category           | optional | path
4  | reference unit     | optional | string
```

**Files**: `lcia_factors/{short-id}.csv`

A reference database can contain many LCIA categories and each LCIA category can
contain many characterization factors. We therefore write the characterization
factors of each LCIA category into a separate file. These files are located in a
`lcia_factors` sub-folder. Anything can be used as name of file in this folder,
we typically take the first part of the uuid of an LCIA category for this.

```
0 | LCIA category | required | uuid
1 | flow          | required | uuid
2 | flow property | required | uuid or name
3 | flow unit     | required | uuid or name
4 | location      | required | uuid or name
5 | factor        | required | double or formula
```

**File**: `lcia_parameters_{short-id}.csv`

This file is **not** handled yet in the openLCA import and export.

```
0 | LCIA category | required | uuid
1 | parameter     | required | string
2 | value         | required | double or formula
```

## LCIA methods

**File**:`lcia_methods.csv`

```
0 | uuid         | required | uuid
1 | name         | required | string
2 | description  | optional | string
3 | category     | optional | path
```

**File**:`lcia_method_categories.csv`

```
0 | LCIA method   | required | uuid or name
1 | LCIA category | required | uuid
```

**File**:`lcia_method_nw_sets.csv`

```
0 | LCIA method          | required | uuid or name
1 | NW set - ID          | required | uuid
2 | NW set - name        | required | string
3 | LCIA category        | required | uuid
4 | nomalisation factor  | optional | double
5 | weighting factor     | optional | double
6 | weighting score unit | optional | string
```
