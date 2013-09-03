openLCA – modules
=================

**Note that this is not a stable version yet.**

The openLCA modules project includes the basic components of the 
[openLCA framework](http://openlca.org). The aim of this project is to provide a 
reusable set of libraries for the JVM with clear dependencies to other open source 
frameworks. Starting from version 1.4 the openLCA application will be built on top of 
these components.


Content
-------
* olca-blas: a Java BLAS and LAPACK API based on [jBlas](http://mikiobraun.github.io/jblas/)
  and [OpenBLAS](http://xianyi.github.io/OpenBLAS/).
* olca-core: the openLCA kernel with the openLCA model, the database layer, LCA calculation,
  etc.
* olca-ecospold-1: an API for reading and writing EcoSpold 01 files.
* olca-ecospold-2: an API for reading and writing EcoSpold 02 files.
* olca-formula: the openLCA formula iterpreter
* olca-ilcd: an API for reading and writing ILCD data sets with an implementation of the ILCD
  network interface
* olca-io: the import-export API of openLCA
* olca-simapro-csv: an API for reading and writing SimaPro CSV files. 


Building
--------
This package has a dependency to the [ojalgo](http://ojalgo.org/) library.
There is a version of ojalgo in the central Maven repository but this is
a bit old. The version that openLCA currently uses is v34 which you can
get from the [ojalgo](http://ojalgo.org/) web-site and install via Maven 
into your local repository:

    mvn install:install-file -Dfile=ojalgo-34.0.jar -DgroupId=org.ojalgo \
    -DartifactId=ojalgo -Dversion=34.0 -Dpackaging=jar

On some platforms you may have to [quote the arguments](http://bit.ly/18k1Bli).

All components in the modules project are [Maven](http://maven.apache.org/) projects. You 
can build each module separately or all together via the POM in the root directory:

    mvn install
	
As this is not yet a stable version, may not all tests work and the build may fail. You
can skip the tests and the build should work:

    mvn install -DskipTests=true


License
-------
Unless stated otherwise, all source code of the openLCA project is licensed under the 
[Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please see the LICENSE.txt
file in the root directory of the source code.
 
