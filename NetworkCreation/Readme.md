### Network Creation

There are three different versions of the network, two one-mode networks and a two-mode network. The csv files can be found in the folder _../Networks_.

Using <b>names.csv and connections.csv</b> one can create a one-mode co-occurence network of persons attested in PNA. The weight of an edge equals the number of times two persons co-occur in different documents.

Using <b>names.csv and connectionsWeighted.csv</b> one can create a one-mode co-occurence network of persons attested in PNA. The edge weights in this network have been calculated as follows. For each document in which two persons co-occur, subtract 1 from the total number of persons attested in the document and divide 1 by the difference \[1/(n-1)]. To calculate the edge weight, add up the weights in all the documents.

Using <b>bimodal.csv</b> one can create a two-mode network of documents and the individuals appearing in them. Unlike names.csv this file does not not contain the node attributes.

The files 'pairs' and 'pairsWeighted' can also each be used to create the one-mode networks. These files are also in the folder _../Networks_. The weights have been counted as in connections.csv and connectionsWeighted.csv respectively. These files can be used, for example, in Gephi, but they do not contain the node attributes.

#### How our script creates the network data and writes csv files

Further details can be found in the commented code in src/PNACreateNetwork.java.

1. Combine the info from the PNA text files and pdf file
* Read the binary files containing the info on individuals in the text files (textfileNames.ser) and pdf file (pdfNames.ser)
   	* save the data to a treemap called 'names'
* From our lists, add the SAA documents to the documents of kings
	* _../Lists/KingsSaao_ is a list extracted from the Oracc SAAo project metadata (subprojects 1, 4, 5, 17, 18, 19, 21). If the king, during whose reign a letter is written, is mentioned in the metadata as the sender or recipient of the letter, he is connected to this document.
	* _../Lists/SAAKings_ contains a manually collected list connecting kings to the SAA project texts that relate to the king even if he is not explicitly mentioned (SAAo projects 8, 9, 10, 12, 13, 15, 16)

2. From the 'names' treemap (individuals and all the documents they are mentioned in), collect all the documents and the individuals mentioned in them to a treemap called 'reversed' (= reverse the network) 
* = two-mode network

3. From the 'reversed' treemap (all the documents and the individuals who are mentioned in them), find pairs of individuals appearing in the same document 
* but only if the document contains more than one individual
* = one-mode network
* Give a weight to each connection between two individuals: 
	* there are two alternative ways of calculating the weight
		* the simple way is to count all the documents in which both individuals are mentioned
		* in order to alleviate the impact of false connections between people who had nothing to do with each other but just happened to be mentioned in the same text (e.g., a long list of names), the weight can be calculated by giving less weight to connections found in documents with many people:
			* dividing 1 by the number of individuals in the document -1 \[1/(n-1)]
	* If the same pair appears in another document, sum up the weights
	* Once for each name in the pairs, normalise the metadata that was extracted from PNA by rules (for cleaning) and by comparing to our lists
		* Get the NameLanguage attribute for the individual
			* Clean the language extracted from PNA
			* Find the normalised language of the name from our list _../Lists/languages_
		* Get the Gender attribute 
			* by cleaning, only masc, fem and masc/fem are accepted
		* Get the Time attribute
			* Correct obvious mistakes in the dating in PNA (e.g., latereign > late reign, probab-ably > probably, etc.)
			* Standardize the dating in PNA (e.g., eighth > 8th, Sargon II > reign of Sargon II, etc.)
			* Find the normalized dating in our list _../Lists/timeperiods_
			* From the normalised dating, get the year and set start (January 1 of the year) and end date (December 31 of the year)
				* the StartYear and EndYear attributes can be used for enabling the timeline feature in Gephi
					* Merge Columns; select EndYear > ; StartYear >; select Create time interval; OK; select Parse date; date format: dd/mm/yyyy ; OK
		* Get a Profession attribute 
			* Find the normalised profession corresponding to the description from our list _../Lists/knownProffs_
			* The list has been compiled using Lists/professionCategory and semi-automatically assigning the raw PNA descriptions to the category that contains similar descriptions
		* Get the Place attribute
			* Find possible names of places from the raw PNA descriptions (e.g., starts with a capital letter after 'of'/'from'/'in'/'active in'/'mayor of', etc.)
				* If that place name is in our list of real places (and their normalizations), i.e. _../Lists/places_
					* use it
				* Else, use the first word in the description which is in our list of real places if any
				* Else, the individual gest 'Unknown' as the Place attribute 
		* Get the Origin attribute
			* If the raw PNA description is in our list of ethnic origins (_../Lists/origins_)
				* use the origin category there
			* Else, the individual gets 'Unknown' as the ethnic origin
		* Give an id number for each individual and save that in a treemap called 'nodes'
		* Write the information on the individual to a file called 'names.csv'
	* For each pair of individuals found together at least in one document
		* Get the id number of both individuals from the 'nodes' treemap
		* Write the id numbers of both individuals + the summed weight of their connections to a file called connections.csv (the weight equals the number of co-occurrences or weighted co-occurrences, depending on what was chosen above)

4. Write other infomation to files
* Write the names of individuals, their raw PNA data, and the names of the documents they appear in to the file Output/allNamesWithDocs
* Write all documents with at least 2 individuals and the names of the persons to Output/allDocsWithIndividuals
* Write the pairs of people who appear in the same documents
	* with an edge weight that equals the number of their co-occurrences (Networks/pairs)
	* with edge weights that take the number of persons in a document into account (Networks/pairsWeighted)
* Optionally, make a simple csv file of document-individual pairs (Networks/biModal.csv)


The script PNACreateNetwork.java uses
* the following files:
	* textfileNames.bin produced with PNANameExtractor.java (see folder ../PersonExtractionTextFiles)
	* pdfNames.bin produced with PNAPdfNameExtractor.java (see folder ../PersonExtractionPdfFile)
* the following files in the folder ../Lists:
	* KingsSaao
	* SAAKings
	* letterConversion
	* timeperiods
	* languages
	* places
	* origins
	* knownProffs
