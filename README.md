# PNANetwork
The data used for creating the Social Network of the Neo-Assyrian Empire

A SHORT EXPLANATION OF EXTRACTING NETWORKS FROM PNA
* Get document names from text files provided by Prof. Emeritus Simo Parpola (Editor in Chief of PNA)
  * Use concordances to standardise the names
  * Save name with the original line it was found in
  * <b>See folder DocumentNameExtraction for more details</b>
* Find names and other info of individuals in the text files
  * find documents an individual is said to be mentioned in by using the "original line - standardised name" conversion list imported from texts.ser
  * <b>See folder PersonExtractionTextFiles for more details</b>
* Find names and other info of individual in the text extracted from PNA_3_2.pdf
	* Find documents using the list of document names in the text files + concordance lists
	* <b>See folder PersonExtractionPdfFile for more details</b>
* Build network of individuals appearing in common documents
	* Find shorter/standardised versions of language of name, dating, profession, ethnicity, and place 
	* Write csv-files of individuals and the connections between them
