#!/bin/bash

# delete old versions
if [ -f "libs/libjolcablas.so" ]; then
    echo "Delete old version of libjolcablas"
    rm "libs/libjolcablas.so" 
fi
if [ -f "libs/libjolcaumf.so" ]; then
    echo "Delete old version of libjolcaumf"
    rm "libs/libjolcaumf.so" 
fi

# compiler flags
CFLAGS="-O3 -DNDEBUG -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -L./libs -shared"

echo "Compile new version of libjolcablas"
gcc $CFLAGS -o libs/libjolcablas.so blas_linux.c -lopenblas64_

echo "Compile new version of libjolcaumf"
gcc $CFLAGS -o libs/libjolcaumf.so umf.c -lumfpack

echo "All done"
