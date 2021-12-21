# PNA Network
The data used for creating "A Social Network of the <i>Prosopography of the Neo-Assyrian Empire</i>"

A SHORT EXPLANATION OF THE WORKFLOW AND THE CONTENTS OF THE REPOSITORY
* The networks were created from the text and pdf file versions of the <i>Prosopography of the Neo-Assyrian Empire</i> (PNA)
* Get document names from the PNA text files provided by Simo Parpola (Editor in Chief of PNA)
  * Use concordances to standardize the names
  * Save the name with the original line it was found in
  * <b>See the folder DocumentNameExtraction for more details</b>
* Find names and other info of individuals in the text files
  * Find the documents in which an individual is mentioned by using the "original line - standardized name" conversion list produced in the previous step
  * <b>See the folder PersonExtractionTextFiles for more details</b>
* Find names and other info of individuals in the text file extracted from PNA_3_2.pdf
	* Find documents using the list of document names in the text files + concordance lists
	* ***Tero: tästä näyttäisi puuttuvan tieto siitä, miten yksilöt haetaan tiedostosta***
	* <b>See the folder PersonExtractionPdfFile for more details</b>
* Build a two-mode network of individuals and the documents they are attested in and a one-mode co-occurrence network of people attested in the same document
	* For the one-mode network, find shorter/standardized versions of the language of a name, date, profession, ethnicity, and place 
	* Write csv files of individuals and the connections between them
	* <b>See the folder NetworkCreation for more details</b>

THE FOLDERS:

<b>DocumentNameExtraction</b>: Document name extraction from the text files

<b>Lists</b>: Various lists used for extracting the names of documents and individuals and for creating the network

<b>Network</b>:

<b>NetworkCreation</b>: Creating the network and writing csv files

<b>Output</b>: Lists produced during the various stages of creating the network

<b>PersonExtractionPdfFile</b>: Finding names and documents in the pdf file

<b>PersonExtractionTextFiles</b>: Finding names and documents in the text files

ACKNOWLEDGEMENTS
We gratefully acknowledge the work of our colleagues who have created The Prosopography of the Neo-Assyrian Empire. The PNA volumes were edited by Heather D. Baker and Karen Radner in the context of the Neo-Assyrian Text Corpus Project (PI Simo Parpola). Baker’s later work on PNA was carried out in the framework of the Royal Institutional Households in First Millennium BC Mesopotamia project, funded by the Austrian Science Fund (FWF). The individual entries in the prosopography were written by colleagues who are too many to be listed here. We thank Parpola for making the PNA volumes available to us in plain text and pdf formats, and Baker, Parpola, and Radner for the permission to publish the dataset online. We also thank the members of the Centre of Excellence in Ancient Near Eastern Empires and the participants of several workshops for their helpful feedback on various versions of the dataset.
