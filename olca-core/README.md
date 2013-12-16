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

    mvn install:install-file -Dfile=ojalgo-34.8.jar -DgroupId=org.ojalgo \
    -DartifactId=ojalgo -Dversion=34.8 -Dpackaging=jar

On some platforms you may have to [quote the arguments](http://bit.ly/18k1Bli). 

Persistence
-----------
We use [EclipseLink](http://www.eclipse.org/eclipselink/) as persistence 
framework with support for MySQL and Derby in embedded mode. 
The schema files for these databases can be found in the 
[resources folder](src/main/resources/org/openlca/core/database/internal) 
(current_schema_derby.sql is the schema for Derby, current_schema_mysql.sql 
is the schema for MySQL). In this folder, also the openLCA reference data are 
contained as SQL scripts. 



	