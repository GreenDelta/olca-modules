@echo off

protoc lib.proto --java_out=..\java
protoc geo.proto --java_out=..\java
