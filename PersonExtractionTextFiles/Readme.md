### Find names of individuals in the PNA text files

The format of the text files is:
```
*Name
(translation of name); language of name; gender;
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
	which might be divided into a list marked with letters, etc. (e.g., a. bla bla 1.' bla bla a.' bla bla)
	- Can have duplicate names indicated by '=', e.g. @@89-4-26,209:13 = Streck (1916) 288 no. 13.

<<Name of the scholar who wrote the PNA entry
```
**The procedure of finding individuals from text files**

1. Iterate thought the text files
* Get name from line starting with '*'
	* convert letters from ascii to unicode according to our list (Lists/letterConversion)
	* add '_1' to the name
* Find the next line with infos divided by ';'
	* clean inconsistences
	* Get language and gender of the name
* Find next paragraph and iterate over lines until line starts with '<<' (or '>>'), i.e. section of the name ends
	* From the first line and from all lines starting with number and full stop
		* get the description and dating of the individual
	* If lines starts with number 
		* replace the _1 at the end of the name with _*number*
		* but if the number indicates several persons continue until an individual is found or the section of the name ends
	* add new individual to the list of persons
	* if first line of a paragraph contains words dating/Date formula/in dates/eponym etc.
		* ignore until next paragraph which
			* starts in similar fashion (e.g. if 1'. until 2'.)
			* or is hierarchically higher (the order is 1. a. 1'. a'., see textfileExample.txt) 
	* if line starts with '@@'
		* get the document name corresponding to the line from the list produced with PNATextExtractor.java and imported in texts.ser)
		* add the document name to the documents of the individual

2. Remove individuals with no valid documents
	 
3. Save information
* Save list of individuals with their info and names of documents they appear into a binary file called textFileNames.ser
* Write names with all their information to a file called Output/individualsInTextfiles

**Takes as input the folder where the text files are**
* Assumes a binary file with the name texts.ser
	* contains the document names with the line they were found on with PNATextExtractor.java
	* the file should be in the folder where the PNANameExtractor.java is started from
		* can here be found in folder _../DocumentNameExtraction/_
* Assumes a file called Lists/letterConversion
	* the script should be started from the folder where the Lists folder is
