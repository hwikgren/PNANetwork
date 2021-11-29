/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnacreatenetwork;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hwikgren
 */
public class PNACreateNetwork {

    /**
     * Assumes files:
      textfileNames.ser
      pdfNames.ser
 Assumes folder 'Lists' with files:
      KingsSaao
      SAAKings
      letterConversion
      timeperiods
      languages
      professionCategory
      places_alone
      origins
      known 
     */
    
    private static InputStream file;
    private static InputStream buffer;
    private static ObjectInput input;
    private static TreeMap<String, TreeMap<String, ArrayList<String>>> names;
    private static TreeMap<String, ArrayList<String>> reversed;
    private static TreeMap<String, String> letters;
    private static TreeMap<String, String> places;
    private static TreeMap<String, String> proffs;
    private static TreeMap<String, String> dates;
    private static TreeMap<String, String> languages;
    private static TreeMap<String, String> origins;
    private static TreeMap<String, String> knownProffs;
    private static TreeMap<String, String> duplicates;
    
    public static void main(String[] args) throws IOException {
        //FIRST READ THE NAMES OF INDIVIDUALS AND DOCUMENTS THEY APPEAR IN
        //TO names-treemap
        //read binary file with names in pna doc files
        readTextfileInfo();
        //read binary file with names in pdf file
        readPdfFileInfo();
        //read a list of SAAo documents written to or by a king
        readSAAoKings();
        //for some of the SAA volumes, use the list produced by ourselves
        readSAAkings();
        
        //read letter conversion list
        //used for changing the ascii character in the description to unicode
        readLetters();
        
        //READING THE VARIOUS LISTS USED FOR NORMALISING THE METADATA OF INDIVIDUALS
        //read list of accepted year frames for dates
        dates = new TreeMap<>();
        dates = readNormalisationList("Lists/timeperiods", dates);
        //read list of the accepted forms of the language of names
        languages = new TreeMap<>();
        languages = readNormalisationList("Lists/languages", languages);
        //read list of the origin or ethnicity of individuals
        //these are based on the description of persons
        origins = new TreeMap<>();
        origins = readNormalisationList("Lists/origins", origins);
        
        //read list of accepted place names
        //places list if different from others and cannot be read by the general normalisation list reader
        places = new TreeMap<>();
        places = readPlaceList("Lists/places", places);
        
        //read list of accepted professions/descriptions of individuals
        //the list has been combiled while developping the network
        //using Lists/professionCategory
        //and semi-automated assigning of descriptions to the category that contains similar descriptions
        proffs = new TreeMap<>();
        readProffs();
        
        //reverse the list of individuals-documents to documents-names
        reverse();
        
        //make network of people found in the same document(s)
        TreeMap<String, Double> newPairs = makePairs();
        
        //WRITE THE CSV-FILES TO BE USED IN GEPHI FOR VISUALISATION (not stdout)
        //The names and other information of the persons in one file (names.csv)
        //The The connections between people (through common documents) with the weight (how many dates) in another (connections.csv)
        //the normalisation of the metadata of individuals is done here!!!
        TreeMap<String, Integer> nodes = writeNameCsv(newPairs);
        writeConnectionCsv(newPairs, nodes);
        
        //WRITE INFORMATION TO FILE(S)
        //print all names in doc and pdf files (+ SAA kings) with documents they appear in
        printNames();
        //print all documents with at least 2 individuals and the names of the persons
        printDocs(reversed);
        //print all docPerson of people in common documents
        printPairs(newPairs);

        //Unquote to write a Bimodal network of individual document pairs
        makeBimodalNetwork();
    }
    
    //reverse the list of individuals-documents to documents-names
    //the new list connects different individuals who appear in the same document(s)
    private static void reverse() {
        reversed = new TreeMap<>();
        TreeMap<String, ArrayList<String>> content;
        ArrayList<String> docs, namesInDoc;
        String name;
        for (Map.Entry<String, TreeMap<String, ArrayList<String>>> entry : names.entrySet()) {
            name = entry.getKey();
            content = entry.getValue();
            if (content.containsKey("texts")) {
                docs = content.get("texts");
                // if the individual is attested in any allowed documents
                if (docs.size() > 0) {
                    for (String doc : docs) {
                        //a list of individuals who appear in one document 
                        namesInDoc = new ArrayList<>();
                        if (reversed.containsKey(doc)) {
                            namesInDoc = reversed.get(doc);
                        }
                        if (!namesInDoc.contains(name)) {
                            namesInDoc.add(name);
                        }
                        reversed.put(doc, namesInDoc);
                    }
                }
            }
            
        }
    }

