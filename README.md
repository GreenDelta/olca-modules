[![Build Status](https://travis-ci.org/GreenDelta/olca-modules.svg?branch=master)](https://travis-ci.org/GreenDelta/olca-modules)

openLCA – modules
=================
This project provides the core functionality of [openLCA](http://openlca.org) as
a set of [Maven](https://maven.apache.org/) modules. Since version version 1.4 
the [openLCA application](https://github.com/GreenDelta/olca-app) is built on 
top of these components.


Installation
------------
In order to install the modules, you need to have a [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
and [Maven 3](https://maven.apache.org/install.html) installed. [Download](https://github.com/GreenDelta/olca-modules/archive/master.zip) 
the repository (or get it via git), navigate to the root folder and type the 
following command in your console:

	cd  olca-modules
	mvn install

This will build the modules from source and install them into your local 
Maven repository. If the build fails because of failing tests you can skip the
tests via:

	mvn install -DskipTests=true


Content
-------
* olca-cloud: a client API to communicate with remote data repositories
* olca-core: the openLCA kernel with the openLCA model, the database layer, 
  LCA calculation, etc.
* olca-ecospold-1: an API for reading and writing EcoSpold 01 files.
* olca-ecospold-2: an API for reading and writing EcoSpold 02 files.
* olca-formula: the openLCA formula interpreter
* olca-geo: provides tools for regionalized LCIA calculation based on shapefiles
  and KML definitions
* olca-ilcd: an API for reading and writing ILCD data sets with an 
  implementation of the ILCD network interface
* olca-io: the import-export API of openLCA
* olca-jsonld: import/export API for data sets in the [olca-schema](https://github.com/GreenDelta/olca-schema) 
  format
* olca-simapro-csv: an API for reading and writing SimaPro CSV files. 


License
-------
Unless stated otherwise, all source code of the openLCA project is licensed 
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please 
see the LICENSE.txt file in the root directory of the source code.
 
