### Find document names from the text file versions of PNA

(see textfileExample.txt for a small extract from one)

1. Iterate through the text files and find possible document names
* Document names are on their own lines that start with '@@'
* Ignore document names that are on the 'Do not collect' list
	* This is done in the code, but the list can be seen in the DoNoCollect.txt file
	* The ignored documents are primarily king lists, eponym lists, and royal inscriptions, because the co-occurrence of two persons in these texts does not indicate that the persons were in contact with each other. Some texts were ignored because it was impossible to know if the document name refers to one or more texts.
* @@-names inside parenthesis are also ignored
* If a line contains '=', there are duplicate names for the document
	* we select the first one but save the information on the duplicate name as well
	* but if the line contains the name 'Hunger' or 'Borger (1996)', we prefer those names
	* the duplicates that are on the same line are cleaned and saved in a treemap called 'onlineDuplicates'
* Remove '@@' from the beginning of line
* Remove everything after a comma unless it is part of the name as in 89-4-26,209:13
* Do some cleaning
* Find the actual document name from the line when the document name is clear, i.e. there is indication of where the line number starts (indicated by 'R,' 'r.', or ':')
	* Examples (Found name; Whole line):
	  * SAA 6 287; @@SAA 06 287 R009 (670).
	  * SAA 10 112; @@SAA 10 112 r. 5, 12 (not dated,
	  * SAA 10 176; @@SAA 10 176:12 (not dated).
* Correct obvious mistakes and inconsistences
	* E.g., @@As 01319 R014 (636*) > As1319
	* and Trade 1998 > Trade 2
* If an obvious name is found, it is saved in a treemap called 'easyNames' ***Tero: mitä "obvious name" tarkoittaa?***
	* all document names are saved in a treemap called 'texts' with the original line they were found from
		* at this point, there might still be several versions of the same document ***Tero: Tarkoittaako tämä duplikaatteja, joilla on eri nimi?***

2. Make thumbprints of the easily found names in 'easyNames'
	* thumbprints are made by replacing numbers with 1 and spaces with _
	* Eg., SAA 14 123 > SAA_11_111

3. Check all the document names in the 'texts' treemap again
	* use a synonymous name if one is found on our concordance lists
	* make a thumbprint
		* check if the list of earlier thumbprints has this thumbprint or a shorter version of it
		* shorten the name to match the length of the thumbprint found
	* check again for synonymous names

4. Write information to files
	* document names with the original line they were found in are saved to the binary file called 'texts.ser'
		* it is used for extracting the names of individuals from the same documents (PNANmeExtractor.java)
	* write the document name and the original line to _../Output/textsWithOriginals_
	* write a list of final normalized document names to _../Output/docsInTextfiles_

Further details can be seen in the commented code in src/PNATextExtractor.java

The script uses lists in Lists folder, i.e. _../Lists/_ (the script should be started from the folder where Lists-folder is)
  * ../Lists/concordances
    * List compiled by us to find texts that have different names but are really the same document
    * Especially Asxxxxx, StAT, and BATSH documents
  * ../Lists/sameText.txt
    * Our first list of documents that are in fact the same although they have different names
  * ../Lists/oraccConcordances
  	* a concordance list of document names extracted from Oracc metadata (catalogue.json) 

The script assumes a folder called Output and writes to it (can be found in folder _../Output/_):
* _textsWithOriginals_: document name and the original line
* _docsInTextfiles_: list of the normalised names of all found documents
* _allDuplicates_: list of all possible duplicates from our lists and found in PNA (from oraccConcordances only SAA)

There is also options to write the following lists to Output (the additional files can be found in this folder):
* _thumbprints_: list of the document names changed to thumbprints
* _usedDuplicates_: list of all document names found in the text files with normalised names for each
	* form of the file: found name TAB used name
	* for the conversion the following sources were used:
  		* duplicate names on the same line in the text files (indicated by the '=')
  		* ../Lists/sameText.txt
  		* ../Lists/concordances
  		* ../Lists/oraccConcordances