    // make docPerson of individuals who appear together in one or more documents
    //returns a treeMap of docPerson with the normalised weight
    // the weight is calculated by adding up for each document an individual appears in
    //      1.0 divided by the number of persons in the document (-1)
    private static TreeMap<String, Double> makePairs() {
        TreeMap<String, Double> pairs = new TreeMap<>();
        ArrayList<String> persons;
        int nrOfPersons;
        double existingWeight, weight;
        String doc;
        for (Map.Entry<String, ArrayList<String>> entry : reversed.entrySet()) {
            doc = entry.getKey();
            // individuals mentioned in the document (doc)
            persons = entry.getValue();
            // the number of individuals in the document (doc)
            nrOfPersons = persons.size();
            //we are only interested in documents that have at least 2 person mentioned
            // if you want to leave out of the network the documents with most people, change '> 1' to, for example, '< 50'
            if (nrOfPersons > 1) {
                for (int i=0; i<nrOfPersons-1; i++) {
                    for (int j=i+1; j<nrOfPersons; j++) {
                        String pair = persons.get(i)+";"+persons.get(j);
                        existingWeight = 0.0;
                        if (nrOfPersons == 1) {
                            weight = 1.0;
                        }
                        else {
                            weight = (1.0 / (nrOfPersons-1));
                        }
                        //reverse the pair and check if it already is in map
                        //as we only want the pair once no matter which way written
                        //this never actually happens as the lists of persons in documents are always in alphabetical order
                        String raip = persons.get(j)+";"+persons.get(i);
                        if (pairs.containsKey(raip)) {
                            pair = raip;
                        }
                        //add pair to treeMap or if the pair is already there add the weight from the new document to the existing one
                        if (pairs.containsKey(pair)) {
                            existingWeight = pairs.get(pair);
                        }
                        pairs.put(pair, existingWeight+weight);
                      
                    }
                }
            }
        }
        return pairs;
    }
    
    //WRITE WANTED INFO
    
    //write the names of individuals and the names of the documents they appear in to the file Output/allNamesWithDocs
    private static void printNames() throws IOException {
        MyWriter writer = new MyWriter("Output/allNamesWithDocs");
        for (Map.Entry<String, TreeMap<String, ArrayList<String>>> entry : names.entrySet()) {
            TreeMap<String, ArrayList<String>> content = entry.getValue();
            writer.write(entry.getKey()+"\n");
            for (Map.Entry<String, ArrayList<String>> entry2 : content.entrySet()) {
                ArrayList<String> docs = entry2.getValue();
                for (String doc : docs) {
                    writer.write("\t"+doc+"\n");
                }
            }
        }
        writer.end();
    }
    
