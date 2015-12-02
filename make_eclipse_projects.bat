@echo off

REM  Adds Eclipse project for every sub-folder matching olca-*. If there are
REM  already Eclipse project files in these folders we overwrite them.

FOR /D %%G IN (olca-*) DO (
	
	CD %%G
	
	REM  delete old project files
	IF EXIST .classpath DEL .classpath
	IF EXIST .project   DEL .project
	IF EXIST .settings  RD  /S /Q .settings
	
	REM  generate new files
	mvn eclipse:eclipse
	
	cd ..
)