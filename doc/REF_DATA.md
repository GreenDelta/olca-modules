# openLCA reference data

The openLCA reference data format defines a simple CSV format for exchanging
reference data like flows, units, categories etc. and mappings to entities of
other formats and databases. In general all CSV files should have the following
properties:

* files should be utf-8 encoded
* columns should be separated by semicolons: `,`
* strings should be enclosed in double quotes if it is necessary: `"`
* the decimal separator of numbers should be a decimal point: `.`
* the files should __not__ contain column headers


## Locations

File: `locations.csv`

```
0  | uuid        | required | uuid
1  | name        | required | string
2  | description | optional | string
3  | category    | optional | path
4  | code        | required | string
5  | latitude    | required | double
6  | longitude   | required | double
```

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

## LCIA categories

File: `lcia_categories.csv`

```
0  | uuid               | required | uuid
1  | name               | required | string
2  | description        | optional | string
3  | category           | optional | path
4  | reference unit     | optional | string
```

File: `lcia_factors_{short-id}.csv`

```
0 | LCIA category | required | uuid
1 | flow          | required | uuid
2 | flow property | required | uuid or name
3 | flow unit     | required | uuid or name
4 | location      | required | uuid or name
5 | factor        | required | double or formula
```

File: `lcia_parameters_{short-id}.csv`; **not yet implemented**

```
0 | LCIA category | required | uuid
1 | parameter     | required | string
2 | value         | required | double or formula
```

## LCIA methods

File: `lcia_methods.csv`

```
0 | uuid         | required | uuid
1 | name         | required | string
2 | description  | optional | string
3 | category     | optional | path
```

File: `lcia_method_categories.csv`

```
0 | LCIA method   | required | uuid or name
1 | LCIA category | required | uuid
```

File: `lcia_method_nw_sets.csv`

```
0 | LCIA method          | required | uuid or name
1 | NW set - ID          | required | uuid
2 | NW set - name        | required | string
3 | LCIA category        | required | uuid
4 | nomalisation factor  | optional | double
5 | weighting factor     | optional | double
6 | weighting score unit | optional | string
```
