### A Social Network of the Prosopography of the Neo-Assyrian Empire

The networks were created from the text and pdf file versions of the Prosopography of the Neo-Assyrian Empire (PNA). 

The files in this folder are sufficient to study and visualize the networks. The other files in our Zenodo repository contain more indepth explanations of the procedure of creating the networks, the normalization lists used for that, and the output of different stages of the procedure. Below is a short explanation.

There are three different versions of the network, two one-mode networks and a two-mode network.

* The <b>names.csv</b> file contains the names of all the individuals in the network and some metadata extracted from PNA (e.g. language of a name, date, profession, ethnic origin, and place). This file can be used both with connections.csv and connectionsWeighted.csv to form a network. The attributes Start date and End date can be used in Gephi for enabling the Timeline feature.

* Using <b>names.csv and connections.csv</b> one can create a one-mode co-occurence network of persons attested in PNA. The weight of an edge equals the number of times two persons co-occur in different documents.

* Using <b>names.csv and connectionsWeighted.csv</b> one can create a one-mode co-occurence network of persons attested in PNA. The edge weights in this network are relative to the total number of persons attested in the same document which helps to alleviate false connections between people who had nothing to do with each other but just happened to be mentioned in the same text, such as a long list of officials. The weights have been calculated as follows: For each document in which two persons co-occur, subtract 1 from the total number of persons attested in the document and divide 1 by the difference \[1/(n-1)]. To calculate the edge weight, add up the weights in all the documents.

* Using <b>bimodal.csv</b> one can create a two-mode network of documents and the individuals appearing in them. Unlike names.csv this file does not not contain the node attributes.

The files <b>pairs.csv</b> and <b>pairsWeighted.csv</b> can also each be used to create the one-mode networks. The weights have been counted as in connections.csv and connectionsWeighted.csv, respectively. These files can be used, for example, in Gephi, but they do not contain the node attributes.

There are also gexf-files of the network versions which have been layouted with Gephi: <b>network.gexf</b>, <b>networkWeighted.gexf</b>, and <b>networkBiModal.gexf</b>. These can also be opened in Gephi. All these layouted networks can also be found in the Gephi-project <b>PNANetworks.gephi</b>.

Gephi is an open-source free visualisation tool, that can be downloaded here: https://gephi.org.

For the reasons of copyright, we are not able to publish the data from which the networks have been extracted. But we hope that by publishing all the source codes, concordance lists, and outputs with explanations we give the user a picture of how the networks were created.

#### A short explanation of the workflow:
* Getting document names from the PNA text files provided by Simo Parpola (Editor in Chief of PNA)
	* the document names are indicated with '@@' 
  * Various lists of concordances to standardize the document names were used
  * Each document name with the original line it was found in was saved for use in later steps
* Finding names and other info of individuals in the text files
	* Names are indicated with '\*' before them and individuals with the same name have been numbered
		* Individuals were given the number in PNA or 1 if only one individual has the specific name
		* The number was added to the name with underscore, e.g. Aššūr-iddin_1
	* The general description and dating of each individual as well as the language and gender of the name were also extracted
  * The documents in which an individual is mentioned were extracted by using the "original line - standardized name" conversion list produced in the previous step
* Finding names and other info of individuals in the text file extracted from PNA_3_2.pdf
	* As preprocessing, the pdf-file was copied to Word and then turned into a text file and semimanually curated
		* Names of individuals that in the pdf-file were marked with bold, were in Word marked with '\*'
		* Mistakes caused by copying from two-columns to one were corrected by hand
	* Individuals with the same name have been numbered and now names were indicated with '\*' before them 
		* Individuals are given the number used in PNA or 1 if only one individual has the specific name
		* The number is added to the name with underscore
	* The general description and dating of each individual as well as the language and gender of the name were also extracted 
	* Document names were found in the unstructured running text by using the list of document names in the text files + concordance lists
* Building a two-mode network of individuals and the documents they are attested in and a one-mode co-occurrence network of people attested in the same document
	* For the one-mode network, shorter/standardized versions of the language of a name, date, profession, ethnicity, and place were produced using various normalization lists (see folder Lists)
	* Writing csv files (a = two-mode network, b and c together form the one-mode network)
		1. of individuals and the documents they are attested in
		2. of individuals with their metadata
		3. of connections between the individuals in 2., the connections come from appearing in same documents

### Dataset creators
* Heidi Jauhiainen (University of Helsinki) was responsible for research design, data extraction and validation, dataset creation, and software development. 
* Tero Alstola (University of Helsinki) was responsible for research design, validation, and Assyriological research. 
* Saana Svärd (University of Helsinki) was responsible for conceptualization and funding acquisition. 
* Krister Lindén (University of Helsinki) was responsible for conceptualization and methodology. 
* Repekka Uotila (University of Helsinki) worked on normalization and reference lists.

### Acknowledgements

We gratefully acknowledge the work of our colleagues who have created The Prosopography of the Neo-Assyrian Empire. The PNA volumes were edited by Heather D. Baker and Karen Radner in the context of the Neo-Assyrian Text Corpus Project (PI Simo Parpola). Baker’s later work on PNA was carried out in the framework of the Royal Institutional Households in First Millennium BC Mesopotamia project, funded by the Austrian Science Fund (FWF). The individual entries in the prosopography were written by colleagues who are too many to be listed here. We thank Parpola for making the PNA volumes available to us in plain text and pdf formats, and Baker, Parpola, and Radner for the permission to publish the dataset online. Some metadata on the attestations of Assyrian kings was collected from the State Archives of Assyria Online, a part of the Munich Open-access Cuneiform Corpus Initiative (PIs Karen Radner and Jamie Novotny, LMU Munich). We also thank the members of the Centre of Excellence in Ancient Near Eastern Empires and the participants of several workshops for their helpful feedback on various versions of the dataset.
