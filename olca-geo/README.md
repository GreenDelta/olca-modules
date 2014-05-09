olca-geo
========
This is a first implementation of localized impact assessment based on
shapefiles and KML definitions. Shapefiles are used to store localized parameters
of impact assessment methods which can be used in formulas for characterisation
factors. These parameters are attributes of features in a shapefile and are
referenced from the impact assessment method via an input parameter with the
same name and a reference to the shapefile. To bind process locations to
geometries in such shapefiles we use KML which is stored as a zipped byte arrays
in the database. The KML of a process can be defined directly on the process
level or at the location which is referenced by the process.

TODO
----
* We currently calculate the parameter set for every process product. If there
  are multiple processes that have the same KML feature these parameter sets
  are calculated multiple times.
* The calculation currently does not work for multi-polygon KML features. This
  is because the geo-tools API does not return a geometry for such types.
* Overlapping features in shapefiles are not handled and may produce wrong
  results.
* See also the log-messages when running the tests.
