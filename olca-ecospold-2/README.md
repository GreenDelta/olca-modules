openLCA EcoSpold v2 API
======================
This module provides an easy API to read and write EcoSpold v2 data sets. The 
following snippet shows how you can read or write a data set using this API:
	
	// read a data set from a file or input stream
	DataSet dataSet = EcoSpold02.read(aFileOrStream);
	
	// write a data set to a file or output stream
	EcoSpold02.write(aFileOrStream);
	
Dependencies
------------
Internally, we use [JDOM 2](https://github.com/hunterhacker/jdom) to map XML to
this API and back again.

TODO
----
* properties under exchanges
* parameters in exchanges
* mathematical formulas of exchanges
* other uncertainty distributions
* meta information: modeling and validation and administrative information