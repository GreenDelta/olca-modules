olca-simapro-csv
================
This is a simple API for writing and reading data sets in the SimaPro 
CSV format.

TODO
----
* The writer currently creates a file in the given directory with the
  name of the data set's project as file name. Maybe it would be better
  if the method would accept a CSV file which is written.
* The CSVReader currently loads the complete data set into memory; the
  alternative CSVParser should be merged with the reader.