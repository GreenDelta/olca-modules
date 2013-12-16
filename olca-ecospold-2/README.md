openLCA EcoSpold v2 API
======================
This module provides an easy API to read and write EcoSpold v2 data sets. The 
following snippet shows how you can read or write a data set using this API:
	
	// read a data set from a file or input stream
	DataSet dataSet = EcoSpold02.read(aFileOrStream);
	
	// write a data set to a file or output stream
	EcoSpold02.write(dataSet, aFileOrStream);
	
Dependencies
------------
Internally, we use [JDOM 2](https://github.com/hunterhacker/jdom) to map XML to
the domain model and back again.

License
-------
The Java API of this project is licensed under the 
[Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). This project 
also contains the EcoSpold 2 schema files wich are available from the
[ecoinvent website](http://www.ecoinvent.org/). These schema files are licensed
under the [EcoSpold Public License Version 1.0](http://www.ecoinvent.org/fileadmin/documents/en/espl.pdf).

