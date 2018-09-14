# olca-formula
`olca-formula` is the formula interpreter of openLCA. The formula parser
is generated from a grammar file
([FormulaParser.jj](./src/main/java/org/openlca/expressions/FormulaParser.jj))
with [JavaCC](https://javacc.org/). To generate the parser, download JavaCC 5.0
package from https://javacc.org/download (you should find it under
`older versions` on the older page.). At the time this documentation was
written, the current JavaCC version was 6.0 but the download package was
incomplete. Thus, this documentation and the `generate_parser.bat` assume
the the 5.0 package is used (and also, as you see from the batch script, that
this is done under Windows; while it should be easy to adopt this for another
OS).

Extract the JavaCC 5.0 package into the `target` folder of this project (we
choose this folder because it is the standard Maven output folder that is
ignored from Git). The folder structure should then look like this:

```
olca-forumula/
|--- src/
|--- .../
|--- target
     |--- ...
     |--- javacc-5.0
          |--- bin
```

After this, you should be able to generate the parser from the command line:

```batch
cd olca-formula
.\generate_parser.bat
```

Now you can update the grammar file and generate the parser. You should always
run the test suite to make sure that everything still works as before.
