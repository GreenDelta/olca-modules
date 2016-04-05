Compiling OpenBLAS on Windows
=============================
In the following the compilation of [OpenBLAS](https://github.com/xianyi/OpenBLAS)
library for Windows is described. In principle this is the same as described in
[OpenBLAS installation guide](https://github.com/xianyi/OpenBLAS/wiki/Installation-Guide)
but with some additional information.


Installing the Toolchain
------------------------
First, you need to install [MSYS and MinGW](http://www.mingw.org/). MSYS is a
collection of GNU utilities (git, make, wget etc.) for Windows and MinWG a
port of the GNU Compiler Collection (g++, gfortran) and GNU Binutils for Windows.

###MSYS
As described in the OpenBLAS installation guide, download the MSYS package from
here: http://sourceforge.net/projects/mingwbuilds/files/external-binary-packages/
and extract it somewhere on your computer. You do not need to add the MSYS
folder to your system path. Just start the msys.bat and it will start a shell
where all GNU utilities you need for the build are available.

###MinGW
For MinGW we take the packages from here: http://sourceforge.net/projects/mingwbuilds/.
For Windows 64 bit, we currently take the last package from this folder:
http://sourceforge.net/projects/mingwbuilds/files/host-windows/releases/4.8.1/64-bit/threads-posix/seh/
(POSIX threads, SEH). Just extract this package under C:/MinGW and add the
C:/MinGW/bin folder to your system path.

To test that everything is working correctly, open the MSYS shell by starting
the msys.bat and test the following commands:

  g++ -v
  make -v
  gfortran -v
  etc.


Getting the OpenBLAS source code
--------------------------------
Open the MSYS shell and navigate to the folder where you want to build the
OpenBLAS library:

    cd <your build path for OpenBLAS>

Then clone the repository:

    git clone https://github.com/xianyi/OpenBLAS.git

After this go into the OpenBLAS folder and check the repository status:

    cd OpenBLAS
    git status

Currently we are on the development branch. To build the stable master we
switch to this branch (or another version tag):

    git checkout master


Compiling OpenBLAS
------------------
To configure the build, open the file Makefile.rule in a text editor and set
the following settings (don't add the comments):

    DYNAMIC_ARCH = 1        # support multiple architectures in one binary
    CC = gcc                # our C compiler
    FC = gfortran           # our Fortran compiler
    BINARY = 64             # or 32, if you build for a 32bit platform
    USE_THREAD = 1          # use threading
    NUM_THREADS = 2         # we assume that we can use at least 2 cores
    NO_CBLAS = 1            # no CBLAS interface
    NO_LAPACKE = 1          # no C interface for LAPACK

After this, just execute the `make` command in the MSYS shell:

    make

The build now takes several minutes. If everything went well you will find a
static and shared library in the build directory.


Using the library
-----------------
When you inspect the resulting library e.g. with
[Dependency Walker](http://www.dependencywalker.com/) you will see that there
are references to MinGW libraries (due to POSIX threading and Fortran symbols).
If you use the static library (libopenblas[version].a) you can statically link
these dependencies to make your programm/library independent from MinGW. For
example if you have the following C file lib.c that referenses the BLAS dcopy_
function:

    extern void dcopy_ (int n, double *x, int incx, double *y, int incy);

    void copy(int n, double *x, double *y) {
        dcopy_(n, x, 1, y, 1);
    }

You can compile it via

    gcc -Wl,--kill-at -static -static-libgcc -O3 -L. -shared -o libcopy.dll lib.c -lopenblas -lgfortran

This expects that you have a libopenblas.a and libgfortran.a (copied from the
MinGW directory) in the same folder. The resulting libcopy.dll should have no
MinGW dependenvies anymore.
