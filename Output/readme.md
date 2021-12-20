### Lists produced during the various stages of creating the network 
#### ...and sometimes used in a following stage

<b>allDocsWithIndividuals</b>: a list of all documents referring to at least two persons (in the text files and pdf file) with the names of the individuals mentioned in them

<b>allDuplicates</b>: a list of all duplicate document names in our lists and found in the PNA text files (only the SAAo project duplicate names are taken from the Oracc metadata)
* used in PNANameExtractor.java ***Tero: vai PNAPdfNameExtractor.java?***

<b>allNamesWithDocs</b>: a list of all individuals (in the text files and pdf file) with their raw PNA metadata and the documents they are mentioned in

<b>docsInTextfiles</b>: the final normalized names of documents found in the PNA text files
* used in PNAPdfNameExtractor.java

<b>individualsInPdffile</b>: all the information of each individual who appears in at least 1 document according to the pdf file

<b>individualsInTextfiles</b>: all the information of each individual who appears in at least 1 document according to the text files

<b>pairs</b>: the network of individuals connected through appearing in the same document(s) with the weight of their connection
* the weight of each connection of a pair is calculated by summing up the number of documents the two persons forming the pair appear together
 
<b>pairsWeighted</b>: the network of individuals connected through appearing in the same document(s) with the weight of their connection
* the weight of each connection of a pair is calculated by dividing 1 with the number of individuals in the document -1
* if the same pair appears in another document the weights are summed up

<b>textsWithOriginals</b>: the final normalised name for each original line in text files with the original line
* this information is also saved in the binary file texts.ser and used in PNANameExtractor.java

