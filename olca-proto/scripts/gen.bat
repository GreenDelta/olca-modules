@echo off

rem this script generates the code for our proto3 messages and services. it
rem should be executed from the root folder of this project: scripts\gen.bat
rem in order to run the script you need to have the following tools installed:
rem * the protoc compiler: https://github.com/protocolbuffers/protobuf/releases
rem * the protoc-gen-grpc-java plugin: https://search.maven.org/artifact/io.grpc/protoc-gen-grpc-java
rem   just download the binary from search.maven.org and put it into your path

protoc -I.\proto --java_out=src\main\java .\proto\olca.proto
protoc -I.\proto --java_out=src\main\java .\proto\services.proto
protoc -I.\proto --plugin=protoc-gen-grpc-java.exe --grpc-java_out=src\main\java .\proto\services.proto

rem Go
rem protoc olca.proto --go_out=go\protolca
