#!/bin/bash

BUILD_LIB="libs/libjolca.so"
JNI_FLAGS=""
JNI="-I$JAVA_HOME/include -I$JAVA_HOME/include/linux"

if [ -f $BUILD_LIB ]; then
    echo "Delete old version of $BUILD_LIB"
    rm $BUILD_LIB 
fi

echo "Compile new version of $BUILD_LIB"
gcc $JNI_FLAGS -O3 -DNDEBUG $JNI -L./libs -shared -o $BUILD_LIB lib.c -lopenblas64_ -lumfpack

echo "All done"
