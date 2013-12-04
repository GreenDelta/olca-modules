Data mappings between openLCA and other data formats
====================================================

EcoSpold 2 activity data sets
-----------------------------
EcoSpold 2 activity data sets can be directly mapped to openLCA
process data sets. Referenced entities like flows, units, actors,
sources etc. are searched by the respective UUID in the openLCA 
database or created during the import. For the documentation of
the EcoSpold 2 data format see 
http://www.ecoinvent.org/data-providers/how-to-submit-data/ecospold2/.

### Activity description

#### Activity

- activityName (string, #r)
--- [+] Process.name

- @id (UUID, #r)
--- [+] Process.refId

- @activityNameId (UUID, #r)
--- [-] *no corresponding field* - we take the refId field

- @activityNameContextId
--- [-] *no corresponding field*

- @type
--- [+] Process.processType

- @specialActivityType
--- [-] *no corresponding field* - 0 as default value

- generalComment
--- [+] Process.description

- @parentActivityId
--- [-] *no corresponding field*

- @parentActivityContextId
--- [-] *no corresponding field*

- @inheritanceDepth
--- [-] *no corresponding field*

- includedActivitiesStart
--- [-] *no corresponding field* - in the import mapped to description

- includedActivitiesEnd
--- [-] *no corresponding field* - in the import mapped to description

- synonym
--- [-] *no corresponding field* 

- tag
--- [-] *no corresponding field*

- @energyValues
--- [-] *no corresponding field*

- @masterAllocationPropertyId
--- [-] *no corresponding field*

- @masterAllocationPropertyContextId
--- [-] *no corresponding field*

- @masterAllocationPropertyIdOverwrittenByChild
--- [-] *no corresponding field*

- allocationComment
--- [-] *no corresponding field* - in the import mapped to description

- @datasetIcon
--- [-] *no corresponding field*

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
