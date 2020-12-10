__Using the openLCA gRPC Server__

To run the openLCA gRPC server just extract to distribution package to a folder
with write permissions. The package contains a `run` script with which the
server can be started from the command line via:

```
run -db <database> [-port <port>]
```

The `-db` parameter takes the name of an openLCA database which is stored in
the default openLCA data folder. The openLCA default folder is the directory
`openLCA-data-1.4` in the respective user directory, e.g.
`C:\Users\<user name>\openLCA-data-1.4` on Windows. Databases are stored in
the `databases` sub-folder of this directory.

The `-port` parameter is optional and defaults to `8080` if not specified. For
example, the following command:

```
run -db ei2 -port 9999
```

will start the server at port `9999` with a connection to the `ei2` database
located in the `~/openLCA-data-1.4/databases/ei2` folder. Note that the gRPC
server and openLCA cannot access the same database simultaneously. So you have
to close the database in openLCA when you want to use it via the gRPC server.
