# Java-DB
Design and implementation of a Relational Database System in Java. Supports tables, column types, constraints, keys, most SQL commands, pretty print, and import/export CSV functionality.  

## Development Overview
### Records
The Record class stores the record’s value in a fixed size array. The number of fields is defined at construction time and cannot be changed. This increases robustness and reduces the chance of table corruptions (table columns, record’s number of fields mismatch). Record fields can be accessed and updated normally. Field names are not stored in the record itself to avoid data duplication.

### Tables
In the Table class the Columns are stored in an Array List. The records are stored in a Linked HashMap, but it is possible to switch to a B-tree. Instead of storing the columns as a list of strings, a Column class is created. This allows for more flexibility at implementing Primary Keys, Data types, and Constraints.

Alter Table Operations include:
- Adding a column at a specific index  
- Append Columns after the last Column  
- Drop columns  

All operations involving altering a table, replace all records with a modified copy of the records (e.g. With more or less fields). It is assumed that these operations are not frequent, and will not affect the general performance of the database.

### SQL Commands
Implemented commands:
- Insert Record
- Select Record by key
- Update Record by key
- Delete Record by key

The related bulk operations are also implemented. The bulk methods are essentially wrappers of the single operations, that take as parameters a Set of keys and return a Set of rows. Changing the underlying data structures later will not require rewriting the bulk operations.

### Constraints & Keys
The constraints functionality, is implemented with an enum class named Constraint and an interface named Violates. The Constraints class contains enums for Not Null, Unique and Primary Key, and also implements the Violates interface. The PK constraint implies that the fields must be Not Null and Unique. Each Enum Subclass, overwrites the single method isViolated(). This method determines if the column constraints are violated by using a stream of the table’s records. Again, lambda expressions are used.  

An EnumSet<Constraint> member exists in the Column class, and its constructor accepts constraints. The method isViolation() in the Table class, checks if the record to be added violates any column constraints. For example, in insert() and update() record methods.  

Extensive unit testing for multiple constraint cases is included.  

### Storage Engine
For this project, the most appropriate format to save tables to disk is CSV. The StorageEngine class is responsible for bridging the database with the CSV utilities. Any file format can be used in the future, without altering the core database code.  

The underlying table’s Map is not exposed. Instead, Java streams and Lambda expression are used to ensure robustness, compact and beautiful code.  

### CSV Parser
A custom CSV Generator and Parser is included with this project. The CSVUtilities class includes static methods to generate and parse CSV files, and it’s separated from the rest of the project.  Extensive unit testing is included for the CSVUtilities class.  

## Testing
Unit testing is included for all aforementioned functionality.

## Author

**Evan Lalopoulos** - [evanlal](https://github.com/evanlal)

## License
Written by Evan Lalopoulos <evan.lalopoulos.2017@my.bristol.ac.uk>    
Copyright (C) - All rights reserved.  
Unauthorized copying is strictly prohibited.  
