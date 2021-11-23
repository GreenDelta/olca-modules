# openLCA – modules
This project provides the core functionality of [openLCA](http://openlca.org) as
a set of [Maven](https://maven.apache.org/) modules. Since version version 1.4
the [openLCA application](https://github.com/GreenDelta/olca-app) is built on
top of these components.


## Installation
In order to install the modules, you need to have a [JDK >=13](https://adoptopenjdk.net/)
and [Maven 3](https://maven.apache.org/install.html) installed.
[Download](https://github.com/GreenDelta/olca-modules/archive/master.zip) the
repository (or get it via git), navigate to the root folder and type the
following command in your console:

```bash
cd  olca-modules
mvn install
```

This will build the modules from source and install them into your local Maven
repository. If the build fails because of failing tests you can skip the tests
via:

```bash
mvn install -DskipTests=true
```

## Content
* olca-cloud: a client API to communicate with remote data repositories of the
  [openLCA Collaboration Server](http://www.openlca.org/collaboration-server/)
* olca-core: the openLCA kernel with the openLCA model, the database layer,
  LCA calculations, [JSON-LD](https://github.com/GreenDelta/olca-schema) data
  exchange etc.
* olca-ecospold-1: an API for reading and writing EcoSpold 01 files.
* olca-ecospold-2: an API for reading and writing EcoSpold 02 files.
* olca-formula: the openLCA formula interpreter
* olca-ilcd: an API for reading and writing ILCD data sets with an
  implementation of the ILCD network interface
* olca-io: the import-export API of openLCA


## License
Unless stated otherwise, all source code of the openLCA project is licensed
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please
see the LICENSE.txt file in the root directory of the source code.
