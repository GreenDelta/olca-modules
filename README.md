openLCA – modules
=================
This project provides a reusable set of libraries for the JVM with clear 
dependencies to other open source libraries for the
[openLCA framework](http://openlca.org). Since version version 1.4 the 
[openLCA application](https://github.com/GreenDelta/olca-app) is built on top of 
these components.


Content
-------
* olca-core: the openLCA kernel with the openLCA model, the database layer, 
  LCA calculation, etc.
* olca-ecospold-1: an API for reading and writing EcoSpold 01 files.
* olca-ecospold-2: an API for reading and writing EcoSpold 02 files.
* olca-eigen: a JNI wrapper for the high performance math libraries 
  [Eigen](https://bitbucket.org/eigen/eigen/) and 
  [OpenBLAS](http://xianyi.github.io/OpenBLAS/).
* olca-formula: the openLCA formula iterpreter
* olca-geo: provides tools for regionalized LCIA calculation based on shapefiles
  and KML definitions
* olca-ilcd: an API for reading and writing ILCD data sets with an 
  implementation of the ILCD network interface
* olca-io: the import-export API of openLCA
* olca-simapro-csv: an API for reading and writing SimaPro CSV files. 


Building
--------
To compile the modules you need to have a 
[Java Development Kit >= 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
and [Maven](http://maven.apache.org/) installed. Download the repository, 
navigate to the root folder and type the following command in your console:

	mvn install

This will build the modules from source and install them into your local 
Maven repository. If the build failes because of failing tests you can skip the
tests via:

	mvn install -DskipTests=true


License
-------
Unless stated otherwise, all source code of the openLCA project is licensed 
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please 
see the LICENSE.txt file in the root directory of the source code.
 
