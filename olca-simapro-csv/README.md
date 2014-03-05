olca-simapro-csv
================
This is an API for writing and reading data sets in the SimaPro CSV format. 
There is no formal specification of the format available but it is quite easy to
understand it when you export data sets from SimaPro and look into the files. In
the following the overall structure of the format and this API are described. 

The file header
---------------
Each data set starts with a file header which looks like this:

	{SimaPro 8.0}
	{processes}
	{Date: 05.03.2014}
	{Time: 09:27:45}
	{Project: Test}
	{CSV Format version: 7.0.0}
	{CSV separator: Semicolon}
	{Decimal separator: ,}
	{Date separator: .}
	{Short date format: dd.MM.yyyy}

This header starts with the first line and each header entry is enclosed in 
curly brackets. Before you read the actual data from the file you need this 
information to parse the data into the correct format. Thus, there is a file 
header reader which just reads this information from a file:

	FileHeaderReader reader = new FileHeaderReader(<file or reader>);
	SPFileHeader header = reader.read();
	




TODO
----
* The first version of that API contained a much more functionality regarding 
  the pedigree matrix but the code may is a bit to complicated (see the archive
  folder). The current implementation handles the pedigree information just as
  a string.  