### Finding document names in the text files

The format of the text files is:
```
*Name
(translation of the name); language of the name; gender;
information on the name (can span several lines)

Profession (time period when lived): 
	- Line starts with a number if there are several individuals with the same name, otherwise not
		e.g., 3. Priest of Ninurta, in Kalhu:
	- Can span several lines
Information on the individual     
	- Can span several lines
@@Document name (year);
more information
@@Document name (year);
	- This can go on for several paragraphs and be divided into a numbered list 
	which might be divided into a list marked with letters, etc. (e.g., a., then 1'., and even a'.)
	- Can have duplicate names indicated by '=', e.g. @@89-4-26,209:13 = Streck (1916) 288 no. 13.

<<Name of the scholar who wrote the PNA entry
```

#### THE WORKFLOW of extracting document names from the text files:

Further details can be found in the commented code in src/PNATextExtractor.java. See textfileExample.txt for a small extract from a text file.

1. Iterate through the text files and find possible document names
* Document names are on their own lines that start with '@@'
* Ignore document names that are on the 'Do not collect' list
	* This is done in the code, but the list can be seen in the DoNotCollect.txt file
	* The ignored documents are primarily king lists, eponym lists, and royal inscriptions, because the co-occurrence of two persons in these texts does not indicate that the persons were in contact with each other. Some texts were ignored because it was impossible to know if the document name refers to one or more texts.
* @@ names inside parenthesis are also ignored
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
* When it is clear where the document name ends and line number starts, the name part is saved in a treemap called 'easyNames' 
* All document names are saved in a treemap called 'texts' with the original line they were found from

2. Make thumbprints of the easily found names in 'easyNames'
	* thumbprints are made by replacing numbers with 1 and spaces with _
		* trailing letters are replaced with x and +-signs with X
	* Eg., SAA 14 123 > SAA_11_111

3. Check all the document names in the 'texts' treemap again
	* use a synonymous name if one is found on our concordance lists
	* make a thumbprint
		* check if the list of earlier thumbprints has this thumbprint or a shorter version of it
		* shorten the name to match the length of the thumbprint found
	* check again for synonymous names

4. Write information to files
	* document names with the original line they were found in are saved to the binary file called 'docs.bin'
		* it is used for extracting the names of individuals from the documents (../PersonExtractionTextFiles/src/PNANameExtractor.java)
		* reading the file docs.bin requires a java TreeMap such as is used in the code here
	* write the document name and the original line to _../Output/textsWithOriginals_
	* write a list of final normalized document names to _../Output/docsInTextfiles_

The script uses lists in the Lists folder, i.e. _../Lists/_ (the script should be started from the folder where the Lists folder is)
  * ../Lists/concordances
    * A list compiled from the indexes of BATSH 6 and StAT 1-3 to find texts that have different names but are the same document
  * ../Lists/sameText.txt
    * Our first list of documents that are in fact the same although they have different names
  * ../Lists/oraccConcordances
  	* a concordance list of document names extracted from the Oracc metadata (catalogue.json in each Oracc project) 

The script also writes the following lists which can be found in folder _../Output/_:
* _allDuplicates_: list of all possible duplicates included in our lists and found in PNA (including only the SAA texts from the list 'oraccConcordances')
* _thumbprints_: list of the document names changed to thumbprints
* _usedDuplicates_: list of all document names found in the text files with a normalized name for each
	* form of the file: found name TAB used name
	* for the conversion the following sources were used:
  		* duplicate names on the same line in the text files (indicated by '=')
  		* ../Lists/sameText.txt
  		* ../Lists/concordances
  		* ../Lists/oraccConcordances


