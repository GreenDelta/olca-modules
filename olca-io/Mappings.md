Data mappings between openLCA and other data formats
====================================================

EcoSpold 2 activity data sets
-----------------------------
EcoSpold 2 activity data sets can be directly mapped to openLCA
process data sets. Referenced entities like flows, units, actors,
sources etc. are searched by the respective UUID in the openLCA 
database or created during the import.

### Activity description

#### Activity

#### Classification

#### Geography

#### Technology

#### Time period

#### Macro-economic scenario


### Flow data

#### Intermediate exchanges

| @id (UUID, required) | in openLCA we use generate integer IDs for exchanges
| @unitId (UUID, required)

#### Elementary exchanges

#### Parameters


### Modelling and validation

#### Representativeness (optional)

| @percent (double, optional) | *no corresponding field*
| @systemModelId (UUID, *required* ) | *no corresponding field* / default value
| @systemModelContextId (UUID, optional ) | *no corresponding field*
| systemModelName (string, required) | *no corresponding field* / default value
| samplingProcedure (string, optional) | ProcessDocumentation/Sampling
| extrapolations (string, optional) | ProcessDocumentation/DataTreatment

#### Review (optional)

| @reviewerId 


| EcoSpold field | openLCA field |
|----------------|---------------|
|Representativeness/Extrapolations | ProcessDocumentation/DataTreatment|
|Representativeness/Percent (double) | #no field# |
|Representativeness/SamplingProcedure | ProcessDocumentation/Sampling|
