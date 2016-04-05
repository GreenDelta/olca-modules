Database scripting with ij
==========================
We use [Apache Derby](http://db.apache.org/derby/) as database engine in openLCA.
There is a simple SQL query editor available in openLCA but it is also possible
to use the Derby database scripting tool
[ij](http://db.apache.org/derby/papers/DerbyTut/ij_intro.html) to do advanced
database management tasks like bulk exports or imports.

## Installation of ij
To install ij just download the latest version of the Derby libraries and
extract them into a folder:

    - lib    
       * derby.jar
       * ...
       * derbytools.jar

In order to run the tool you need to set an environment variable `JAVA_HOME`
on your system which points to a Java Development Kit. Then you can start the
ij scripting tool via

    java -cp "lib/derby.jar;lib/derbytools.jar" org.apache.derby.tools.ij

You can put this line in a batch or shell script next to the lib folder.

## Connecting to an openLCA database
The openLCA databases are stored in the folder <user dir>/openLCA-data-1.4/databases.
You can connect to a database with the following command (make sure that the
database is not opened in openLCA):

    ij> connect 'jdbc:derby:C:/Users/Besitzer/openLCA-data-1.4/databases/ecoinvent_3_2_apos';

Then you can execute SQL statements, e.g.:

    ij> select id, name from tbl_units;

To close the database and exit the tool type:

    ij> disconnect;
    ij> exit;

## Run SQL files
With the `run` command you can execute a SQL script. For example the
[csv_export.sql](./csv_export.sql) script will export all tables as CSV files:

    ij> run 'csv_export.sql';
