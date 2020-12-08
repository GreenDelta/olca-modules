# The CSV format of openLCA flow mapping files

A flow mapping file describes the mapping of flows from a source system to
a target system. The file contains in each row information of a flow `s` from
the source system that is mapped to a flow `t` from the target system. When
such a mapping file is applied in an import, `s` is always the flow from the
external data source and `t` is the flow of the database into which data from
the external source is imported. However, when the mapping file is used to
replace flows in a database with flows from an external source (e.g. a
JSON-LD package), then `s` is the respective flow in the database and `t` the
flow from the external source. So the meaning of `s` and `t` depends on the
respective task in which a mapping file is used.

Flow mappings can be exchanged as CSV files with the following properties:

* mapping files should be encoded in UTF-8
* columns should be separated by semicolons: `;`
* strings should be enclosed in double quotes if it is necessary: `"`
* the decimal separator of numbers should be a decimal point: `.`
* the files should not contain column headers

The column schema is as follows:

```
# required columns
  0. s: flow uuid           - uuid
  1. t: flow uuid           - uuid
  2. conversion factor      - double

# optional columns

# general flow attributes
  3. s: flow name           - string
  4. s: category            - string
  5. s: location code       - string
  6. t: flow name           - string
  7. t: category            - string
  8. t: location code       - string

# flow property information
  9. s: flow property uuid  - uuid
 10. s: flow property name  - string
 11. t: flow property uuid  - uuid
 12. t: flow property name  - string

# unit information
 13. s: unit uuid           - uuid
 14. s: unit name           - string
 15. t: unit uuid           - uuid
 16. t: unit name           - string

# provider process of the target flow
 17: t: provider UUID       - uuid
 18: t: provider name       - string
 19: t: provider category   - string
 20: t: provider location   - string
```

Only the first three columns are required (to be compatible with previous
format versions). The conversion factor is applied in the following way: when a
flow `s` is mapped to a flow `t`, the amount `a_s` of an exchange with flow `s`
is multiplied with the conversion factor `x` to get the amount `a_t` for that
exchange with flow `t`.

```
a_t = x * a_s
```

Correspondingly, for an LCIA factor `c_s` with flow `s` the inverse of
conversion factor is applied in order to get the LCIA factor `c_t` with flow `t`:

```
c_t = (1 / x) * c_s
```

If not otherwise specified in the mapping (e.g. via `s: unit`, `t: unit`) it is
assumed that the conversion factor is related to the reference flow property
and unit of the respective flows as defined in the respective systems.

Finally, it is possible to link a flow `t` in exchanges to a provider process
during an import when it is a product or waste flow.