    //print all documents with at least 2 individuals and the names of the persons
    //to Output/allDocsWithIndividuals
    private static void printDocs(TreeMap<String, ArrayList<String>> reversed) throws IOException {
        MyWriter writer = new MyWriter("Output/allDocsWithIndividuals");
        ArrayList<String> temp = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : reversed.entrySet()) {
            temp = entry.getValue();
            if (temp.size() > 1) {
                writer.write(entry.getKey()+"\n");
                for (String item : temp) {
                    writer.write("\t"+item+"\n");
                }
            }
        }
        writer.end();
    }
    
    //write the docPerson of people that appear in common documents with the weight of the number of connections
    //to Output/pairs
    private static void printPairs(TreeMap<String, Double> newPairs) throws IOException {
        MyWriter writer = new MyWriter("Output/pairs");
        for (Map.Entry<String, Double> entry : newPairs.entrySet()) {
            if (entry.getValue() > 0) {
                writer.write(entry.getKey()+";"+entry.getValue()+"\n");
            }
        }
        writer.end();
    }
    
    //write a bi-modal network of documents and individuals (without weight)
    //to Output/biModal.csv
    private static void makeBimodalNetwork() throws IOException {
        MyWriter writer = new MyWriter("biModal.csv");
        ArrayList<String> docPerson = new ArrayList<>();
        ArrayList<String> persons;
        String doc;
        for (Map.Entry<String, ArrayList<String>> entry : reversed.entrySet()) {
            doc = entry.getKey();
            persons = entry.getValue();
            if (persons.size() > 1) {
                for (String perso : persons) {
                    if (!docPerson.contains(doc+";"+perso)) {
                        docPerson.add(doc+";"+perso);
                    }
                }
            }
        }
        writer.write("Source;Target\n");
        for (String pair : docPerson) {
            writer.write(pair+"\n");
        }
        writer.end();
    }

    // WRITE THE NETWORK INTO A CSV FILE FOR THE USE IN GEPHI
    // persons with their information > names.csv
    //the normalisation of the metadata of individuals is done here!!!
    private static TreeMap<String, Integer> writeNameCsv(TreeMap<String, Double> pairs) throws IOException {
        MyWriter writer = new MyWriter("names.csv");
        //write the headings needed in Gephi
        writer.write("Id;Label;NameOrigin;Gender;Time;Profession;Place;Ethnicity;start date;end date\n");
        //the individual persons form the nodes in the network
        TreeMap<String, Integer> nodes = new TreeMap<>();
        //treemap with the metadata on an individual
        TreeMap<String, ArrayList<String>> content = new TreeMap<>();
        //a list of the infos on an individual (in oder: gender, description, origin, year)
        ArrayList<String> infos = new ArrayList<>();
        String gender, language, date, editedProff, proff, origin, place;
        //running number for the nodes
        int nodeNr = 1;
        //we interate through the docPerson 
        //since we know that all individuals in them have at least one connection to another person
        for (Map.Entry<String, Double> entry : pairs.entrySet()) {
            String pair = entry.getKey();
            String[] pairArray = pair.split(";");
            if (entry.getValue() > 0) {
                for (String node : pairArray) {
                    //we only need to write an individual once
                    //the connections are done separately in writeConenctionCsv-method
                    //but we put each individual to nodes treemap with a running number
                    //nodes is used when writing the connections csv-file
                    if (!nodes.containsKey(node)) {
                        nodes.put(node, nodeNr);
                        //we get the infos from names-treemap
                        if (names.containsKey(node)) {
                            content = names.get(node);
                            if (content.containsKey("info")) {
                                infos = content.get("info");
                            }
                        }
                        //get the normalised language of the name
                        language = setLanguage(infos.get(0).trim());
                        //check gender
                        gender = setGender(infos.get(1).trim());
                        //get the normalised dating of the individual from the year originally in the description
                        date = setDate(infos.get(3).trim());
                        //get the start and end year from the year period in the dating of the individual
                        //can be used in Gephi for activating the timeline feature
                        String startYear = setYears(date, true);
                        String endYear = setYears(date, false);
                        //first check and normalise the description
                        editedProff = editProff(infos.get(2).trim());
                        //get the short normalised profession/description
                        proff = setProff(editedProff);
                        //get the normalised place name found in the original description
                        place = setPlace(infos.get(2).trim());
                        //get the normalised origin/ethincity of the individual from the description
                        origin = setOrigin(editedProff);
                        writer.write(nodeNr+";"+node+";"+language+";"+gender+";"+date+";"+proff+";"+place+";"+origin+";"+startYear+";"+endYear+"\n");
                        nodeNr++;
                    }
                }
            }
        }
        writer.end();
        return nodes;
    }
    // connections between persons in common documents (with weight) > connections.csv
    private static void writeConnectionCsv(TreeMap<String, Double> pairs, TreeMap<String, Integer> nodes) throws IOException {
        MyWriter writer = new MyWriter("connections.csv");
        //write the column heading needed in Gephi
        writer.write("Source;Target;Weight\n");
        //iterate through the docPerson treemap > the connection and their weight
        //and get the number of the individuals 
        for (Map.Entry<String, Double> entry : pairs.entrySet()) {
            String pair = entry.getKey();
            String[] pairArray = pair.split(";");
            writer.write(nodes.get(pairArray[0]).toString()+";"+nodes.get(pairArray[1]).toString()+";"+entry.getValue()+"\n");
        }
        writer.end();
    }
    
    
    //CONVERT THE METADATA OF AN INDIVIDUAL TO NORMALISED FORMS
    
    //normalise the form of the gender desinations
    private static String setGender(String gender) {
        gender = gender.replaceAll("[\\p{Punct}]", "");
        if (gender.contains(" wr ")) {
            gender = gender.split(" wr ")[0];
        }
        if (gender.equals("mac")) {
            gender = "masc";
        }
        if (gender.contains("masc") && gender.contains("fem")) {
            gender = "masc/fem";
        }
        if (gender.isEmpty() || !gender.matches("[(masc)|(fem)].*")) {
            gender = "Unknown";
        }
        return gender;
    }
    
    //normalise the profession/description of an inscription
    //start by cleaning
    private static String editProff(String proff) {
        proff = proff.trim();
        proff = proff.replaceAll(" +", " ");
        proff = proff.replaceAll("[\\(\\)]", "");
        proff = proff.replaceAll("\\*\\*.*", "_");
        if (proff.isEmpty()) {
            proff = "_";
        }
        proff = proff.replaceAll("\"", "");
        proff = proff.replaceAll("[;,]", "");
        proff = proff.replaceAll("^A ", "");
        proff = proff.replaceAll("^The ", "");
        proff = proff.replaceAll(":.*$", "");
        proff = proff.substring(0,1).toUpperCase() + proff.substring(1);
        proff = proff.replaceAll(" +", " ");
        return proff;
    }
    
    //find the short profession that matches the description of the person
    private static String setProff(String editedProff) {
        String shortProff = "";
        //all the description are, in fact, in our list of professions
        //check anyways
        if (proffs.containsKey(editedProff)) {
            shortProff = proffs.get(editedProff);
        }
        else {
            shortProff = "_";
        }
        return shortProff;
    }
    
    //set origin/ethnicity of a person from his/her profession/description
    private static String setOrigin(String editedProff) {
        //the origin/ethnicity cannot be interpreted from all descriptions
        //so we mark origin as 'Unknown' by default
        String origin = "Unknown";
        //if the description is found in origins-treeMap
        //get the origin 
        if (origins.containsKey(editedProff)) {
            origin = origins.get(editedProff);
        }
        return origin;
    }
    
    //get the normalised language designation of the name
    private static String setLanguage(String language) {
        language = language.replaceAll("\\([^\\)]*\\)", "").trim();
        language = language.replaceAll(" +", " ");
        language = language.replaceAll(",", "");
        if (!language.isEmpty() && languages.containsKey(language)) {
            //System.out.print(language);
            language = languages.get(language);
            //System.out.println("\t"+language);
        }
        else {
            if (!language.isEmpty()) {
                //System.out.println(language);
            }
            language = "Unknown";
        }
        return language;
    }
    
    //clean and correct the dating of the individual from the year originally in the description
    //return the short date from dates-treemap
    private static String setDate(String date) {
        //first we normalise many inconsistance and mistakes
        date = date.replaceAll("(\\s+)|(\\t)", " ");
        date = date.replaceAll(", possibly", " and possibly");
        date = date.replaceAll("[\\)\\[\\];,]", "");
        date = date.replaceAll(" of the ", " of ");
        date = date.replaceAll("ddonand", "ddon and");
        date = date.replaceAll("paland", "pal and");
        date = date.replaceAll("(As-surbanipal)|(Assur-banipal)|(Assurbani-pal)|(A ssurbanipal)|(Assurbanpal)|(Assur-baipal)", "Assurbanipal");
        if (date.matches(".* or[A-Z].*")) {
            date = date.replaceAll(" or", " or ");
        }
        if (date.matches(".* of[A-Z`].*")) {
            date = date.replaceAll(" of", " of ");
        }
        date = date.replaceAll("afterreign", "after reign");
        date = date.replaceAll("and after", "and later");
        date = date.replaceAll("(or after)|(orlater)", "or later");
        if (date.matches(".* and[a-zA-Z].*")) {
            date = date.replaceAll(" and", " and ");
        }
        date = date.replaceAll("the +reign", "reign");
        if (date.matches(".*reigns?of .*")) {
            date = date.replaceAll("of ", " of ");
        }
        date = date.replaceAll("reign or", "reign of");
        date = date.replaceAll("(\\{umm[^ ]*. )|(\\{ummu\\|)|(1=.*)", "");
        if (date.isEmpty() || date.matches("\".*\"")|| date.equals("possibly thebrother")) {
            date = "_";
        }
        date = date.replaceAll("(612 ?reign)|(612reign)", "612 reign");
        date = date.replaceAll("nedzar", "nezzar");
        date = date.replaceAll("poss-ibly", "possibly");
        if (date.matches(".*possibly[a-z].*")) {
            date = date.replaceAll("possibly", "possibly ");
        }
        if (date.matches(".*[a-z]possibly.*")) {
            date = date.replaceAll("possibly", " possibly");
        }
        if (date.contains("possibly") && !date.contains("and")) {
            date = date.replaceAll("possibly", "and possibly");
        }
        date = date.replaceAll("possible", "possibly");
        if (date.matches(".*cherib[a-z].'")) {
            date = date.replaceAll("cherib", "cherib ");
        }
        date = date.replaceAll("cheribthrough", "cherib through");
        date = date.replaceAll("Aššur\\-nerari", "Assur-nerari");
        date = date.replaceAll("ner\\-ari", "nerari");
        date = date.replaceAll("(A@@ur)|(Aššur)|(Asur)|(Asssur)|(Ashur)", "Assur");
        date = date.replaceAll("Shal\\-maneser", "Shalmaneser");
        date = date.replaceAll("\\?Shalmaneser IV", "Shalmaneser IV?");
        date = date.replaceAll("Ti\\-glath", "Tiglath");
        date = date.replaceAll("Tiglathpileser", "Tiglath-pileser");
        date = date.replaceAll("Sar\\-gon", "Sargon");
        date = date.replaceAll("(Sanherib)|(Senacherib)|(Sen-nacherib)|(Sennach-erib)", "Sennacherib");
        date = date.replaceAll("(Si\\^n\\-@arru\\-i@kun)|(Sin\\-@ar\\-i@kun)", "Sin-@arru-i@kun");
        date = date.replaceAll("thcentury", "th century");
        if (date.matches(".*early[a-zA-Z1-9].*") || date.matches(".*probably[a-zA-Z1-9].*")) {
            date = date.replaceAll("early", "early ");
            date = date.replaceAll("probably", "probably ");
        }
        if (date.matches(".*[a-z]probably .*")) {
            date = date.replaceAll("probably", " probably");
        }
        date = date.replaceAll("orearly", "or early");
        if (date.matches(".* reign [A-Z].*") || date.matches("reign [A-Z].*")) {
            date = date.replaceAll("reign", "reign of");
        }
        if (date.matches("Sargon II")) {
            date = "reign of Sargon II";
        }
        if (date.matches("Sennacherib")) {
            date = "reign of Sennacherib";
        }
        if (date.matches("Shalmaneser IV\\?")) {
            date = "reign of Shalmaneser IV?";
        }
        if (date.matches("Tiglath-pileser III")) {
            date = "reign of Tiglath-pileser III";
        }
        if (date.matches("Adad-nerari III")) {
            date = "reign of Adad-nerari III";
        }
        date = date.replaceAll("during ", "");
        date = date.replaceAll("reing", "reign");
        date = date.replaceAll("cen-tury", "century");
        date = date.replaceAll("afer ", "after ");
        if (date.matches("(\\? .*)|(an?d?)|(ass.*)|(comm.*)|(ia)|(ki)|(name .*)|([ox\\{\\}@\\-=].*)|([st\\?]\\??)|(tab.*)|(tem.*)|(food.*)|(prece.*)|(.* temple)|(with)")) {
            date = "_";
        }
        if (date.matches("after [A-Z].*")) {
            date = date.replaceAll("after ", "after reign of ");
        }
        date = date.replaceAll("(reigns)|(time)", "reign");
        date = date.replaceAll("th or", "th century or");
        date = date.replaceAll("latereign", "late reign");
        date = date.replaceAll("eighth", "8th");
        date = date.replaceAll("seventh", "7th");
        date = date.replaceAll("second", "2nd");
        date = date.replaceAll("perhaps", "possibly");
        date = date.replaceAll("presumably", "probably");
        date = date.replaceAll("prob\\-ably", "probably");
        date = date.replaceAll("\\-(ni)rari|(Ne)rari", "-nerari");
        date = date.replaceAll("\\-\\-", "-");
        date = date.replaceAll("[^\\-]nerari", "-nerari");
        date = date.replaceAll("Ada\\-ner", "Adad-ner");
        date = date.replaceAll("(Asarhaddon)|(Esahaddon)|(Esarhad-don)|(Esar-haddon)", "Esarhaddon");
        if (date.matches(".*[a-eg-z]or .*")) {
            date = date.replaceAll("or ", " or ");
        }
        date = date.replaceAll("(\\s+)|(\\t)", " ");
        if (date.matches(".*late[1-9].*")) {
            date = date.replaceAll("late", "late ");
        }
        if (date.matches(".*[a-z]II.*")) {
            date = date.replaceAll("II", " II");
        }
        if (date.matches(".*III[a-zA-Z].*")) {
            date = date.replaceAll("III", "III ");
        }
        if (date.matches(".* II[a-zA-HJ-Z].*")) {
            date = date.replaceAll("II", "II ");
        }
        if (date.matches(".* V[ao].*")) {
            date = date.replaceAll("V", "V ");
        }
        if (date.matches(".*[a-z]V .*")) {
            date = date.replaceAll("V", " V");
        }
        date = date.trim();
        //all the possible dates are in the dates-treemap
        if (dates.containsKey(date)) {
            date = dates.get(date);
        }
        return date;
    }
    
    //get the normalised place name found in the description of the individual
    private static String setPlace(String line) {
        String place = "", match;
        Boolean Of = false;
        line = line.replaceAll(" +", " ");
        //not all names that look like place names are them
        //we search first for special cases
        if (line.contains("New Town of Assur")) {
            return "Assur";
        }
        if (line.contains("Inner City")) {
            if (line.contains("Assur")) {
                return "Assur";
            }
            if (line.contains("Kalhu")) {
                return "Kalhu";
            }
        }
        if (line.contains("Town of the")) {
            
            match = getMatch("(Town of the [^ ]*)", line);
            if (places.containsKey(match)) {
                return places.get(match);
            }
            else if (line.matches(".*Town of the [^ ]* [^ ]*.*")) {
                match = getMatch("(Town of the [^ ]* [^ ]*)", line);
                //System.out.println(match);
                if (places.containsKey(match)) {
                    return places.get(match);
                }
            }
        }
        if (line.contains("City of the Donkey Drivers")) {
            return places.get("City of the Donkey Drivers");
        }
        if (line.contains("Brewer")) {
            match = getMatch("(Brewer[^ ]* [^ ]*)", line); 
            if (places.containsKey(match)) {
                return places.get(match);
            }
        }
        if (line.contains("Eunuch Town")) {
            return places.get("Eunuch Town");
        }
        if (line.contains("King's village")) {
            return places.get("King's village");
        }
        //normalise the Till/Tell names
        if (line.matches(".* T[ie]ll? .*")) {
            String end = getMatch("T[ie]ll? ([^ ]*)", line);
            return "Til-"+end;
        }
        
        //get the word after certain words that might precede a place name
        //where the person is active or from
        //this way we can find place names that are not necessary the the first one in the line
        //but that tell more about where the person lives
        //these forms were also used for creating our list of accepted place names
        if (line.contains("from")) {
            place = line.replaceFirst(".*from ", "");
        }
        else if (line.contains("active in")) {
            place = line.replaceFirst(".*active in ", "");
        }
        else if (line.contains("working in")) {
            place = line.replaceFirst(".*working in ", "");
        }
        else if (line.contains(" at ")) {
            place = line.replaceFirst(".* at ", "");
        }
        else if (line.contains(" stationed ")) {
            place = line.replaceFirst(".*stationed ", "");
        }
        else if (line.contains(" in the region of ")) {
            place = line.replaceFirst(".*region of ", "");
            
        }
        else if (line.matches(".*[Mm]ayor of .*")) {
            place = line.replaceFirst(".*ayor of ", "");
        }
       else if (line.matches(".*[gG]overnor of .*")) {
            place = line.replaceFirst(".*overnor of ", "");
        }
        //then normalise the possible place name
        //first replace ascii characters with unicode
        place = convertName(place);
        //System.out.println(place);
        //remove everything before the first capital letter
        place = place.replaceFirst("^[^A-ZĀĀĪŪÂÊÎÛŠṢṬ]*", "");
        //then take the first word (the one starting with a capital letter)
        if (!place.equals("")&& place.lastIndexOf(" ") != -1) {
                place = place.substring(0, place.indexOf(" "));
        }
        //System.out.println(place);
        //remove extra characters
        place = place.replaceAll("[,:/]$", "");
        place = place.replaceAll("\\(\\?\\)", "");
        //check if the found place name is in our list of accepted places
        //and if necessary get a normalised form
        if (places.containsKey(place)) {
            return places.get(place);
        }
        //not all descriptions contain the words with which we found the possible places
        //we check each word in the line for a place name in our list
        //and get the normalised form for the first one
        String[] lineArray = line.split(" ");
        for (String word : lineArray) {
            word = word.replaceAll("[,:/]", "");
            word = word.replaceAll("\\(\\?\\)", "");
            if (places.containsKey(word)) {
                return places.get(word);
            }
        }
        //if no accepted place name was found in the line
        return "Unknown";
    }
    
    //READ IN INFORMATION FROM FILES
    
    //the information on the doc files is read in from the binary file textfileNames.ser
    //and put into names-treemap
    private static void readTextfileInfo() throws IOException {
        file = null;
        buffer = null;
        input = null;
        try {
            file = new FileInputStream("textfileNames.ser");
            buffer = new BufferedInputStream(file);
            input = new ObjectInputStream(buffer);
            names = (TreeMap<String, TreeMap<String, ArrayList<String>>>) input.readObject();
        }
        catch (Exception e) {
            System.out.println(e); 
        }
        finally {
            input.close();
            buffer.close();
            file.close();
        }
    }
    
    //the information on the pdf file is read in from the binary file pdfNames.ser
    //and added to names-treemap already contining the individuals from textfiles
    private static void readPdfFileInfo() throws IOException {
        TreeMap<String, TreeMap<String, ArrayList<String>>> tempNames = new TreeMap<>();
        TreeMap<String, ArrayList<String>> content;
        ArrayList<String> infos;
        ArrayList<String> texts;
        try {
            file = new FileInputStream("pdfNames.ser");
            buffer = new BufferedInputStream(file);
            input = new ObjectInputStream(buffer);
            //read the info first to a temporary treemap
            tempNames = (TreeMap<String, TreeMap<String, ArrayList<String>>>) input.readObject();
        }
        catch (Exception e) {
            System.out.println(e); 
        }
        finally {
            input.close();
            buffer.close();
            file.close();
        }
        //some checking while adding individuals to names-treemap
        for (Map.Entry<String, TreeMap<String, ArrayList<String>>> entry : tempNames.entrySet()) {
            TreeMap<String, ArrayList<String>> hisInfos = entry.getValue();
            String name = entry.getKey();
            if (name.equals("Ululaiu_3")) {
                ArrayList<String> newDocs = hisInfos.get("texts");
                hisInfos = names.get("Salmānu-ašarēd_5");
                ArrayList<String> oldDocs = hisInfos.get("texts");
                for (String doc : newDocs) {
                    if (!oldDocs.contains(doc)) {
                        oldDocs.add(doc);
                    }
                }
                hisInfos.put("texts", oldDocs);
                names.put("Salmānu-ašarēd_5", hisInfos);
                
            }
            else if (!names.containsKey(name)) {
                names.put(name, hisInfos);
            }
        }
    }
    
    // a list of letter conversions is read in to a treeMap
    // the list should be of the form 'foundLetter = wantedLetter'
    //used for converting ascii to unicode
    private static void readLetters() throws FileNotFoundException, IOException {
        letters = new TreeMap<>();
        BufferedReader reader = null;
        String line = "";
        try {
            reader = new BufferedReader(new FileReader("Lists/letterConversion"));
            while ((line = reader.readLine()) != null) {
                String[] lineArray = line.split(" = ");
                if (lineArray.length == 3) {
                    letters.put(lineArray[0], lineArray[2]);
                    letters.put(lineArray[1], lineArray[2]);
                }
                else {
                    letters.put(lineArray[0], lineArray[1]);
                }
            }
        }
        catch (Exception e) {
            System.out.println("Reading letters: "+e+"\t"+line);
        }
        finally {
            reader.close();
        }
    }
    
    // various lists of normalisation are read into treeMap provided in the call
    // takes the conversion list filename and the treeMap as parameters
    // the list should be of the form:
        // +wantedForm
        //      notWantedForm
        //      another notWantedForm
        // +another wantedForm
        //      notWantedForm
        //  etc.
    //i.e. when a notWantedForm is found in doc, the wantedForm is used instead
    // returns the treeMap with notWantedForm and wantedForm
    private static TreeMap<String, String> readNormalisationList(String filename, TreeMap<String, String> listMap) throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        String line = "", wantedWord, otherWord;
        String[] lineArray;
        try {
            reader = new BufferedReader(new FileReader(filename));
            line = reader.readLine();
            line =  Normalizer.normalize(line, Normalizer.Form.NFD);
            while (line != null) {
                //wanted word starts with '+'
                if (line.startsWith("+")) {
                    wantedWord = line.substring(1);
                    //some files may have explanations, we ignore them
                    if (wantedWord.contains(":")) {
                        wantedWord = wantedWord.split(":")[0];
                    }
                    //check the next lines until one starting with '+' is found
                    line = reader.readLine();
                    while (line != null && !line.startsWith("+")) {
                        if (!line.isEmpty()) {
                            line = line.trim();
                            //cleaning
                            line = line.replaceAll("-", "-");
                            line = line.replaceAll("[:,]", "");
                            line = line.replaceAll(" +", " ");
                            //the line may have a number in the beginning
                            //we leave that out
                            lineArray = line.split("^[^ ]* ");
                            if (lineArray.length > 1) {
                                otherWord = lineArray[1];
                                listMap.put(otherWord, wantedWord);
                            }
                        }
                        line = reader.readLine();
                    }
                }
                //espacially in the beginning of the file there might be other kinds of lines
                //which are ignored
                else {
                    line = reader.readLine();
                }
            }
        }
        catch (Exception e) {
            System.out.println(e+"\t"+line);
        }
        finally {
            reader.close();
        }
        return listMap;
    }
    
    //read list of accepted place names
    //places list if different from others and cannot be read by the general normalisation list reader
    //the list contains place names that are allowed
    //'+' at the beginning of the line is an allowed place name and might not have other variants
    private static TreeMap<String, String> readPlaceList(String filename, TreeMap<String, String> listMap) throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        String line = "", wantedWord;
        try {
            reader = new BufferedReader(new FileReader(filename));
            line = reader.readLine();
            while (line != null) {
                if (line.startsWith("+")) {
                    wantedWord = line.substring(1).trim();
                    if (wantedWord.contains(":")) {
                        wantedWord = wantedWord.split(":")[0];
                    }
                    line = reader.readLine();
                    //if there are variants add them to the wanted word
                    while (line != null && !line.startsWith("+")) {
                        if (!line.isEmpty()) {
                            line = line.trim();
                            line = line.replaceAll("-", "-");
                            line = line.replaceAll("\t", "");
                            listMap.put(line, wantedWord);
                        }
                        line = reader.readLine();
                    }
                    //but also add the wanted word
                    listMap.put(wantedWord, wantedWord);
                }
                else {
                    line = reader.readLine();
                }
            }
        }
        catch (Exception e) {
            System.out.println(e+"\t"+line);
        }
        finally {
            reader.close();
        }
        return listMap;
    }
    
    
    //for certain SAA volumes we have our own list of documents 
    //that are written to or by the king
    //the volumes are: 8, 9, 10, 12, 13, 15, 16
    //the form of the file to read is:
    //King's name;SAA vol;page;page-page,page...
    //the information is added to the names-treemap
    private static void readSAAkings() throws IOException {
        BufferedReader reader = null;
        String line = "", king, saa, docName, number;
        int start, end;
        String[] lineArray;
        TreeMap<String, ArrayList<String>> contents;
        ArrayList<String> docs;
        try {
            reader = new BufferedReader(new FileReader("Lists/SAAKings"));
            while ((line = reader.readLine()) != null) {
                
                king = line.split(";")[0];
                lineArray = line.split(";");
                contents = names.get(king);
                saa = lineArray[1];
                docs = new ArrayList<>();
                //get the list of documents of the king in question
                if (contents.containsKey("texts")) {
                    docs = contents.get("texts");
                }
                //get the page numbers which can be single or a range (e.g. 3-12)
                for (int i=2; i<lineArray.length; i++) {
                    number = lineArray[i];
                    if (!number.equals("")) {
                        if (number.contains("-") || number.contains("–")) {
                            start = Integer.parseInt(number.split("[-–]")[0]);
                            end = Integer.parseInt(number.split("[-–]")[1])+1;
                        }
                        else {
                            start = Integer.parseInt(number);
                            end = Integer.parseInt(number)+1;
                        }
                        for (int j=start; j<end; j++) {
                            docName = saa+" "+j;
                            if (!docs.contains(docName)) {
                                docs.add(docName);
                            }
                        }
                    }
                    else {
                        break;
                    }
                }
                contents.put("texts", docs);
                names.put(king, contents);
            }
        }
        catch (Exception e) {
            System.out.println("SAA kings:"+e+"\t"+line);
        }
        finally {
            reader.close();
        }
    }
    
    //read a list of document name - King's name docPerson
    //the list if compiled from Oracc SAAo metadata 
    //and indicates that the document was written by or to the king
    //the information is added to the names-treemap
    private static void readSAAoKings() throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        String line = "", king, saa, docName, number;
        int start, end;
        String[] lineArray;
        TreeMap<String, ArrayList<String>> contents;
        ArrayList<String> docs;
        try {
            reader = new BufferedReader(new FileReader("Lists/KingsSAAo"));
            while ((line = reader.readLine()) != null) {
                if (line.contains(";")) {
                    saa = line.split(";")[0];
                    king = line.split(";")[1];
                    if (names.containsKey(king)) {
                        //System.out.println(king);
                        contents = names.get(king);
                        docs = new ArrayList<>();
                        if (contents.containsKey("texts")) {
                            //System.out.println(king);
                            docs = contents.get("texts");
                        }
                        docs.add(saa);
                        contents.put("texts", docs);
                        names.put(king, contents);
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println("Reading Saao Kings: "+line+"\t"+e);
        }
        finally {
            reader.close();
        }
    }
        
    //read the known profession/descriptions to proffs-treemap
    //each line contains the wanted short description and the longer description found in PNA
    //with PNANameExtractor.java and PNAPdfNameExtractor.java
    //the list has been combiled while developping the network
    //using Lists/professionCategory
    //and semi-automated assigning of descriptions to the category that contains similar descriptions
    private static void readProffs() throws IOException {
        BufferedReader reader = null;
        String line;
        String[] lineArray;
        try {
            reader = new BufferedReader(new FileReader("Lists/knownProffs"));
            while ((line = reader.readLine()) != null) {
                lineArray = line.split("\t");
                proffs.put(lineArray[1], lineArray[0]);
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
    }
    
    //ADDITIONAL METHODS
    
    //return 
    private static String getMatch(String pattern, String line) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(line);
        String match = "";
        if (matcher.find()) {
            match = matcher.group(1);
            //System.out.println(match);
        }
        return match;
    }
    
    private static String convertName(String name) {
        String newName = "";
        //System.out.println(name);
        for (int i=0; i<name.length(); i++) {
            String letter = String.valueOf(name.charAt(i));
            if (letters.containsKey(letter)) {
                newName += letters.get(letter);
            }
            else if (i<name.length()-1 && letters.containsKey(letter+""+String.valueOf(name.charAt(i+1)))) {
                newName += letters.get(letter+""+String.valueOf(name.charAt(i+1)));
                i++;
            }
            else {
                newName += letter;
            }
            //System.out.println(name+"\t"+newName);
        }
        return newName;
    }
    
    //get the start or end year of the year of an individual
    //indicate by boolean value whether start (true) or end (false) is wanted
    private static String setYears(String date, boolean start) {
        //System.out.println(year);
        String year = "";
        //the years are in the beginning of the date
        String years = date.split(" ")[0];
        //sometimes there was no date or it could not be extracted
        //we return a year (for both start and end) that is not in any actual dates
        if (date.equals("N/A")) {
            return "01/01/2000";
        }
        //depending on whether the start of end is wanted
        //return either the part before or after the '-' sign
        //the day and month are also needed, but are arbitrary
        if (start) {
            year = years.split("[-–-]")[0];
            return "01/01/"+year;
        }
        else {
            year = years.split("[-–-]")[1];
            return "31/12/"+year;
        }
    }
    

    
    private static void readDuplicates(String filename) throws IOException {
        duplicates = new TreeMap<>();
        BufferedReader reader = null;
        String line = "";
        String[] lineArray;
        try {
            reader = new BufferedReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                lineArray = line.split("\t");
                if (!duplicates.containsKey(lineArray[0])) {
                    duplicates.put(lineArray[0], lineArray[1]);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
    }
    
}
