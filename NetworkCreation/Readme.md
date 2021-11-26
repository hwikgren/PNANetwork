### Making the network and writing csv-files

1. Combine the info from text files and the pdf-file
   * Read the binary files containing the info on individuals in the texts files and the pdf-file
     * save the data to a treemap called names
   * From our lists add the SAA documents to the documents of kings
     * _../Lists/KingsSAAo_ contains the kings in the Oracc SAAo project who are mentioned in the metadata as sender or recipient of a text and the text in SAAo projects/books 1, 4, 5, 17, 18, 19, 21
     * _../Lists/SAAKings_ contains a list compiled by us for kings who appear in SAA books 8, 9, 10, 12, 13, 15, 16
2. Collect from the list of individuals all the documents and the individuals mentioned in them (= reverse the network)
   1. Bimodal network
      * If document contains more than one individual
        * Make a simple csv-file of document-individual pairs (biModal.csv)
   2. Unimodal network
      * From the list of documents, find pairs of individuals appearing in the same document
        * Give weight to each connection of a pair: divide 1 by the number of individuals in the document -1
	* If the same pair appears in another document, sum up the weights
      * For each name in the pairs
      	* Get shorter profession/description from our list 'professionCategory'
	  * the list has been combiled while developping the network using Lists/professionCategory and semi-automated assigning of descriptions to the category that contains similar descriptions
	* Get ethnicity
	  * If the profession is in our list 'ethnicities'
	    * use the ethnicity there
	    * most individuals get 'Unknown' as ethnicity
        * Get the place name mentioned in the profession
	  * Find possible names of Places from the text (e.g. starts with Capital letter following of/from/in etc)
	    * If the place name is in our list of real places (placesAlone)
	      * use the first place name in the profession for the place of the individual
	* Get dating of the individual
	  * Correct obvious mistakes in the dating in PNA (e.g. latereign > late reign, probab-ably > probably, etc.)
	  * standardise dating in PNA (e.g. eighth > 8th, Sargon II > reign of Sargon II, etc.)
	  * find the dating in our list of 'timeperiods'
	    * use that for the dating of the individual
	  * Give number for each individual
		  * Write the information on the individuals to names.csv
	  * For each pair found in documents
		  * write the number of each individual from previous step + the weight of the connections to connections.csv
