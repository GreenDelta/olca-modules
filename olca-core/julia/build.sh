#!/bin/bash

# delete old versions
if [ -f "libs/libolcar.so" ]; then
    echo "Delete old version of libolcar"
    rm "libs/libolcar.so"
fi
if [ -f "libs/libolcar_withumf.so" ]; then
    echo "Delete old version of libolcar_withumf"
    rm "libs/libolcar_withumf.so"
fi

# compiler flags
CFLAGS="-O3 -DNDEBUG -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -L./libs -shared"

echo "Compile new version of libolcar"
gcc $CFLAGS -o libs/libolcar.so blas_linux.c -lopenblas64_

echo "Compile new version of libolcar_withumf"
gcc $CFLAGS -o libs/libolcar_withumf.so umf.c -lumfpack

echo "All done"
