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

- name
--- [+] Exchange.Flow.name

- @id
--- [+] Exchange.id

- @amount
--- [+] Exchange.amountValue

- @unitId
--- [+] Exchange.Unit.refId

- @unitContextId
--- [-] *no corresponding field*

- unitName
--- [+] Exchange.Unit.name

- @variableName
--- [-] *no corresponding field*

- @isCalculatedAmount
--- [-] *no corresponding field*

- @mathematicalRelation
--- [+] Exchange.amountFormula

- @casNumber
--- [+] Exchange.Flow.casNumber

- comment
--- [-] *no corresponding field*

- @sourceId
--- [-] *no corresponding field*

- @sourceContextId
--- [-] *no corresponding field*

- @sourceIdOverwrittenByChild
--- [-] *no corresponding field*

- @sourceYear
--- [-] *no corresponding field*

- @sourceFirstAuthor
--- [-] *no corresponding field*

- @pageNumbers
--- [-] *no corresponding field*

- @specificAllocationPropertyId
--- [-] *no corresponding field*

- @specificAllocationPropertyContextId
--- [-] *no corresponding field*

- @specificAllocationPropertyIdOverwrittenByChild
--- [-] *no corresponding field*

- transferCoefficient
--- [-] *no corresponding field*

- uncertainty
--- [+] Exchange.uncertainty

- property
--- [+] Exchange.Flow.flowPropertyFactors (*)

- tag
--- [-] *no corresponding field*

- synonym
--- [-] *no corresponding field*

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

- @activityLinkContextId
--- [-] *no corresponding field*

- @activityLinkIdOverwrittenByChild
--- [-] *no corresponding field*

- @productionVolumeAmount
--- [-] *no corresponding field*

- @productionVolumeSourceIdOverwrittenByChild
--- [-] *no corresponding field*

- @productionVolumeVariableName
--- [-] *no corresponding field*

- @productionVolumeSourceId
--- [-] *no corresponding field*

- @productionVolumeMathematicalRelation
--- [-] *no corresponding field*

- @productionVolumeComment
--- [-] *no corresponding field*

- @productionVolumeSourceContextId
--- [-] *no corresponding field*

- @productionVolumeSourceYear
--- [-] *no corresponding field*

- @productionVolumeSourceFirstAuthor
--- [-] *no corresponding field*

- productionVolumeUncertainty
--- [-] *no corresponding field*

- classification
--- [+] Exchange.Flow.category

#### Elementary exchanges

- inputGroup
--- [+] Exchange.Flow.flowType

- outputGroup
--- [+] Exchange.Flow.flowType

- @elementaryExchangeId
--- [+] Exchange.Flow.refId

- @elementaryExchangeContextId
--- [-] *no corresponding field*

- compartment
--- [+] Exchange.Flow.category

- @formula
--- [+] Exchange.Flow.formula

#### Parameters

- name
--- [-] *no corresponding field*

- @id
--- [+] Parameter.id

- @parameterContextId
--- [-] *no corresponding field*

- @amount
--- [+] Parameter.value

- @parameterContextId
--- [-] *no corresponding field*

- @unitId
--- [-] *no corresponding field*

- @unitContextId
--- [-] *no corresponding field*

- unitName
--- [-] *no corresponding field*

- @variableName
--- [+] Parameter.name

- @unitIdOverwrittenByChild
--- [-] *no corresponding field*

- @mathematicalRelation
--- [+] Parameter.name

- @isCalculatedAmount
--- [+] Parameter.inputParameter

- uncertainty
--- [-] *no corresponding field* (* will be added soon)

- comment
--- [-] *no corresponding field* 

### Modelling and validation

#### Representativeness (optional)

- @percent (double, optional)
--- *no corresponding field*
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
