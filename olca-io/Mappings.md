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

Field mappings: 

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

Field mappings: 

- classificationSystem
--- [-] *no corresponding field* - but we use this field to tag openLCA
categories

- classificationValue
--- [+] Category.name

- @classificationId
--- [+] Category.refId

- @classificationContextId
--- [-] *no corresponding field*


#### Geography

- @geographyId
--- [+] Process.Location.refId

- @geographyContextId
--- [-] *no corresponding field*

- shortname
--- [+] Process.Location.code

- comment
--- [+] ProcessDocumentation.geography

#### Technology

- @technologyLevel
--- [-] *no corresponding field*

- comment
--- [+] ProcessDocumentation.technology

#### Time period

- @startDate
--- [+] ProcessDocumentation.validFrom

- @endDate
--- [+] ProcessDocumentation.validUntil

- @isDataValidForEntirePeriod
--- [-] *no corresponding field*

- comment
--- [+] ProcessDocumentation.time

#### Macro-economic scenario

- @macroEconomicScenarioId
--- [-] *no corresponding field*

- @macroEconomicScenarioContextId
--- [-] *no corresponding field*

- @name
--- [-] *no corresponding field*

- @comment
--- [-] *no corresponding field*

### Flow data

#### Exchanges



#### Intermediate exchanges

- inputGroup
--- [+] Exchange.Flow.flowType

- outputGroup
--- [+] Exchange.Flow.flowType / Process.quantitativeReference

- @intermediateExchangeId
--- [+] Exchange.Flow.refId

- @intermediateExchangeContextId
--- [-] *no corresponding field*

- @activityLinkId
--- [+] Exchange.defaultProvider.refId

@activityLinkContextId
--- [-] *no corresponding field*

@activityLinkIdOverwrittenByChild
--- [-] *no corresponding field*

@productionVolumeAmount
--- [-] *no corresponding field*

@productionVolumeSourceIdOverwrittenByChild
--- [-] *no corresponding field*

@productionVolumeVariableName
--- [-] *no corresponding field*

@productionVolumeSourceId
--- [-] *no corresponding field*

@productionVolumeMathematicalRelation
--- [-] *no corresponding field*

@productionVolumeComment
--- [-] *no corresponding field*

@productionVolumeSourceContextId
--- [-] *no corresponding field*

@productionVolumeSourceYear
--- [-] *no corresponding field*

@productionVolumeSourceFirstAuthor
--- [-] *no corresponding field*

productionVolumeUncertainty
--- [-] *no corresponding field*

classification
--- [+] Exchange.Flow.category

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
