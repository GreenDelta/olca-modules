openLCA Shell
=============
A simple command line shell for openLCA. 


Loading command files
---------------------
You can load openLCA commands from a file with the load command:

	load C:/path/to/a/file/with/commands.txt

The path of the file can be relative to the current location. Note, if your file
path contains white spaces you have to enclose the path in quotation marks:

	load "relative file path/with white spaces.txt"

In such a file every non-empty line is executed as an openLCA command except the
lines that starts with a hash mark (`#`).


Loading the BLAS library
------------------------
openLCA can calculate using the high performance BLAS library OpenBLAS. To
initialise this library in a session, you can run the `blas` command which
takes a file folder as argument, from which the library should be loaded. If
the folder is empty, openLCA first copies this library into this folder. For
example the following command initialises the BLAS library:

	blas C:\Users\Dell\Downloads


Connect to a database
---------------------

### MySQL
The command to connect to a MySQL database is:

	mysql <host>:<port>/<database> <user> <optional password>
	
The password parameter is optional. For example, this connects to the database
`openlca`:

	mysql localhost:3306/openlca root
	
	
### Derby 
The command to connect to a derby database is:

	derby <path to directory> <optional type>

The first argument must be a directory where the database is located. If the
directory not yet exist, it is created. In this case you can pass an additional
argument that indicates the type of the database that should be created. The
allowed types for the database are:

* empty: an empty database with no data is created
* units: the database is filled with the openLCA reference units and flow 
  properties
* full: the database is created with all the reference data of openLCA


SQL
---

If there is a connection to a database, you can execute SQL statements using the
`sql` command. All SQL statements in the respective database dialect (MySQL, 
Derby) are allowed. If you execute a SELECT query the results of the query are
printed on the screen. Examples for SQL commands are

	sql select * from tbl_flows where name like 'a%'
	
or

	sql update tbl_flows set flow_type = 'ELEMENTARY_FLOW'
	
Additionally, it is possible to execute a file with SQL commands via the 
`sql+file` command:

	sql+file <a path to a file> <optional encoding>
	
As an example, this can be used to load the reference data into a MySQL database.	
	
	
Import
------

The `import` command allows to import data sets from different formats:

	import <format> <path to file or directory>
	
The following formats are supported:

* EcoSpold_1: the path must be an XML file, a ZIP file with  XML files, 
  or a directory with XML or ZIP files
* EcoSpold_2: the path must be a SPOLD file, a ZIP file with SPOLD files, 
  or a directory with SPOLD and ZIP files
* ILCD: the path must be a ZIP file in the ILCD format

This an import command could look like:

	import EcoSpold_1 C:\...\ecoinvent\ecoinvent2.2\Process_withinfra_raw.zip


Creating a product system
-------------------------

A product system can be created from a process with the `make_system` command:

	make_system <ID of process>
	
This will create a new product system in the database using the process with
the given ID as reference process and the quantitative reference of this 
process as reference exchange. 

  	
Solving a product system
------------------------

	solve <process-id>#<product-flow-id>
	
e.g.

	solve 20309#20305