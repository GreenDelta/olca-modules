olca-ilcd
=========
An API for the ILCD format and the ILCD data network.

JAXB configuration
==================
The ILCD API was initially generated via JAXB. The JAXB configuration
files that were used are located under `jaxb_config`.

* Ant task jaxb.build.xml makes the work (requires a Python installation)
* a folder jaxb is expected for the build which contains the libraries for 
  running XJC (the XML Schema to Java Compiler)
* folder schemas: contains the schemas for the ILCD format and the Network API
* folder xjc_config: binding configuration of XJC
	* file: binding_template.xml - used to generate the file bindings.xml
	* file: bindings_descriptors.xml - handwritten file to configure the generation of the Network API format classes (descriptors)
	* file: bindings.py - generates the bindings.xml using the files field_binding_def.csv, schema_binding_def.csv, and type_binding_def.csv
	* file: bindings.xml - generated file to configure the generation of the ILCD format classes

