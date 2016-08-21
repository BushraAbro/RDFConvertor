# RDFConvertor

This code is used for converting Hadith database to RDF using Hadith Ontology, which is also designed as a part of this project. 
The code uses OWL API version 2. The ontology is created in Protege 5.0 beta and its Java code generator has been used to generate Java classes for ontology.
Data Access layer is implemented separately which reads data from SQL database. InstanceCreation.java gets the respective data from each class and populate the Ontology. 


## Usage

This approach can be extended to any other hadith dataset.
A sample database hadith.sql has been given to view the structure of database we have used. 
The code and Ontology should be modified according to the respective needs (in case database structure is modified)
The code takes the following parameters :
1- Ontology file name and path
2- Database file
3- Output file name and path
4- Database connection settings can be changed in src/dataAccess/connectionFactory.java
