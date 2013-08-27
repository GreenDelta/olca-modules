olca-core
=========

This package contains the core model, data persistence, and calculation 
functionalities of the openLCA framework. 

Missing Maven dependencies
--------------------------
This package has a dependency to the [ojalgo](http://ojalgo.org/) library.
There is a version of ojalgo in the central Maven repository but this is
a bit old. The version that openLCA currently uses is v34 which you can
get from the [ojalgo](http://ojalgo.org/) web-site and install via Maven 
into your local repository:

    mvn install:install-file -Dfile=ojalgo-34.0.jar -DgroupId=org.ojalgo \
    -DartifactId=ojalgo -Dversion=34.0 -Dpackaging=jar


Persistence
-----------

MySQL or Derby


Simple calculation
------------------


