# Julia bindings for openLCA
This folder contains the glue code for linking the high-performance math
libraries from [Julia](https://julialang.org/) with openLCA.

## Running the tests

```bash
echo %OLCA_JULIA%
mvn -Dtest="org.openlca.julia.*Test" test
```
