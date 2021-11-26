### Find names and documents in the pdf-file (Lists/pdfAsText)

The last volume of the Prosopography of the Neo-Assyrian Empire, e.g. volume 3/II is not available as a text file. The individuals and the documents they appear in in this this particular volume have been extracted from a text file made from a pdf-file of the volume.
* For the details of extracting the text from the pdf file see the file pdfTextExtraction.txt.

The form of the file:
```
*name (translation of name); language of name; gender of name; wr. transliteration.
short description/profession (dating): longer description with document names in running text
      - Can start with a number (e.g. 3.), if there are several individuals with the same name 
<<scholar who added to database>>.
```
1. Make a "database" of all known document names, including the ones in the concordance list
* Read _../Output/docsInTextfiles_ and _../Output/allDuplicates_
	* First word of the name is the key
		* all document names starting with the key are variants of that key
		* record the length of the longest variant
	* Duplicates are also added to a treemap called duplicates
	* A thumbprint is made of all document names and added to the list of thumbprints
	  * thumbprints are made by replacing numbers with 1 and spaces with _
	  * Eg. SAA 14 123 > SAA_11_111
* Read the Radner (forthcoming) conversion list to MAss names (_../Lists/Mass_Radner_conversion_)
  * the names add added to treemap called MAss   
2. Iterate through the lines of the pdfAsText-file
* Get name from line starting with '*'
	* Convert letters from ascii to unicode according to our list (_../Lists/letterConversion_)
	* Add '_1' to the name
	* Get language and gender of the name from the same line (divided by ';')
* Iterate over lines until line starts with '<<' (or '>>')
	* From the first line and lines starting with number and full stop 
		* Get the "profession" and date (in parentheses) of the individual 
			(separated by ':' from the rest of the line)
		 * If the line starts with a number, change the number of the name to that number
	*  For each line
		* If beginning of line (before ':') contains words dating/Date formula/in dates/eponym etc.
			* ignore the line
		* Else iterate word by word the line after ':' 
			* if the word is 'Radner'
				* if 'word + next word' are found in our MAss-concordances list
					* add the MAss name to the documents of the individual
			* Else if the word is a key in the document "database"
				* Consider only the rest of the line after that word
					* Cut from the line the part that cannot be part of a doc name
				* Take from the snippet that is left the beginning up to the length of the longest doc name starting with that word
				* If the snippet is in our duplicates treemap
					* Change the snippet to be the duplicate name instead
				* If the snippet is among the variants of the word/key
					* Add it to the documents of the individual
					* if the individual is not yet in the list of persons
						* Add new individual with his/her info
				* Else
					* Make thumbprint of the snippet 
						* compare that to the thumbprints of the doc names in the text files
						* If matching thumbprint was found
							* extract the length of that thumbprint from the snippet 
							* Add it to the documents of the individual
							* if the individual is not yet in the list of persons
								* Add new individual with his/her info
				* If not found, leave out last word of the snippet
					* Repeat until matching document name found or all words in the 
					snippet have been handled
			* Continue from the following word
3. Save the information 
* write the treemap with individual and their info + names of documents they appear in to a binary file called pdfNames.ser
* write list of individual with their info and names of documents they appear in to a file called Output/individualsInPdffile

The script assumes 
* a folder named Lists with the following files:
     * letterConversion
     * MAss_Radner_conversion
* a folder named Output with the following files produced with PNATextExtractor.java and PNANameExtractor.java:
     * docsInTextfiles
     * allDuplicates

There is an option of writing only the names of the document names found in the pdf-file to file _../Output/docsInPdffile_
* the file is found in this folder
