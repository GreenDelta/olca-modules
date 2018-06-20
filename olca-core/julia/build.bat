@echo off

rem delete old versions
for %%i in (libjolcablas.dll libjolcaumf.dll) do (
    if exist libs\%%i (
        echo delete old version of %%i
        del libs\%%i
    )
)

rem comiler flags
set jni_flags=-D_JNI_IMPLEMENTATION_ -Wl,--kill-at
set jni_include="-I%JAVA_HOME%\include" "-I%JAVA_HOME%\include\win32"
set cflags=%jni_flags% -O3 -DNDEBUG %jni_include% -L.\libs -shared

rem compile modules
echo build BLAS bindings: libjolcablas.dll
gcc %cflags% -o libs\libjolcablas.dll blas.c -lopenblas64_

echo build UMFPACK bindings: libjolcaumf.dll
gcc %cflags% -o libs\libjolcaumf.dll umf.c -lumfpack

echo all done
