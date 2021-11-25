### Find names of individuals in the text files

The format of the text files is:
    * *Name
    * (translation of name); language of name; gender
    * information on the name (can span several lines)
    *
    * Profession (time period when lived)   [Line starts with a number if several individuals with the same name, otherwise not]
    * information on the individual     [can span over several lines]
    * @@Document name line (year)
    * more information
    * @@Document name line (year)
    *      [This can go on for several paragraphs and be divided into numbered list with might be divided into list marked with letters etc. a. bla bla 1' bla bla a' bla bla]
    *
    * <<Name of scholar who added to database

1. Iterate thought the text files
* Get name from line starting with '*'
	* convert letters from ascii to unicode according to our list (Lists/letterConversion)
	* add '_1' to the name
* Find the next line with infos divided by ';'
	* clean inconsistences
	* Get language and gender of the name
* Find next paragraph and iterate over lines until line starts with '<<' (or '>>')
	* From the first line and from all lines starting with number and full stop
		* get the description and dating of the individual
	* If lines starts with number 
		* replace the _1 at the end of the name with _*number*
		* but if the number indicates several persons continue until an individual is found or the section of the name ends
	* add new individual to the list of persons
	* if first line of a paragraph contains words dating/Date formula/in dates/eponym etc.
		* ignore until next paragraph 
			* which starts in similar fashion (e.g. if 1'. until 2'.)
			* or that is hierarchically higher (the order is 1. a. 1'. a'., see textfileExample.txt) 
	* if line starts with '@@'
		* get the document name corresponding to the line from the list build in section 1)
		* add it to the documents of the individual

2. remove individuals with no valid documents
3. save list of individuals with their info and names of documents they appear in

Takes as input the folder where the text files are
	* Assumes a binary file with the name texts.ser
		* contains the document names with the line they were found on with PNATextExtractor.java
	* Assumes a file called Lists/letterConversion
		* the script should be started in the folder where the Lists folder is
