openLCA – modules
=================

** Note that this is not a stable version yet. **

The openLCA modules project includes the basic components of the [openLCA framework]
(http://openlca.org). The aim of this project is to provide a reusable set of libraries 
for the JVM with clear dependencies to other open source frameworks. Starting from 
version 1.4 the openLCA application will be built on of these components.

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



License
-------
Unless stated otherwise, all source code of the openLCA project is licensed under the 
[Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please see the LICENSE.txt
file in this directory.
 
