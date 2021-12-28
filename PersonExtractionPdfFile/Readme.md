### Find names and documents in the pdf file

For further details of the procedure, see the commented code in src/PNAPdfNameExtractor.java.

The last volume of PNA (3/II) was not available to us as a text file. The individuals and the documents they appear in have been extracted from a text file converted from a pdf file of the volume.
* For the details of extracting the text from the pdf file, see the file pdfTextExtraction.txt.

An example of the text file extracted from the pdf file can be seen in pdfAsText_example. The form of the text file is:
```
*name (translation of the name); language of the name; gender of the name; wr. transliteration.
short description/profession (dating): longer description with document names in running text
      - Can start with a number (e.g., 3.), if there are several individuals with the same name 
      	- may have subcategories listing different types of activities/documents of the individual (e.g. a, then 1', and even a')
<<the name of the scholar who wrote the PNA entry>>.
```
THE WORKFLOW:

1. Create a "database" of all known document names in the PNA text files, including the ones in the concordance lists
* Read _../Output/docsInTextfiles_ and _../Output/allDuplicates_
	* The first word of the document name is the key
		* All document names starting with the key are variants of that key
		* Record the length of the longest variant
	* Duplicates are also added to a treemap called 'duplicates'
	* A thumbprint is made of all document names and added to the list of thumbprints
	  * Thumbprints are made by replacing numbers with 1 and spaces with _ 
	  	* trailing letters are replaced with x and +-signs with X
	  * Eg., SAA 14 123 > SAA_11_111
* Read the Radner (forthcoming) conversion list to MAss names (_../Lists/Mass_Radner_conversion_)
	* The names are added to a treemap called 'MAss'
2. Iterate through the text file converted from pdf
* Get a name from a line starting with '*'
	* Convert letters from ASCII to Unicode according to our list (_../Lists/letterConversion_)
	* Add '_1' to the end of the name
	* Get the language and gender of the name from the same line (divided by ';')
* Iterate over lines until a line starts with '<<' (or '>>')
	* From the first line and the lines starting with a number and full stop 
		* Get the "profession" and date (in parentheses) of the individual 
			(separated by ':' from the rest of the line)
		 * If the line starts with a number, change the number of the name to that number
	*  For each line
		* If the beginning of the line (before ':') contains the words 'dating'/'Date formula'/'in dates'/'eponym,' etc. (the person is attested as an eponym in a dating formula)
			* Ignore the line
		* Else, iterate word by word the line after ':' 
			* If the word is 'Radner'
				* If 'word + next word' are found in our MAss_Radner_conversion list
					* Add the MAss name to the documents of the individual
			* Else, if the word is a key in the "database" of document names
				* Consider only the rest of the line after that word
					* Cut from the line the part that cannot be part of a document name
				* From the snippet that is left, take the beginning of the snippet up to the length of the longest document name starting with that word
				* If the snippet is in our duplicates treemap
					* Change the snippet to the duplicate name instead
				* If the snippet is among the variants of the key
					* Add it to the documents of the individual
					* If the individual is not yet in the list of persons
						* Add a new individual with his/her info
				* Else
					* Make a thumbprint of the snippet 
						* Compare the thumbprint to the thumbprints of the document names in the text files
						* If a matching thumbprint is found
							* Extract the length of that thumbprint from the snippet 
							* Add it to the documents of the individual
							* If the individual is not yet in the list of persons
								* Add a new individual with his/her info
					* If not found, leave out the last word of the snippet
						* Repeat until a matching document name is found or all the words in the 
					snippet have been iterated over
			* Continue from the following word
3. Save the information 
* Write a treemap with individuals and their info + names of the documents they appear in to a binary file called pdfNames.bin
* Write a list of individuals with their info + names of the documents they appear in to a file called _..Output/individualsInPdffile_

The script uses 
* following files in the folder ../Lists:
     * letterConversion
     * MAss_Radner_conversion
* following files in the folder called ../Output:
     * docsInTextfiles
     * allDuplicates

There is an option of writing only the document names found in the pdf file to the file _../Output/docsInPdffile_
