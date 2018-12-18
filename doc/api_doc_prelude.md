% The openLCA core API

# How to read this document
This document contains the documentation of the openLCA core API, automatically
extracted from the Java documentation of the
[olca-modules repository](https://github.com/GreenDelta/olca-modules) (using
the [jmd tool](https://github.com/msrocka/jmd)). The headings of this
document follow the structure of the API:

1. Packages (e.g. ` org.openlca.core.results`)
2. Types (mainly classes), e.g. `ContributionResult`
3. Fields (e.g. `techIndex`) and methods (e.g. `getFlows()`)

A heading `TypeB > TypeA` means that the following section is about `TypeB` which
extends `TypeA` and, thus, has all the methods and fields that `TypeA` has. The
heading `fieldC : TypeD` means that `fieldC` is an accessible field (a property) of
type `TypeD`. Finally, a heading `methodG(): TypeE` means that `methodG` is a
callable method that returns an instance of `TypeE`. The parameters of that
method are directly listed after the heading in the same order as they need to
be applied when calling the method. Methods that have the same name as their
type (like `TypeG()`) are constructor methods which create a new instance of
that type.

The openLCA core API is a plain Java API that should run on any Java Virtual
Machine (JVM) >= v8. Thus, you could use it from any JVM language like Java,
Scala, Kotlin, Clojure, Jython, etc. It is also fully exposed to the openLCA
development tools within openLCA (`Window > Developer tools > Python` within
openLCA). The Python scripting environment in openLCA uses
[Jython](http://www.jython.org/) (a Python implementation that runs on the JVM).
The following example shows how the usage of the elements `package`, `type`,
`field`, and `method`:


```python
# import the type 'FlowDao' from the package
# 'org.openlca.core.database'
from org.openlca.core.database import FlowDao

# create a new instance of "FlowDao" by calling
# its constructor method and passing a database
# instance as parameter
dao = FlowDao(db)

# get all flows by calling the method "getAll"
# on the instance of the FlowDao type
flows = dao.getAll()

# writing the value of the field "name" of the
# first flow to the logger output
log.info("Name of the first flow: {}", flows[0].name)
```