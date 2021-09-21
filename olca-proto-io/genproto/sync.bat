rem @echo off

rem _ assumes that the olca-schema project is located next to the olca-modules
rem _ project. it then converts the schema definitions into the proto3 message
rem _ types and saves them into the src/main/proto/olca.proto file of the
rem _ olca-proto project.

set home=%cd%

rem change to directory where this script is located
cd %~dp0

rem generate the messages
genproto ..\..\..\olca-schema ..\src\main\proto\olca.proto

cd ..
rem call maven package to generate the Java sources
rem call mvn package -DskipTests=true
call py generate-sources.py

rem back to home
cd %home%
