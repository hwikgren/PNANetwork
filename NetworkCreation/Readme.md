### Network Creation

There are three different versions of the network, two one-mode networks and a two-mode network.

Using <b>names.csv and connections.csv</b> one can create a one-mode co-occurence network of persons attested in PNA. The weight of an edge equals the number of times two persons are attested in the same document.

Using <b>namesWeighted.csv and connectionsWeighted.csv</b> one can create a one-mode co-occurence network of persons attested in PNA. The edge weights in this network have been normalized as follows. For each document in which two persons co-occur, subtract 1 from the total number of persons attested in the document and divide 1 by the difference \[1/(n-1)]. To calculate the edge weight, add up the results in all the documents.
***Tero: eikö niin, että names.csv ja namesWeighted.csv ovat täsmälleen sama tiedosto? Siinä tapauksessa voitaneen poistaa namesWeighted.csv?***

Using <b>bimodal.csv</b> one can create a two-mode network of documents and the individuals appearing in them.

The files 'pairs' and 'pairsWeighted' in the Output folder can also be used to create the one-mode networks. Those can be used in Gephi, but they do not contain the node attributes. A line with Source;Target;Weight has to be added to the beginning of the files in order to use them in Gephi.

#### How our script creates network data and writes csv files

Further details can be found in the commented code in src/PNACreateNetwork.java.

1. Combine the info from the PNA text files and pdf file
* Read the binary files containing the info on individuals in the text files (textfileNames.ser) and pdf file (pdfNames.ser)
   	* save the data to a treemap called 'names'
* From our lists, add the SAA documents to the documents of kings
	* _../Lists/KingsSaao_ is a list extracted from the Oracc SAAo project metadata (subprojects 1, 4, 5, 17, 18, 19, 21). If the king, during whose reign a letter is written, is mentioned in the metadata as the sender or recipient of the letter, he is connected to this document.
	* _../Lists/SAAKings_ contains a list connecting kings to the SAA project texts that relate to the king even if he is not explicitly mentioned (SAAo projects 8, 9, 10, 12, 13, 15, 16)

2. From the list of individuals, collect all the documents and the individuals mentioned in them (= reverse the network) ***Tero: Mikä tämä "list of individuals" on? Sitä ei ole mainittu aikaisemmin.***
* = Bimodal network

3. From the list of documents, find pairs of individuals appearing in the same document ***Tero: Mikä tämä "list of documents" on? Sitä ei ole mainittu aikaisemmin.***
* but only if the document contains more than one individual
* = one-mode network
* Give weight to each connection between two individuals: divide 1 by the number of individuals in the document -1 \[1/(n-1)]
	* If the same pair appears in another document, sum up the weights
	* For each name in the pairs
		* Get a Profession attribute for the individuals from our list _../Lists/knownProffs_
			* The list has been compiled using Lists/professionCategory and semi-automatically assigning the raw PNA descriptions to the category that contains similar descriptions
		* Get the Ethnicity attribute for the individuals
			* If the raw PNA description is in our list of ethnicities (_../Lists/origins_)
				* use the Ethnicity category there
			* Else, individuals get 'Unknown' as the Ethnicity attribute
		* Get the Place attribute for the individuals
			* Find possible names of places from the raw PNA descriptions (e.g., starts with a capital letter after 'of'/'from'/'in'/'active in'/'mayor of', etc.)
				* If that place name is in our list of real places (and their normalizations), i.e. _../Lists/places_
					* use it
				* Else use the first word in the description which is in our list of real places if any
		* Get the Time attribute for the individuals
			* Correct obvious mistakes in the dating in PNA (e.g., latereign > late reign, probab-ably > probably, etc.)
			* Standardize the dating in PNA (e.g., eighth > 8th, Sargon II > reign of Sargon II, etc.)
			* Find the normalized dating in our list of time periods (_../Lists/timeperiods_)
				* use that for the Time attribute of the individual
		* Give an id number for each individual and save that in a treemap called 'nodes'
		* Write the information on each individual to a file called 'names.csv'
	* For each pair of individuals found together at least in one document
		* Get the id number of both individuals from the 'nodes' treemap
		* Write the number of both individuals + the summed weight of their connections to file called connections.csv
***Tero: Voisitko tarkentaa tähän vielä sen, miten connections.csv- ja connectionsWeighted.csv-tiedostot tuotetaan? Nyt puhutaan vain connections.csv-tiedostosta niin kuin se olisi normalisoitu tiedosto.***

4. Write other infomation to files
* Write the names of individuals and the names of the documents they appear in to the file Output/allNamesWithDocs
* Write all documents with at least 2 individuals and the names of the persons to Output/allDocsWithIndividuals
* Write the pairs of people who appear in the same documents
	* with an edge weight that equals the number of their co-occurrences (Output/pairs)
	* with normalized edge weights (Output/pairsWeighted)
* Optionally, make a simple csv file of document-individual pairs (biModal.csv)


The script PNACreateNetwork.java assumes
* the following files in the folder where the script is started from:
	* textfileNames.ser
	* pdfNames.ser
* the following files in the folder Lists:
	* KingsSaao
	* SAAKings
	* letterConversion
	* timeperiods
	* languages
	* places
	* origins
	* knownProffs
