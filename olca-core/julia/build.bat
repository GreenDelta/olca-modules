@echo off

set BUILD_LIB=libs\libjolca.dll
set JNI_FLAGS=-D_JNI_IMPLEMENTATION_ -Wl,--kill-at
set JNI="-IC:\Program Files\Java\jdk1.8.0_131\include" "-IC:\Program Files\Java\jdk1.8.0_131\include\win32"

if exist %BUILD_LIB% (
    echo delete old version of %BUILD_LIB%
    del %BUILD_LIB%
)

echo compile new version of %BUILD_LIB%
g++ %JNI_FLAGS% -O3 -DNDEBUG %JNI% -L.\libs -shared -o %BUILD_LIB% lib.c -lopenblas64_ -lumfpack

echo all done
