# eILCD Import and Export
eILCD is an extension of the [ILCD format](http://eplca.jrc.ec.europa.eu/LCDN/developer.xhtml)
to support the exchange of life cycle models (product systems). It was developed
in ... **TODO PROJECT NAME**.

openLCA supports the exchange of product systems in the eILCD format. However,
not all format features are currently supported:

* `dataSetInformation/referenceToResultingProcess`:
  openLCA does not save references to calculated LCI results in a product system.
  In general, it is possible to calculate an unlimited number of LCI results from
  a product systems by changing parameters and things like allocation methods etc.
* `dataSetInformation/referenceToExternalDocumentation`
* `technology/groupDeclarations/...`:
  openLCA currently not supports the grouping of processes in a product system
  into life cycle stages. However, this is something we want to implement in a
  future version.
* `technology/referenceToDiagram`
* `modellingAndValidation`: all the information under this element are currently
  not supported in product systems in openLCA
* `administrativeInformation`: only timestamp and version are currently supported
