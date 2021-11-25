### Find document names from text files received from Simo Parpola 

(see textfileExample.txt for a small extract from one)

1. Iterate through the text files and find possible document names
Document names are on their own lines that start with '@@'
* Ignore document names that are on the "Do not collect list"
	* This is done in the code, but the list can be seen in DoNoCollect.txt file
* @@-names inside parenthesis are also ignored
* If line contains '=', i.e. there are duplicate names for the document
	* we select the first one but save the information on the duplicate name as well
	* but if the line contains the name Hunger or Borger (1996), we prefer those names
	* the duplicates that are on the same line are cleaned and saved in a treemap called onlineDuplicates
* Remove the @@ from the beginning of line
* Remove everything after a comma unless its part of the name as in 89-4-26,209:13
* Do some cleaning
* Find the actual document name from the line when document name is clear, i.e. there is indication of where the line number starts (R, r., :)
	* Examples (Found name		Whole line):
	  * SAA 6 287       @@SAA 06 287 R009 (670).
	  * SAA 10 112      @@SAA 10 112 r. 5, 12 (not dated,
	  * SAA 10 176      @@SAA 10 176:12 (not dated).
* Correct obvious mistakes and inconsistences
	* E.g. @@As 01319 R014 (636*) > As1319
	* and Trade 1998 > Trade 2
* If an obvious name had been found it is saved in a treemap called easyNames
	* all document names are saved in a treemap called texts with the original line they were found from
		* at this point, there might still be several versions of the same document

2. Make thumbprints of the easily found names in easyNames
	* thumbprints are made by replacing numbers with 1 and spaces with _
	* Eg. SAA 14 123 > SAA_11_111

3. Check all the document names in texts-treemap again
	* use synonymous name if on our concordance lists
	* make a thumbprint
		* check if the list of earlier thumbprints has this thumbprint or a shorter version of it
		* shorten the name to match the length of the thumbprint found
	* check again for synonymous names

4. Save found document name with the original line it was found in into a binary file texts.ser 
	* the binary file used in extracting the names of individuals from the same documents
	* Print the document name and the original line to *textsWithOriginals*
	* Print a list of document names to *docsInTextfiles*

Further details can be seen in the commented code in src/PNATextExtractor.java

The script uses lists in Lists folder, i.e. ../Lists/ (the script should be started from the folder where Lists-folder is)
  * ../Lists/concordances
    * List compiled by us to find texts that have different names but are really the same document
    * Especially Asxxxxx, StAT, and BATSH documents
  * ../Lists/sameText.txt
    * Our first list of documents that are in fact the same although they have different names
  * ../Lists/oraccConcordances
  	* a concordance list of document names extracted from Oracc metadata (catalogue.json) 

The script assumes a folder called Output and writes to it:
* *textsWithOriginals*: document name and the original line
* *docsInTextfiles*: list of the normalised names of all found documents

There is also options to write the following lists to Output:
* *thumbprints*: list of the document names changed to thumbprints
* *usedDuplicates*: list of all document names found in the text files with normalised names for each
* *allDuplicates*: list of all possible duplicates from our lists and found in PNA (from oraccConcordances only SAA)





