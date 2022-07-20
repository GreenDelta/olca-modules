@echo off

protoc ix.proto --java_out=..\java
protoc geo.proto --java_out=..\java
