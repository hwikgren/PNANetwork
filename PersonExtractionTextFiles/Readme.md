### Finding the names of individuals in the PNA text files

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
#### THE WORKFLOW for finding individuals in the text files:

Further details can be found in the commented code in src/PNANameExtractor.java.

1. Iterate through the text files
* Get a name from a line starting with '*'
	* convert letters from ASCII to Unicode according to our list (_..Lists/letterConversion_)
	* add '_1' after the name
* Find the next line with pieces of information divided by ';'
	* clean inconsistences
	* get the language and gender of the name
* Find the next paragraph and iterate over lines until a line starts with '<<' (or '>>'), i.e. the section of the name ends
	* From the first line and from all the lines starting with a number and full stop
		* get the description and dating of the individual
	* If a line starts with a number 
		* replace the _1 at the end of the name with _*number*
		* but if the number indicates several persons (e.g., 3.-5.) continue until a single number is found or the section of the name ends
	* Add a new individual to the list of persons
	* If the first line of a paragraph contains words 'dating'/'Date formula'/'in dates'/'eponym,' etc. (the person is attested as an eponym in a dating formula)
		* ignore until the next paragraph which
			* is hierarchically on the same level (e.g., if 1'. then ignore until 2'.)
			* or is hierarchically higher (the order is 1./a./1'./a'., see _../DocumentNameExtraction/textfileExample.txt_)
	* If a line starts with '@@'
		* get the document name corresponding to the line from the list produced with PNATextExtractor.java and imported in texts.ser
		* add the document name to the documents of the individual
	* There may subcategories listing different types of activities/documents of the individual (e.g. a, then 1', and even a')

2. Remove individuals without any valid documents
	 
3. Save information
* Save a list of individuals with their information and names of the documents they appear in into a binary file called textFileNames.bin
* Write individuals with all their information to the file _..Output/individualsInTextfiles_

**PNANameExtractor.java takes as input the folder where the text files are**
* Uses a binary file with the name docs.bin
	* contains the document names with the line they were found in using PNATextExtractor.java
	* the file assumes the same kind of treemap as is used in the scripts
* Uses a file called Lists/letterConversion
