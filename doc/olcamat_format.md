# Draft: olcamat - A Simple Binary Storage Format for Matrices
This is a specification of a simple binary data format to exchange matrix data
with a focus on simplicity and performance. Simplicity means that it should be
very easy to read and write this format in different languages (preferably using
only the respective standard library; see the code examples below). Performance
means that the format should support LCA based calculations in an optimal way
(e.g. getting a column of a precalculate inverse should only take a few disc
seeks without loading the matrix into memory).

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
|  8 - 11 | int32    | The [data type](#data-type) of the matrix entries.      |
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



