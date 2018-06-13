## olca-core
This package contains the core model, data persistence, and calculation 
functionalities of the openLCA framework. 

## Persistence
We use [EclipseLink](http://www.eclipse.org/eclipselink/) as persistence 
framework with support for MySQL and Derby in embedded mode. 
The schema files for these databases can be found in the 
[resources folder](src/main/resources/org/openlca/core/database/internal) 
(current_schema_derby.sql is the schema for Derby, current_schema_mysql.sql 
is the schema for MySQL). In this folder, also the openLCA reference data are 
contained as SQL scripts. 
	