@echo off

set JAVACC_PATH=.\target\javacc-5.0\bin

if not exist %JAVACC_PATH% (
  echo could not find %JAVACC_PATH%
  echo download JavaCC 5.0 from https://javacc.org/download
  echo and extract it to the target folder
  goto :end
)

echo Compile parser
cd %JAVACC_PATH%
set OUT_DIR=.\..\..\..\src\main\java\org\openlca\expressions
set GRAMMAR=%OUT_DIR%\FormulaParser.jj

call javacc.bat -OUTPUT_DIRECTORY:%OUT_DIR% -GRAMMAR_ENCODING:"UTF-8" -JDK_VERSION:"1.8" %GRAMMAR%

cd .\..\..\..
:end
