openLCA Shell
=============
A simple command line shell for openLCA. 


Loading command files
---------------------
You can store openLCA commands from a file with the load command:

	load C:/path/to/a/file/with/commands.txt

The path of the file can be relative to the current location. Note, if you file
path contains white spaces you have to enclose the path in quotation marks:

	load "relative file path/with white spaces.txt"

In such a file every non-empty line is executed as openLCA command except the
lines that starts with a hash mark (`#`).

Connect to a database
---------------------

### MySQL
The command to connect to a MySQL database is:

	mysql <host>:<port>/<database> <user> <password>
	
The password parameter is optional. For example, this connects to the database
`openlca`:

	mysql localhost:3306/openlca root
	

