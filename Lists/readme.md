### Lists used to produce the dataset

concordances
* A list compiled from the indexes of BATSH 6 and StAT 1-3 to find texts that have different names but are the same document
* E.g., StAT 2 62;A 1055;As8645a;;
* Use the first name instead of the the latter ones

KingsSAAo
* List extracted from the Oracc SAAo project metadata
* If the king, during whose reign a text is written, is mentioned in the metadata as the sender or recipient of a letter, he is connected to the text
* These attestations are often missing from PNA
* E.g., SAA 1 175;Šarru-ken, Šarru-ukin_2
* Only used for the SAAo projects/books 1, 4, 5, 17, 18, 19, 21

knownProffs
* Used for assigning the raw PNA descriptions of people's professions (or similar) to broader categories
* Main category TAB Raw PNA information
* E.g., Craftspeople	Carpenter from Assur
* Each person in the network has a Profession attribute that corresponds to a profession category
* The list has been compiled using the professionCategory list and semi-automatically assigning the raw PNA descriptions to the category that contains similar descriptions

languages
* List of languages of the personal names
* Used for assigning the raw PNA descriptions to broader categories
* E.g.,
* +Semitic
* 7 Akk. or Arab.?
* If the language of the personal name is the second line (without the number (= frequency)), use the first line as the person's NameOrigin attribute in the network

letterConversion
* PNA text files use combinations of ASCII characters to represent certain Unicode characters
* The list gives the sign combinations and the letters they are converted to in personal names
* E.g., a_ = ā
* Use the latter instead of the former

MAss_Radner_conversion
* List compiled from WVDOG 152 to find the concordances between the documents titled 'MAss' and 'Radner (forthcoming)' 
* Used for the pdf document
* E.g., Radner (forthcoming) I.33;MAss 10
* Use the MAss number instead of Radner (forthcoming)

oraccConcordances
* A concordance list of document names extracted from the Oracc metadata (catalogue.json in each Oracc project)
* This file is used for collapsing the duplicate names of the same text
* Oracc name and number; designation; accession_no; museum_no; popular_name; publication_history
* E.g. saao/saa12/P235242;SAA 12 69;NARGD 42;_;VAT 9824;_;SAA 12 69
* Use the Oracc designation category (SAA 12 69) instead of the other ones

origins
* List of raw PNA descriptions of people's ethnicities that correspond to broader categories
* Used for assigning the raw PNA descriptions to broader categories
* E.g.,
* +Aramean
* 1 Sheikh of the Naqiru tribe son of
* If the description of an individual is the second line (without the number (= frequency)), use the first line as the person's Ethnicity attribute in the network

pdfAsText
***Tero: tämä tiedosto pitänee poistaa tekijänoikeussyistä. Toimisiko tämänkin kohdalla se, että otetaan siitä lyhyt pätkä esimerkiksi?***

places
* List of allowed place names; used also for standardizing the variants
* E.g.,
* +Arbunâ
* +Argasu
* Argusu
* If a place name that has a plus sign in our list is found in the PNA description of an individual, use that place as the person's Place attribute in the network
* If a name that has no plus sign in our list is found in the PNA description of an individual, use the previous place with a plus sign as the person's Place attribute in the network

professionCategory
* List of raw PNA descriptions of people's professions that correspond to broader categories
* The list is used for semi-automated assigning of descriptions to the category that contains similar descriptions (see 'knownProffs')
* E.g.,
* +Military personnel
* 1 Leader of a group of archers from Nineveh
* If the profession/description of an individual is the second line (without the number (= frequency)), use the first line as the person's Profession attribute in the network

SAAKings
* A list compiled by us connecting kings to the SAA project texts that relate to the king even if he is not explicitly mentioned
* These instances are often missing from PNA
* E.g. Aššūr-bāni-apli_1;SAA10;27;28;29;30;57;63;64;75;76;77;78;88;89;90;91;94;;96;100;101;104;105;131;134;136;137;138;139;140;141;152;173;174;180;182;186;195;196;197;224;225;226;227;228;276;345;346;381
* Only used for the SAA projects/books 8, 9, 10, 12, 13, 15, 16

sameText.txt
* Our first list of documents that are in fact the same although they have different names
* E.g., ZA 73 10 == AfO 32 38
* Use the latter designation instead of the first one

timeperiods
* List of raw PNA descriptions of the period when a person is attested, corresponding to broader categories that also include exact years
* E.g.,
* +668-600 Assurbanipal and/or later
* 1 7th century probably reign of Assurbanipal or later
* If the dating of an individual is the second line (without the number (= frequency)), use the first line as the person's Time attribute in the network
***Tero: tämän timeperiods-tiedoston alusta voisi poistaa suomenkielisen kommenttirivin***
