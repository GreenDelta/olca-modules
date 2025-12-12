# openLCA – modules
This project provides the core functionality of [openLCA](http://openlca.org) as
a set of [Maven](https://maven.apache.org/) modules. Since version 1.4
the [openLCA application](https://github.com/GreenDelta/olca-app) is built on
top of these modules.


## Usage

The modules are available in the [Maven Central Repository](https://repo1.maven.org/maven2/org/openlca/),
so you can just add them as a dependency to your project:

```xml
<dependency>
  <groupId>org.openlca</groupId>
  <artifactId>olca-core</artifactId>
  <version>2.0.0</version>
</dependency>
```

A Java version >= 21 is required.


## Installation from source

In order to build the modules from source, you need to have a [JDK >=21](https://adoptium.net)
and [Maven 3](https://maven.apache.org/install.html) installed. Then, building
the modules can be done like this:

```bash
git clone https://github.com/GreenDelta/olca-modules.git
cd olca-modules
mvn install
```

If the build fails because of failing tests you can skip them via:

```bash
mvn install -DskipTests=true
```

## Content
* olca-core: the openLCA kernel with the openLCA model, the database layer,
  LCA calculations, [JSON-LD](https://github.com/GreenDelta/olca-schema) data
  exchange etc.
* olca-git: a client API to communicate with data repositories using Git
* olca-io: the import-export API of openLCA
* olca-ipc: inter-process communication with openLCA over JSON-RPC/HTTP
* olca-proto-io: data exchange via Protocol Buffers and gRPC

## License
Unless stated otherwise, all source code of the openLCA project is licensed
under the [Mozilla Public License, v. 2.0](http://mozilla.org/MPL/2.0/). Please
see the LICENSE.txt file in the root directory of the source code.
