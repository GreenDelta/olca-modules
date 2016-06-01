olca-simapro-csv
================
This is an API for writing and reading data sets in the SimaPro CSV format. 
There is no formal specification of the format available but it is quite easy to
understand it when you export data sets from SimaPro and look into the files. In
the following the overall structure of the format and this API are described. If
you just want to get started, here is a quick example that prints all process
names from a SimaPro CSV file:

```java
SimaProCSV.parse(csvFile, new Object() {
	@BlockHandler
	public void handle(ProcessBlock block) {
		System.out.println(block.getName());
	}
});
```


The file header
---------------
Each data set starts with a file header which looks like this:

```csv
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
```

This header starts with the first line and each header entry is enclosed in 
curly brackets. Before you read the actual data from the file you need this 
information to parse the data into the correct format. Thus, there is a 
`FileHeaderReader` which just reads this information from a file:

```java
FileHeaderReader reader = new FileHeaderReader(<file or reader>);
SPFileHeader header = reader.read();
```
	

Blocks, sections, and rows
--------------------------
After the header a SimaPro CSV file contains a set of blocks with data. Each
data block starts with a header and ends with the keyword `End`. For example
the following is a block with quantity entries. 

```csv
Quantities
Mass;Yes
Length;Yes

End
```
	
A block can contain data rows directly, like in the example above, or contain
sections with data rows. For example a process block starts with the header
`Process` and contains a set of sections like `Category type`, 
`Process identifier`, etc: 

```csv
Process

Category type
material

Process identifier
DefaultX25250700002

Type
Unit process

...

End
```

As for the blocks each section has a header but it does not end with the keyword 
`End` but with an empty line. Data rows of a block or section are directly 
located in the next line under the header. A section of a block starts with an
empty line.

This model is reflected in the types `Block`, `Section`, and `IDataRow` of this
API. A section can be retrieved by its name from a block as the section name is
always unique within a block: 

```java
Block block = ...
Section section = block.getSection(<section name>);
if (section != null) {
    for(String dataRow : section.dataRows) {
        // do something
    }
}
```

Reading data sets
-----------------

For accessing these blocks and sections from a file there is a block reader in
this API. You can use the `BlockReader` like a standard `BufferedReader`:

	try (BlockReader reader = new BlockReader(s)) {
		Block block = null;
		while ((block = reader.read()) != null) {
			// do something with the block
		}
	}


File encoding
-------------
SimaPro is a Windows program and thus we use 
[Windows-1252](http://en.wikipedia.org/wiki/Windows-1252) as default file 
encoding for reading and writing. However, you can set the file encoding in the
constructor calls of the respective readers and writers.


Encoding multi-line strings
---------------------------

TODO: add doc


TODO
----
* Fields and rows of waste scenarios are not yet implemented
* LCIA methods are not yet implemented 
* The first version of that API contained a much more functionality regarding 
  the pedigree matrix but the code may is a bit to complicated (see the archive
  folder). The current implementation handles the pedigree information just as
  a string.  