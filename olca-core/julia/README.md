# Julia bindings for openLCA
This folder contains the glue code for linking the high-performance math
libraries from [Julia](https://julialang.org/) with openLCA.

## Running the tests

```bash
echo %OLCA_JULIA%
mvn -Dtest="org.openlca.julia.*Test" test
```

## Library dependencies

### Windows
* using [dependency walker](http://www.dependencywalker.com/)

```
-> libopenblas64_.dll
    -> libgfortran-3.dll
        -> libquadmath-0.dll
        -> libgcc_s_seh-1.dll
        -> libwinpthread-1.dll
-> libumfpack.dll
    -> libopenblas64_.dll
    -> libsuitesparseconfig.dll
    -> libamd.dll
    -> libcholmod.dll
        -> libccolamd.dll
        -> libcamd.dll
        -> libcolamd.dll
```
