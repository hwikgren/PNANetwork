### Lists with which the dataset was produced

concordances
* List compiled by us to find texts that have different names but are really the same document
* E.g. StAT 2 62;A 1055;As8645a;; use the first one instead of the the latter names

KingsSAAo
* List extracted from the Oracc SAAo project metadata
* If the king, during whose reign a text is written, is mentioned in the metadata as the sender or recipient of a letter, he is connected to the text
* E.g., SAA 1 175;Šarru-ken, Šarru-ukin_2
* Only used for the SAAo projects/books 1, 4, 5, 17, 18, 19, 21

knownProffs
* Used for assigning the raw PNA descriptions of people's professions (or similar) to broader categories
* Main category TAB Raw PNA information
* E.g., Craftspeople	Carpenter from Assur
* Each person in the network has a profession attribute that corresponds to a profession category
* The list has been compiled using the professionCategory list and semi-automatically assigning the raw PNA descriptions to the category that contains similar descriptions

languages
List of languages of the names of persons
* E.g. +Semitic
* 7 Akk. or Arab.?
* If the language of the name of an individual is the second line (without number), use the first line as language of the name

letterConversion
* List of signs and the letter they are converted to in names of persons
* E.g. a_ = ā
* Use latter instead of former

MAss_Radner_conversion
* List compiled by us to find the concordances between MAss documents and Radner forthcoming 
* (used for the pdf-document)
* E.g. Radner (forthcoming) I.33;MAss 10
* Use the MAss number instead of the Radner

oraccConcordances
* A concordance list of document names extracted from Oracc metadata (catalogue.json in each project)
* Oracc name and number ; designation ; accession_no ; museum_no ; popular_name ; publication_history
* E.g. saao/saa12/P235242;SAA 12 69;NARGD 42;_;VAT 9824;_;SAA 12 69
* Use the Oracc designation category (SAA 12 69) instead of the other ones

origins
* List of descriptions of people with the ethnic/regional origin extracted from them
* E.g. +Aramean
* 1 Sheikh of the Naqiru tribe son of
* If the profession/description of an individual is the second line (without number (= frequency)), use the first line as ethnicity

pdfAsText
***Tero: kuvaus puuttuu***

places
* List of allowed place names
* E.g. 	+Arbunâ
* +Argasu
* Argusu
* If a place name that has '+' sign in our list is found in the profession, assign that place to the individual
* If a name that has no '+' sign in our list is found in the profession, assign the previous place with '+' to the individual

professionCategory
* List of descriptions that correspond to a shorter profession or description
* The list was compiled by us during the development of the network and used for semi-automated assigning of descriptions to the category that contains similar descriptions
* E.g. +Military personnel
* 1 Leader of a group of archers from Nineveh
* If the profession/description of an individual is the second line, use the first line as short profession/description

SAAKings
* A list compiled by us connecting kings to SAA project texts even if they are not mentioned
* E.g. Aššūr-bāni-apli_1;SAA10;27;28;29;30;57;63;64;75;76;77;78;88;89;90;91;94;;96;100;101;104;105;131;134;136;137;138;139;140;141;152;173;174;180;182;186;195;196;197;224;225;226;227;228;276;345;346;381
* Only used for SAA projects/books 8, 9, 10, 12, 13, 15, 16

sameText.txt
* Our first list of documents that are in fact the same although they have different names
* E.g. ZA 73 10 == AfO 32 38 use the latter one instead of the first designation

timeperiods
* List of dating attributes that correspond to shorter dating with years
* E.g. +668-600 Assurbanipal and/or later
* 1 7th century probably reign of Assurbanipal or later
* If the dating of an individual is the second line, use the first line as date
