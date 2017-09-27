# Draft: olcamat - A Simple Binary Storage Format for Matrices
This is a specification of a simple binary data format to exchange matrix data
with a focus on simplicity and performance. Simplicity means that it should be
very easy to read and write this format in different languages (preferably using
only the respective standard library; see the code examples below). Performance
means that the format should support LCA based calculations in an optimal way
(e.g. getting a column of a precalculate inverse should only take a few disc
seeks without loading the matrix into memory).

There is currently no user interface for running an olcamat export in openLCA
but a first version of the API is already available. The following Python
script exports a database into the olcamat format:

```python
import java.io.File as File
import org.openlca.core.matrix.io.olcamat.Export as Export

folder = File('C:/Users/Besitzer/Desktop/rems/olcamat/')
export = Export(db, folder)
export.run()
```

## File Header
The `olcamat` format supports different matrix storage types (dense, sparse,
column or row major order, etc.). The file header format is always the same
whereas the layout of the data section depends on the storage type of the matrix
(see below). All values in the header are stored in little endian order (least
significant byte first).


| Byte    | Contents | Description                                             |
|---------|----------|---------------------------------------------------------|
|  0 -  3 | int32    | The version of the format (currently `1`)               |
|  4 -  7 | int32    | The [storage format](#storage-formats)                  |
|  8 - 11 | int32    | The [data type](#data-types) of the matrix entries.      |
| 12 - 15 | int32    | The length in number of bytes of a single matrix entry. |

The last field is only required if the data type is `custom`.

### Storage Formats

| Value | Description                       |
|-------|-----------------------------------|
|  `0`  | Dense array in column-major order |
|  `1`  | Dense array in row-major order    |
|  `2`  | Coordinate format                 |
|  `3`  | Compressed Column Storage (CCS)   |
|  `4`  | Compressed Row Storage (CRS)      |

In the dense storage format, the file header is followed by the number of rows
and columns and then by the values:

| Byte     | Contents  | Description        |
|----------|-----------|--------------------|
| 16 - 19  | int32     | Number of rows     |
| 20 - 23  | int32     | Number of columns  |
| 24 - `N` | def. type | Values             |

In the column-major order the values are written column after column and in the
row major order row after row. The size of `N` can be caluated as:

```
N = 24 + L * rows * columns
```

Where `L` is the size of the data type.

### Data Types

| Value | Description                   |
|-------|-------------------------------|
|  `0`  | 64-bit floating point numbers |


## Index files

### index_A.csv

| Column | Field                |
|--------|----------------------|
|   0    | index                |
|   1    | process ID           |
|   2    | process name         |
|   3    | process type         |
|   4    | process location     |
|   5    | flow ID              |
|   6    | flow name            |
|   7    | flow type            |
|   8    | flow location        |
|   9    | flow property ID     |
|  10    | flow property name   |
|  11    | unit ID              |
|  12    | unit name            |

### index_B.csv

| Column | Field                |
|--------|----------------------|
|  0     | index                |
|  1     | flow ID              |
|  2     | flow name            |
|  3     | flow type            |
|  4     | flow location        |
|  5     | flow property ID     |
|  6     | flow property name   |
|  7     | unit ID              |
|  8     | unit name            |
