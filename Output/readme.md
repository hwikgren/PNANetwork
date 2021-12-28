### Lists produced during the various stages of creating the network 
#### ...and sometimes used in a following stage

<b>allDocsWithIndividuals</b>: a list of all documents referring to at least two persons (in the text files and pdf file) with the names of the individuals mentioned in them

<b>allDuplicates</b>: a list of all duplicate document names in our lists and found in the PNA text files (only the SAAo project duplicate names are taken from the Oracc metadata)
* used in PNAPdfNameExtractor.java

<b>allNamesWithDocs</b>: a list of all individuals (in the text files and pdf file) with their raw PNA metadata and the documents they are mentioned in

<b>docsInPdfFile</b>: the final normalized names of documents found in the PNA pdf file

<b>docsInTextfiles</b>: the final normalized names of documents found in the PNA text files
* used in PNAPdfNameExtractor.java

<b>individualsInPdffile</b>: a list of all individuals who appear at least in one document according to the PNA pdf file, including their raw metadata and the documents they are mentioned in

<b>individualsInTextfiles</b>: a list of all individuals who appear at least in one document according to the PNA text files, including their raw metadata and the documents they are mentioned in

<b>textsWithOriginals</b>: a list of normalized text names and the original data (a full line) in the PNA text files
* this information is also saved in the binary file texts.ser and used in PNANameExtractor.java

<b>thumbprints</b>: Thumbprints made in PNATextExtractor.java for all the documents where it was clear where the document name ended and line number started. Before trying to find document names in running unstructured texts PNAPdfNameExtractor.java made similar thumbprints for all the document names found in the text files and in corcordance lists.

<b>usedDuplicates</b>: The actual duplicates that were used for instead of the document name found in the text files. 

