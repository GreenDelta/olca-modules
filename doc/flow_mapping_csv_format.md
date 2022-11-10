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


## Mapping IDs of EcoSpold 1 flows

There are no flow IDs in the EcoSpold 1 format. We generate hash based IDs
based on flow attributes when importing flows from EcoSpold 1 data sets. These
IDs are also used in the mapping files. The script below explains in plain
Python how these IDs are generated:

```py
import hashlib
import uuid
import types

# for elementary flows, the mapping key is calculated
# from the following attributes
category = "air"
sub_category = "unspecified"
name = "Carbon dioxide"
unit = "kg"

# first, we combine these attributes to a lower-case path
# with a defined order
segments = [category, sub_category, name, unit]
path = '/'.join([s.strip().lower() for s in segments])
print(f'path: {path}')  # air/unspecified/carbon dioxide/kg

# we generate a version 3 UUID (MD5 based UUID) with
# an empty namespace
ns = types.SimpleNamespace(bytes=b'')
uid = uuid.uuid3(ns, path)
print(f'uuid: {uid}')  # 5e738bf0-6bfe-3acd-8dcb-c74fe4f18b53

# you may ignore the following; it shows how the
# UUID is constructed internally
hash = hashlib.md5(path.encode('utf-8')).digest()
uuid_bytes = []
for i in range(0, len(hash)):
    b: int
    match i:
        case 6:
            # clear version and set it to 3
            b = (hash[i] & 0x0f) | 0x30
        case 8:
            # clear variant and set it to IETF
            b = (hash[i] & 0x3f) | 0x80
        case _:
            b = hash[i]
    uuid_bytes.append(b)

uid2 = uuid.UUID(bytes=bytes(uuid_bytes))
assert uid == uid2
print(f'home brewed uuid: {uid}')  # 5e738bf0-6bfe-3acd-8dcb-c74fe4f18b53
```

In the openLCA, there is a `KeyGen` utility for generating such UUIDs, e.g.
this can be executed in the P/Jython environment in openLCA:

```py
uid1 = KeyGen.get('air/unspecified/Carbon dioxide/kg')
uid2 = KeyGen.get('air', 'unspecified', 'Carbon dioxide', 'kg')
assert uid1 == uid2  # 5e738bf0-6bfe-3acd-8dcb-c74fe4f18b53
print(uid1)
```
