### Find document names from text files received from Simo Parpola (see textfileExample for a small extract from one)

Document names are on their own lines that start with '@@'
* Ignore document names that are on the "Do not collect list"
	* This is done in the code, but the list can be seen in DoNoCollectList
* Correct obvious mistakes
	* E.g. @@As 01319 R014 (636*) > As01319
* Find document lines where document name is clear, i.e. there is indication of where the line number starts 
	* Examples (Found document name	Whole line):
	  * SAA 6 287       @@SAA 06 287 R009 (670).
	  * SAA 10 112      @@SAA 10 112 r. 5, 12 (not dated,
	  * SAA 10 176      @@SAA 10 176:12 (not dated).
* Make thumbprints of the found names, i.e. replace numbers with 1 and spaces with _ 
	* Eg. SAA 14 123 > SAA_11_111
* Check all the document names again
	* use synonymous name if on our concordance lists
	* make a thumbprint
		* check if the list of earlier thumbprints has this thumbprint or a shorter version of it
		* shorten the name to match the length of the thumbprint found
	* check again for synonymous names
* Save found document name with the original line it was found in into a binary file (used in extracting the names of individuals from the same documents)
* Print the document name and the original line to *textsWithOriginals*
* Print a list of document names to *docsInTextfiles*

Further details can be seen in the commented code in src/PNATextExtractor.java

The script uses lists in *../Lists/*. (the folder should be inside this folder for the script to work)
  * ../Lists/concordances
    * List compiled by us to find texts that have different names but are really the same document
    * Especially Assxxxxx, StAT, and BATSH documents
  * ../Lists/sameText.txt
    * Our first list of documents that are in fact the same although they have different names
  * ../Lists/oraccConcordances

The script assumes a folder called Output and writes to it:
* *textsWithOriginals*: document name and the original line
* *docsInTextfiles*: list of the normalised names of all found documents

There is also options to write the following lists to Output:
* *thumbprints*: list of the document names changed to thumbprints
* *usedDuplicates*: list of all document names found in the text files with normalised names for each
* *allDuplicates*: list of all possible duplicates from our lists and found in PNA (from oraccConcordances only SAA)



