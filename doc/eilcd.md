# Implementation of the eILCD format in openLCA
The eILCD format is an extension to the ILCD format to exchange life cycle
models. It was developed in the PEF remodelling project to enable data exchange
of such models between different LCA tools. In principle, such a life cycle
model data set is the same as a product system model in openLCA: it stores the
life cycle model of a product (the reference product) as a set of connected
process data sets where the process data set that produces this reference
product is the reference process of the model.

However, there are concepts in the eILCD format that are very different to the
concepts of product systems in openLCA. How these concepts of eILCD models are
mapped to openLCA product systems, and the other way around, are described in
the following sections.

## Process instances
One key element of the eILCD format is the concept of process instances which
are stored under the `processes` element:

```xml
<processes>
  <processInstance dataSetInternalID="0">
    <referenceToProcess>...</referenceToProcess>
    <groups>...</groups>
    <connections>...</connections>
  </processInstance>
  ...
</processes>
```

A process instance describes the occurrence of a process in the life cycle model.
It is identified by a data set internal ID and contains a reference to the
respective process data set, references to groups (life cycle stages) to which
this process instance belongs (see below), and connections to other process
instances in the life cycle model. For each process in the life cycle model
there is at least one process instance. However, it is also possible to have
multiple process instances of the same process in the system, e.g. to model the
occurrence of the same process in different life cycle stages (e.g. for
electricity in the production and use phase).

The concept of process instances is a nice feature and we may have to think
about it when we want to implement life cycle stages in openLCA (but then we
also have to think about the mapping to matrices in the calculation as this
could blow up matrix sizes). Currently, openLCA stores a single reference to
each process in the product system so that when importing an eILCD model into
openLCA **all instances of a process are merged to single process reference**.

## Groups / life cycle stages
With the group concept in the eILCD format it is possible to declare things like
life cycle stages in a product system model:

```xml
<groupDeclarations>
  <group id="0">
    <groupName xml:lang="en">Use phase</groupName>
  </group>
</groupDeclarations>
```

A process instance can be then linked to a group via the data set internal ID:

```xml
<processInstance>
  ...
  <groups>
    <memberOf groupId="0"/>
  </groups>
  ...
</processInstance>
```

As described above, in the eILCD format a process can have multiple process
instances which can link to different life cycle stages (e.g. to model
electricity in the production and use phase). In openLCA there is a similar
concept of grouping processes for results, where the groups can also be
stored. However, **there is currently no concept for storing life cycle stages**
**and linking processes to these stages in openLCA product systems**.

## Process connections
Connections between processes are stored under the `connections` element within
a process instance element:

```xml
<processInstance dataSetInternalID="0">
  <connections>
    <outputExchange flowUUID="7601...">
      <downstreamProcess id="1" flowUUID="1d02..." />
    </outputExchange>
    ...
  </connections>
</processInstance>
```

An output of the process, identified by the UUID of the flow of the respective
exchange, can be linked to an input of a downstream process (instance) also 
identified by the respective flow UUID. This linking model of the eILCD format
has quite some differences compared to the process links stored in openLCA
product systems which are described in more detail in the following sections.

### Multiple exchanges with the same flow
The `downstreamProcess` element can have an additional `location` attribute
which can be used to identify an input when there are multiple inputs with the
same flow in the downstream process. Thus, if there are multiple inputs of the
same flow in a downstream process (which is typical for electricity mixes for
example) they need to have different locations in order to be linked correctly
(the `exchange` element of an ILCD process data set allows the specification of
locations).

In openLCA, it is also possible to have multiple inputs of the same flow in a
process that can be linked from different processes but we use the exchange ID
to identify the exchanges in a link. **We do not have a location attribute for**
**exchanges in openLCA**. Also, in openLCA it is also possible to link multiple
outputs with the same waste flow to different waste treatment processes (again
identified by exchange IDs). While this is not a problem for the import, not
every model in openLCA can be correctly represented in the eILCD format (only
with artificial location codes).

### Linking different flows
In the eILCD format the input and output exchange of a process link can have
different flows respectively. There is a `dominant` attribute for the
`outputExchange` and `downstreamProcess` in order to specify the flow property
for display and calculation if both flows do not support the same flow property
(however, the question here is how something like that can be calculated then).

**In openLCA, only the same flow can be used in process links on the input and**
**output side** (although we have plans to allow linking of flows that have the
same reference flow property). For such links with different flows, artificial
connection processes are currently created in the import to overcome this:

```
eILCD link with different flows:

+---------------+     +---------------+
|process1::flow1| --> |flow2::process2|
+---------------+     +---------------+

import result in openLCA

+---------------+     +-----------------------+     +---------------+
|process1::flow1| --> |flow1::connector::flow2| --> |flow2::process2|
+---------------+     +-----------------------+     +---------------+
```

## Other fields and meta-data
Beside the differences in the models the eILCD format specifies additional
fields and meta-data elements very similar to the ILCD process data sets that
are currently not supported in openLCA product systems:

* References to resulting processes `/referenceToResultingProcess`:
  openLCA does not save references to calculated LCI results in a product system.
  In general, it is possible to calculate an unlimited number of LCI results from
  a product system by changing parameters and things like allocation methods etc.
* External documentation links `/referenceToExternalDocumentation`
* External chart links `referenceToDiagram` (openLCA dynamically creates charts
  for product systems)
* Most of the information under the `modellingAndValidation` and
  `administrativeInformation` elements (`useAdviceForDataSet`, 
  `complianceDeclarations`, `commissionerAndGoal`,  `review`, `dataGenerator`,
  `dataEntryBy`, `publicationAndOwnership` etc.)
