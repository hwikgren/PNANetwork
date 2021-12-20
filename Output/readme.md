### Lists produced during the various stages of creating the network 
#### ...and sometimes used in a following stage

<b>allDocsWithIndividuals</b>: a list of all documents referring to at least two persons (in the text files and pdf file) with the names of the individuals mentioned in them

<b>allDuplicates</b>: a list of all duplicate document names in our lists and found in the PNA text files (only the SAAo project duplicate names are taken from the Oracc metadata)
* used in PNANameExtractor.java ***Tero: vai PNAPdfNameExtractor.java?***

<b>allNamesWithDocs</b>: a list of all individuals (in the text files and pdf file) with their raw PNA metadata and the documents they are mentioned in

<b>docsInTextfiles</b>: the final normalized names of documents found in the PNA text files
* used in PNAPdfNameExtractor.java

<b>individualsInPdffile</b>: a list of all individuals who appear at least in one document according to the PNA pdf file, including their raw metadata and the documents they are mentioned in

<b>individualsInTextfiles</b>: a list of all individuals who appear at least in one document according to the PNA text files, including their raw metadata and the documents they are mentioned in

<b>pairs</b>: a list of persons co-occurring in the texts; the edge weight between two persons equals the number of their co-occurrences
* can be used to create a one-mode co-occurrence network
* does not contain the node attributes
* a line with Source;Target;Weight has to be added to the beginning of the files in order to use them in Gephi.
 
<b>pairsWeighted</b>: a list of persons co-occurring in the texts; the edge weight takes the number of persons in a document into account
* the weight of each connection of a pair is calculated by dividing 1 with the number of individuals in the document -1 \[1/(n-1)]
* if the same pair appears in another document, the weights are summed up
* can be used to create a one-mode co-occurrence network
* does not contain the node attributes
* a line with Source;Target;Weight has to be added to the beginning of the files in order to use them in Gephi.

<b>textsWithOriginals</b>: the final normalised name for each original line in text files with the original line
* this information is also saved in the binary file texts.ser and used in PNANameExtractor.java

