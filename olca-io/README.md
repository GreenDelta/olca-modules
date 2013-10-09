The openLCA import - export API
===============================
This API provides import and export functionalities for LCA data formats like
EcoSpold 01/02 and ILCD. 

EcoSpold 02
===========

## Import

An import of a set of EcoSpold 02 files can be done via:

	EcoSpold2Import es2Import = new EcoSpold2Import(aDatabase);
	es2Import.run(anArrayOfFiles);	
	
The import accepts `*.spold`-files in the EcoSpold 02 format and `*.zip`-files
containing such files.


### Process categorisation

The EcoSpold 02 format allows the classification of a process data set for 
multiple classifications. A classification section in a data set may look like 
this:

	<classification classificationId="359b3be8-bc56-4abf-a04b-294115165214">
		<classificationSystem xml:lang="en">EcoSpold01Categories</classificationSystem>
	 	<classificationValue xml:lang="en">agricultural means of production/mineral fertiliser</classificationValue>
	</classification>
	<classification classificationId="a2cfa5cb-7e0a-4b16-88d6-75bececda629">
		<classificationSystem xml:lang="en">ISIC rev.4 ecoinvent</classificationSystem>
		<classificationValue xml:lang="en">0112:Growing of rice</classificationValue>
	</classification>
	
There is now hierarchy defined for these classifications (but it could be 
derived). OpenLCA currently only supports a single categorisation of processes.
Thus, we take the ISIC classes in the import (if available).


### Flow categorisation

The current version of the ecoinvent 3 database does not provide categories for
product flows (intermediate exchanges). Thus, we take the category of a process
also for the respective output product.


### Units and flow properties

The EcoSpold 02 format has no concept of unit groups like the ILCD format and 
openLCA. Also the concept of flow properties is different in EcoSpold 02 as 
the value for a flow property can be different in every input/output. In
contrary, flow properties in ILCD and openLCA are defined for a flow and 
the amounts for these flow properties are convertible. 

The import currently maps units from EcoSpold 02 that directly can be mapped to 
an openLCA unit (with unit group and flow property). This is a fixed list and
can be found in the file `ei3_unit_map.csv`. 

### Parameters

It is currently not possible to evaluate all parameters of the ecoinvent 
database with openLCA because of the following reasons:

* there are `LiveLink` functions which link to system local Excel tables in 
  formulas of some data sets
* there are functions like `UnitConversion` (would mean that a formula
  interpreter knows units) or `Ref(<UUID>)` (would mean that we have have the
  same context UUIDs in our data sets available in the calculation)   

Thus, by default we do not import the parameters. However, the import of 
parameters can be enabled via

     EcoSpold2Import e2Import = ...
     e2Import.setImportParameters(true);
     
     
TODO
----
* import unknown units and handle flow properties (see problems above)

 
 
