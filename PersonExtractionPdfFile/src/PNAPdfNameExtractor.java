/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnapdfnameextractor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 /**
 *
 * @author hwikgren
 */
public class PNAPdfNameExtractor {

    /**
     * @param args the command line arguments
     * 0 = name of the pdf file as text file
     * Assumes a folder named Lists with the following files:
     *      letterConversion
     *      MAss_Radner_conversion
     * Assumes also a folder named Output with the following files
     * (produced with PNATextExtractor.java and PNANameExtractor.java):
     *      docsInTextfiles
     *      allDuplicates
     */
    
    //letter conversion from ascii to unicode
    private static TreeMap<String, String> letters;
    //document names found in text files and in concordance lists (from oracc on SAAo)
    //first word acts as a key and each document name starting with that word is in 'textVariants'
    private static TreeMap<String, TreeMap<Integer, ArrayList<String>>> documents;
    //thumbprints made from all document names in documents
    private static ArrayList<String> thumbprints;
    //treeMap for textVariants, the length of the longest variant is the key
    private static TreeMap<Integer, ArrayList<String>> nrAndTexts;
    //list of text variants all starting with the same word
    private static ArrayList<String> textVariants;
    //
    private static int length;
    //list of document names found in one snippet of a line
    private static ArrayList<String> docs;
    //names of individuals with their other info and the names of the documents their name is found in
    private static TreeMap<String, TreeMap<String, ArrayList<String>>> names;
    //all duplicate names found in text files and in concordance lists (from oracc on SAAo)
    private static TreeMap<String, String> duplicates;
    //conversion list from our MAss <> Rander (forthcoming) list
    private static TreeMap<String, String> MAss;
    //private static String wholeText;
    
    public static void main(String[] args) throws IOException {
        //name of the text file extracted from the pdf
        String filename = args[0];
        thumbprints = new ArrayList<>();
        //read names of all documents in text files to documents-treemap
        readDocumentNames();
        //read our list of ascii letter conversion to unicode and save them to letters-treemap
        readLetterConversion();
        
        //read the file with all duplicate names (not Oracc except SAA) and textfile duplicates
        readDuplicateNames();
        
        //read Radner fortcoming > MAss conversion list
        readMAssRadnerList();
        
        //Find individuals and document names in the pdf-file
        readPdfFile(filename);
        
        //save the names-treemp to binary file for use in PNACreateNetworks.java
        writeInfoToBinaryFile();
        
        //print the names and info of individuals to file individualsInPdffile
        writeIndividuals(names);
        
        //OPTIONALLY UNQUOTE THE COMMAND TO PRINT JUST THE DOCUMENT NAMES FOUND IN THE PDF-FILE
        //writeDocs();
    }
    
    //iterate through the pdf-filea and find individuals and document names
    //The form of the file:
    //*name (translation of name); language of name; gender of name; transliteration.
    //number. //if several individual with the same name// short description/profession (dating): longer description with document names in running text
    //<<scholar who added to database>>.
    private static void readPdfFile(String filename) throws IOException {
        BufferedReader reader = null;
        String line = "", name = "", ethnicity="", gender="", proff = "", time = "";
        String line2 = "";
        String end = "";
        String[] lineArray;
        TreeMap<String, ArrayList<String>> content;
        boolean ended = true, king = false, insideKing = false;
        try {
            reader = new BufferedReader(new FileReader(filename));
            names = new TreeMap<>();
            line = reader.readLine();
            while (line != null) {
                end = "";
                //when << has been reached add the last individual with the particular name to names-treemap
                if (line.startsWith("<<")) {
                    ended = true;
                    insideKing = false;
                    king = false;
                    /*if (names.containsKey(name)) {
                        content = names.get(name);
                        ArrayList<String> text = new ArrayList<>();
                        if (!wholeText.equals("")) {
                            text.add(wholeText);
                            content.put("text", text);
                            names.put(name, content);
                        }
                    }
                    wholeText = "";*/
                }
                //if line starts with *, get the name and information on the name
                else if (line.startsWith("*") && ended) {
                    ended = false;
                    //get the name from the beginning of the line
                    name = line.replaceFirst(" \\(.*", "");
                    //take out the * from the beginning of the line
                    name = name.replaceFirst("\\*", "");
                    //want to divide from ';' but there are some case where ';' is used in translation of the name
                    if (line.indexOf(";") < line.indexOf(")")) {
                        line = line.replaceFirst(";", "");
                    }
                    lineArray = line.split(";");
                    
                    //convert the ascii letters to unicode
                    name = convertName(name);
                    //Normalise the first part of the line and, if needed, divide the line again
                    if (lineArray[0].matches(".*\\), .*") || lineArray[0].matches(".*\\): .*")) {
                        line = line.replaceFirst("\\)[,:]", ");");
                        lineArray = line.split(";");
                    }
                    else if (lineArray[0].matches(".* \\([^\\)]+\\) [A-Z].*")) {
                        line = line.replaceFirst("\\) ", "); ");
                        lineArray = line.split(";");
                    }
                    //get the language/ethnicity of the name
                    ethnicity = lineArray[1].trim();
                    if (ethnicity.contains(",")) {
                        line = line.replaceFirst(",", ";");
                        lineArray = line.split(";");
                        ethnicity = lineArray[1].trim();
                    }
                    //get the gender of the name
                    gender = lineArray[2].trim();
                    if (ethnicity.equals("masc.") || ethnicity.equals("fem.")) {
                        gender = ethnicity;
                        ethnicity = "_";
                    }
                    
                }
                //handle the next lines
                else if (!ended) {
                    //get the information on the individual before the longer description
                    String beginning = line.substring(0, line.indexOf(":")+1);;
                    //special case
                    if (beginning.matches("[IV]+\\. .*") && king) {
                        insideKing = true;
                    }
                    //if the line starts with a number and full stop 
                    //or capital letter and small letter (i.e. a word in the beginning of sentence)
                    if (beginning.matches("([0-9]{1,2}\\..*)|(“?[A-Z][a-z].*)")) {
                        //but not if the number refers to several individuals
                        if (beginning.matches("[0-9]{1,2}\\.-.*") || beginning.matches("[0-9]{1,2}\\. or.*")) {
                            line = reader.readLine();
                            continue;
                        }
                        else if (beginning.matches("[0-9]{1,2}\\. .*")) {
                            if (!insideKing) {
                                //add the previous individual to the names-treemap
                                if (names.containsKey(name)) {
                                    content = names.get(name);
                                    ArrayList<String> text = new ArrayList<>();
                                    /*if (!wholeText.equals("")) {
                                        text.add(wholeText);
                                        content.put("text", text);
                                        names.put(name, content);
                                    }*/
                                }
                                king = false;
                                //get the profession/hosrt description
                                proff = beginning.substring(beginning.indexOf(" ")+1, line.indexOf(":"));
                                if (name.contains("_")) {
                                    name = name.substring(0, name.indexOf("_"));
                                }
                                if (proff.contains("king of Assyria") || proff.contains("Assyrian king") || proff.contains("ruler of upper Mesopotamia")) {
                                    king = true;
                                }
                                //add the number from the beginning of the line to the end of the name
                                name = name+"_"+beginning.substring(0, line.indexOf("."));
                                
                            }
                        }
                        //if the line does not start with a number, mark the name with number 1
                        else {
                            if (king == false && !name.contains("_")) {
                                proff = beginning.substring(0, line.indexOf(":"));
                                name = name+"_1";
                            }
                        }
                        if (!insideKing) {
                            time = "_";
                        }
                        //clean the the short description and extract the dating of the individual
                        if (proff.endsWith(")")) {
                            time = proff.substring(proff.lastIndexOf("(")+1, proff.length()-1);
                            proff = proff.substring(0, proff.lastIndexOf("(")-1);
                        }
                        if (proff.matches(".*[0-9]{3,3}\\-[0-9]{3,3}.*") && time.equals("_")) {
                            time = matchYears(" ([0-9]{1,2}[0-9][0-9]\\-[1-9][0-9][0-9]{1,2})", proff);
                        }
                    }
                    //get the longer description from the line
                    end = getEnd(line).trim();
                    //the description can be divided into sections starting with letters or numbers
                    if (beginning.matches("[0-9]+\\. .*") || beginning.matches("[A-Z][a-z].*")) {
                        if (end.matches("[a-z]\\. .*") || end.startsWith("1′. ")) {
                            beginning = end.substring(0, end.indexOf(":")+1);
                            //check for unwanted information
                            if (checkBeginning(beginning, king)) {
                                end = getEnd(end);
                            }
                            else {
                                end = "";
                            }   
                        }
                        //check for unwanted information
                        else if (!checkBeginning(beginning, king)) {
                            //if unwanted, check the next line
                            line = getNextWantedLine(beginning, reader);
                            continue;
                        }
                    }
                    //check for unwanted information and if necessary find the next wanted line
                    else if (!checkBeginning(beginning, king)) {
                        line = getNextWantedLine(beginning, reader);
                        continue;
                    }
                    //special case that is not corrected with convert name
                    time = time.replaceAll("Aššur-", "A@@ur-");
                    //if the longer description contains at least one white space
                    if (end.matches(".* .*")) {
                        //A section talking about the content of one document often ends with '(date);'
                        //split the description at each ');' and ').'
                        String[] splitLines = end.split("\\)[\\.;]");
                        for (String splitLine : splitLines) {
                            //sometimes there is no date for the document, so also split with just ';'
                            String[] semiSplits = splitLine.split(";");
                            //process all the smaller snippets for document names in 'processLine' method
                            for (String semi : semiSplits) {
                                //split into even smaller snippets from 1*) i.e. from the date of a document
                                //this bring mostly just envelopes but also some legit documents
                                String[] demiSplits = semi.split("([0-9]\\*\\))|(dated\\))|(lost\\))");
                                for (String demi : demiSplits) {
                                    //check the snippet for document names
                                    //if any found, add the information to the names-treemap
                                    if (processLine(demi)) {
                                        content = new TreeMap<>();
                                        ArrayList<String> info = new ArrayList<>();
                                        ArrayList<String> textNames = new ArrayList<>();
                                        proff = Normalizer.normalize(proff, Normalizer.Form.NFD);
                                        //if the individual is already in names-treemap
                                        //the the info for that name
                                        if (names.containsKey(name)) {
                                            content = names.get(name);
                                            //info = content.get("info");
                                            if (content.get("texts") != null) {
                                                textNames = content.get("texts");
                                            }
                                        }
                                        //if not yet there, add the collected info into info treemap
                                        else {
                                            //System.out.println(name);
                                            info.add(ethnicity);
                                            info.add(gender);
                                            info.add(proff);
                                            info.add(time);
                                            content.put("info", info);
                                        }
                                        //add the document names found in the snippet to the texts of the individual
                                        for (String textName : docs) {
                                            if (!textNames.contains(textName)) {
                                                textNames.add(textName);
                                                content.put("texts", textNames);
                                            }
                                        }
                                        //add or update the info of this individual in the names-treemap
                                        names.put(name, content);

                                    }
                                }
                            }
                        }
                    }
                }
                line = reader.readLine();
            }
        }
        catch (Exception e) {
            System.out.println("Reading text: "+e+"\t"+end);
        }
        finally {
            reader.close();
        }
    }
    
    //write the names and info of each individual to the file individualsInPdffile
    private static void writeIndividuals(TreeMap<String, TreeMap<String, ArrayList<String>>> names) throws IOException {
        MyWriter writer = new MyWriter("Output/individualsInPdffile");
        for (Map.Entry<String, TreeMap<String, ArrayList<String>>> entry : names.entrySet()) {
            String name = entry.getKey();
            TreeMap<String, ArrayList<String>> content = entry.getValue();
            ArrayList<String> infos = content.get("info");
            ArrayList<String> textNames = content.get("texts");
            if (textNames != null && !textNames.isEmpty()) {
                writer.write(name+"\n\t");
                for (String info : infos) {
                    writer.write(info+"\t");
                }
                writer.write("\n");
                for (String text : textNames) {
                    writer.write("\t"+text+"\n");
                }
            }
        }
        writer.end();
    }
    
    //write the names of all documents found in the pdf file into file docsInPdffile
    private static void writeDocs() throws IOException {
        MyWriter writer = new MyWriter("Output/docsInPdfFile");
        TreeSet<String> docs = new TreeSet<>();
        for (Map.Entry<String, TreeMap<String, ArrayList<String>>> entry : names.entrySet()) {
            TreeMap<String, ArrayList<String>> content = entry.getValue();
            ArrayList<String> textNames = content.get("texts");
            if (textNames != null && !textNames.isEmpty()) {
                for (String text : textNames) {
                    docs.add(text);
                }
            }
        }
        for (String doc : docs) {
            writer.write(doc+"\n");
        }
        writer.end();
    }
    
    //Check the profession/short description for signs of use of dating or royal inscriptions that do not tell about connections
    private static boolean checkBeginning(String beginning, boolean king) {
        if (beginning.matches(".*(([Dd]ating )|([Ee]ponym)|(date formula)|(In dates)).*")) {
            return false;
        }
        if (king && beginning.matches(".*((royal inscription)|(In (an)|(the)? inscriptions? of)|([Kk]ing [Ll]ist)).*")) {
            return false;
        }
        return true;
    }
    
    //get the longer description from the line
    private static String getEnd(String line) {
        String end = "";
        if (line.indexOf(":") != line.length()-1) {
            end = line.substring(line.indexOf(":")+2);
        }       
        //take out parentheses from around year numbers
        if (!end.isEmpty()) {
            String match;
            if (end.matches(".* \\([0-9]{4,4}\\) .*")) {
                match = getMatch("\\(([0-9]{4,4})\\)", end);
                end = end.replaceAll("\\("+match+"\\)", match);
            }
        }
        else {
            end = line;
        }
        return end;
    }
    
    //look for document names in text snippets
    private static boolean processLine(String line) {
        boolean found = false;
        boolean thisFound = false;
        docs = new ArrayList<>();
        //A document name often end with ':' before the line number
        //but since we cannot be sure whether there are other cases too
        //we replace the ':' with space and handle "words" indicated by spaces
        if (line.matches(".*[0-9][\\+A-Z]?:[0-9].*")) {
            line = line.replaceAll(":", " ");
        }
        //remove all parentheses
        line = line.replaceAll("[\\(\\)]", "");
        
        //CLEAN INCONSISTANCES
        line = line.replaceAll(" +", " ");
        line = line.replaceAll("SA A", "SAA");
        line = line.replaceAll("StA T", "StAT");
        //in pdf Ass 12345 is a mistake for As12345
        if (line.contains(" Ass ")) {
            line = line.replaceAll(" Ass ", " Ass");
            if (!line.contains("AssU")) {
                line = line.replaceAll("Ass", "As");
            }
        }
        if (line.matches(".* As[0-9]+[a-z]*:.*")) {
            line = line.replaceAll(":", " ");
        }
        line = line.replaceAll(" VA T", "VAT");
        line = line.replaceAll("BA TSH", "BATSH");
        line = line.replaceAll("CTN 2", "GPA");
        line = line.replaceAll("BaM ", "BaM");
        
        //split texts into "words" indicated by spaces
        String[] lineArray = line.split(" ");
        String word, textLine = "";
        found = false;
        //iterate throught the words and look for ones that are keys in the documents-treemap
        for (int i=0; i<lineArray.length; i++) {
            thisFound = false;
            word = lineArray[i];
            //€ is only found at the end of the line
            if (word.endsWith("€") || word.equals("€")) {
                break;
            }
            //if the following words are of form A 1... (i.e. one letter space numbers)
            //replace 'A' with AX
            if (word.length() == 1 && line.length() > 2 && i<lineArray.length-1) {
                if (lineArray[i+1].matches("[0-9].*")) {
                    word = word+"X";
                }
            }
            //IF WORD IS 'RADNER' AND IS FOUND IN MAss CONCORDANCE
            //Radner (forthcoming) documents are in the other volumes called MAss
            //normalise from our list
            if (word.equals("Radner")) {
                String word2 = lineArray[i+2].replaceAll("\\*", "");
                if (MAss.containsKey(word+" "+word2)) {
                    addText(MAss.get(word+" "+word2));
                    thisFound = true;
                    //return true;
                }
            }
            //OR WORD IS A KEY IN DOCUMENTS-TREEMAP
            else if (documents.containsKey(word) || word.matches("As[0-9].*")) {
                //thisFound = false;
                if (word.equals("Orient")) {
                    word = "Orient 29";
                }
                //get the rest of the line starting with the word starting a document name
                textLine = cutLine(lineArray, i);
                textLine = textLine.replaceAll("Tadmor 1994", "Tadmor");
                //if the line has duplicate names for the document
                if (textLine.contains(". ") && !line.contains(" r. ")) {
                    //System.out.println(textLine);
                }
                //this will only find on valid document on a line
                //sometimes a line does not contain ';' between documents (usually a king)
                //and there might be multiple document publicate pairs but no way to recognize them automatically
                if (textLine.contains(" = ")) {
                    thisFound = handleDuplicates(textLine);
                    if (thisFound) {
                        return true;
                    }
                    //return found;
                }
                //else find the possible name of a document
                else {
                    //find the possible document name
                    textLine = getCandidateName(textLine).trim();
                    //Check if a valid document name can be found in the line
                    //and add it to the list of documents for this individual
                    thisFound = findDocumentName(textLine, false);
                }
            }
            //if a document was found at any point of the line
            //we mark the found as true but continue to the end of the snippet
            if (thisFound) {
                found = true;
            }
        }
        //inform the calling method whether at least one document name was found or not
        return found;

    }
    
    //return just the rest of the line starting with the index indicated
    //used when the index indicates a word that starts a document name
    private static String cutLine(String[] lineArray, int i) {
        String textLine = "";
        for (int j=i; j<lineArray.length; j++) {
            textLine += lineArray[j]+" ";
        }
        return textLine.trim();
    } 
    
    //if line contains '=', i.e. it contains duplicate names for the documents
    //handle the duplicate names separately
    private static Boolean handleDuplicates(String textLine) {
        //split from '=', i.e. handle the duplicate names separately
        String[] stringArray = textLine.split(" = ");
        Boolean found = false;
        //if either side of '=' starts with a name we do not want
        //interrupt
        /*for (int k=0; k<stringArray.length; k++) {
            if (!checkText(stringArray[k])) {
                return false;
            }
        }*/
        //we want Hunger if it is on the line
        if (!textLine.startsWith("Hunger") && textLine.contains(" = Hunger")) {
            textLine = stringArray[1];
            if (!textLine.startsWith("Hunger")) {
                textLine = stringArray[2];
            }
        }
        //we also want Hug
        else if (!textLine.startsWith("Hug") && textLine.contains(" = Hug")) {
            textLine = stringArray[1];
            if (!textLine.startsWith("Hug")) {
                textLine = stringArray[2];
            }
            textLine = textLine.replaceFirst("1993 [0-9]{0,2}", "");
        }
        else {
            //from different possibilities only accept an existing one
            for (int k=0; k<stringArray.length; k++) {
                //foundInVariants = false;
                textLine = stringArray[k];
                //get a possible document name
                textLine = getCandidateName(textLine).trim();
                //check if this candidate is a valid document name
                //if it is found in the textVariants, the findDocumentName return true
                //as we are here handing duplicate names indicated with '=', thumbprints are not yet checked
                found = findDocumentName(textLine, true);
                //if one of the names on the line is found in variants
                //as another one cannot possibly have been found (the cycle would have been stopped),
                //assume that there is only one name in docs and return true (this has been tested!)
                //ON A LONG LINE WITHOUT ';', OTHER NAMES THAT ARE NOT DUPLICATE NAMES FOR THE FOUND ONE MAY BE LOST
                if (found) {
                    return found;
                }
                
            }
            //if no existing document name was found in textVariants, 
            //we accept the first one that can be found using the thumbprints
            if (!found) {
                for (int k=0; k<stringArray.length; k++) {
                    textLine = stringArray[k];
                    textLine = getCandidateName(textLine).trim();
                    found = findDocumentName(textLine, false);
                    return found;
                }
            }
        }
        //The document names starting with Hunger or Hug have not been found yet
        textLine = getCandidateName(textLine).trim();
        found = findDocumentName(textLine, false);
        return found;
    }
    
    //when line starts with one of the keys in documents-treemap
    //find the possible document name
    private static String getCandidateName(String line) {
        //split the line into words
        String[] lineArray = line.split(" ");
        int j = 1;
        String word = lineArray[0];
        String candidate = word+" ";
        //iterate though the words
        //if an end for the name is found, break and return the candidate name
        while (j<lineArray.length) {
            String nextWord = lineArray[j];
            //if the word contains number with '-', keep only the part before '-'
            if (nextWord.matches(".*[0-9]-.*")) {
                nextWord = nextWord.split("-")[0].trim();
                if (!nextWord.isEmpty()) {
                    candidate += nextWord+" ";
                    j++;
                }
            }
            //handle special case of Orient 29
            else if (word.equals("Orient 29")) {
                if (nextWord.equals("1993")) {
                    candidate += nextWord+" ";
                    j++;
                }
                else if (nextWord.matches("no\\.[1-9]\\.[0-9]+")) {
                    candidate += nextWord.replaceAll("no\\.", "")+" ";
                    break;
                }
                else if ((nextWord.matches("no\\.?") || nextWord.matches("[1-9][0-9][0-9],?"))) {
                    j++;
                }
                else if (nextWord.matches("[0-9]\\.[0-9]+")) {
                    candidate += nextWord+" ";
                    break;
                }
            }
            //we want Hug without the year 1993
            else if (word.equals("Hug") && nextWord.equals("1993")) {
                j++;
            }
            //both T. Hadid 1 and T. Hadid 2 are T. Hadid 1
            else if (word.equals("T.") && nextWord.equals("Hadid")) {
                if (!lineArray[j+1].matches("[12]")) {
                    candidate += "Hadid 1 ";
                    break;
                }
                else {
                    candidate += nextWord+" ";
                    j++;
                }
            }
            //if next word has number+r+., remove the r. and ignore the rest of the line
            else if (nextWord.matches(".*[0-9]r\\..*")) {
                candidate += nextWord.split("r\\.")[0]+" ";
                break;
            }
            //if the word ends with punctuation, it marks the end of the document name
            else if (nextWord.matches(".*[;:,\\.\\{€]")) {
                candidate += nextWord.replaceAll("[;:\\{\\.,€]", "")+" ";
                //System.out.println(wordArray[j]);
                break;
            }
            //else keep adding words till the end of the line
            else {
                candidate += nextWord.replaceAll("[\\*\\+]", "")+" ";
                j++;
            } 
        }
        return candidate;
    }
    
    //Check if a valid document name can be found in the line
    //and add it to the list of documents for this individual
    private static boolean findDocumentName(String textLine, boolean severalTexts) {
        //foundText = "";
        String modelLine;
        //normalising
        if (textLine.contains("SAAB 9 Appendix")) {
            textLine = textLine.replaceAll("Appendix 2a", "A2");
        }
        if (textLine.contains("Orient") && !textLine.contains("1993")) {
            textLine = textLine.replaceAll("29", "29 1993");
        }
        //split again to words
        String[] textArray = textLine.split(" ");
        //get the lenght of the line, i.e. number of words
        int j=textArray.length;
        //iterate through the line, removing each time the last word
        while (j>0) {
            textLine = "";
            //first make the line of this iteration
            //each time j is smaller and the line is shorter
            for (int i=0; i<j; i++) {
                //remove X that was inserted to names starting with a single letter
                if (i==0 && textArray[0].endsWith("X")) {
                    textLine += textArray[0].substring(0, 1)+ " ";
                }
                else {
                    textLine += textArray[i]+" ";
                }
            }
            textLine = textLine.trim();
            if (!checkText(textLine)) {
                return false;
            }
            //remove 'f' from after the last number
            if (textLine.matches(".*[0-9]f+$")) {
                textLine = textLine.replaceFirst("f+$", "");
            }
           // System.out.println(textLine);
            //check if duplicates contains the line
            //in that case replace the line with the duplicate name
            if (duplicates.containsKey(textLine)) {
                textLine = duplicates.get(textLine);
               // System.out.println("Dup: "+textLine);
            }
            //get info on the possible document names for first word of the line
            //these are assigned to the variables
            //length tells the length of the longest variants
            //textVariant-list contains all the variants that start with the word
            getDocNamePossibilities(textLine.split(" ")[0]);
            //no use to check the variants if the line is still longer than the longest variant
            if (textLine.length() <= length) {
                //else if the line is in the list of textVariants
                //update the variables for other methods to see
                //add the found document name to to the list of documents for this individual
                if (textVariants.contains(textLine)) {
                    //foundInVariants = true;
                    //foundText = textLine;
                    //System.out.println("Adding: "+textLine);
                    addText(textLine);
                    return true;
                }
                //in other cases make a thumbprint of the possible document name
                //and compare this to the thumbprints from the document names in the text files
                //small variations are allowed
                //if found, add the document name to to the list of documents for this individual
                else {
                    if (!severalTexts) {
                        modelLine = getThumbprint(textLine);
                        if (thumbprints.contains(modelLine)) {
                            addText(textLine);
                            return true;
                        }
                        //if exact match was not found but thumbprints contains:
                        //the same with one additional number
                        else if (thumbprints.contains(modelLine+"1")) {
                            addText(textLine);
                            return true;
                        }
                        //the same without the letter at the end
                        else if (modelLine.matches(".*x") && thumbprints.contains(modelLine.substring(0, modelLine.length()-1))) {
                            addText(textLine.substring(0, textLine.length()-1));
                            return true;
                        }
                        
                    }
                }
            }
            //if not found yet make j smaller, until no words are left
            j--;
        }
        //if nothing was found, return false
        return false;
    }
    

    //add document name to the list of documents for the individual on the line in question
    //if not present yet
    private static void addText(String textLine) {
        if (!docs.contains(textLine)) {
            if (checkText(textLine)) {
                docs.add(textLine);
            }
        }
    }
    
    //if documents-treemap contains the word as key
    //get the length of the longest variant and all the variants of the document names starting with the word
    private static void getDocNamePossibilities(String word) {
        if (documents.containsKey(word)) {
            nrAndTexts = documents.get(word);
            length = nrAndTexts.firstKey();
            textVariants = nrAndTexts.get(length);
        }
        if (word.equals("cf.")) {
            System.out.println(textVariants.toString());
        }
    }
    
    //read names of all documents in text files
    //First part of the name (before first white space) is the key
    //all names starting with the same key are added its textVariants-list
    private static void readDocumentNames() throws FileNotFoundException, IOException {
        documents = new TreeMap<>();
        TreeMap<Integer, ArrayList<String>> nrAndTexts;
        ArrayList<String> textVariants;
        BufferedReader reader = null;
        String line = "";
        int length;
        try {
            reader = new BufferedReader(new FileReader("Output/docsInTextfiles"));
            while ((line = reader.readLine()) != null) {
                String textCode = "";
                //get the key, the first word of the name
                if (line.contains(" ")) {
                    textCode = line.substring(0, line.indexOf(" "));
                }
                else {
                    textCode = line;
                }
                //?
                if (textCode.length() == 1) {
                    if (line.substring(2, 3).matches("[0-9]")) {
                        textCode = textCode+"X";
                    }
                }
                addToDocuments(textCode, line);
            }
        }
        catch (Exception e) {
            System.out.println("Reading document name variants: "+e+"\t"+line);
        }
        finally {
            reader.close();
        }
    }
    
    //add a document name to the documents-treemap
    //where the first word is the key and each name starting with the same word is a textVariant
    //also keep track of the longest variant in each list
    //ALSO make a thumbprint of each document name and insert it into thumbprints-list
    private static void addToDocuments(String textCode, String line) {
        textVariants = new ArrayList<>();
        nrAndTexts = new TreeMap<>();
        length = 0;
        //Add the key and the text variant to documents + update the length of the length variant if needed
        if (documents.containsKey(textCode))  {
            nrAndTexts = documents.get(textCode);
            length = nrAndTexts.firstKey();
            textVariants = nrAndTexts.get(length);
            documents.remove(textCode);
        }
        if (line.length() > length) {
            nrAndTexts.remove(length);
            length = line.length();

        }
        textVariants.add(line);
        nrAndTexts.put(length, textVariants);
        documents.put(textCode, nrAndTexts);
        String thumbprint = getThumbprint(line);
        if (!thumbprints.contains(thumbprint)) {
            thumbprints.add(thumbprint);
        }
    }
    
    //read our list of ascii letter conversion to unicode and save them to letters-treemap
    private static void readLetterConversion() throws FileNotFoundException, IOException {
        letters = new TreeMap<>();
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader("Lists/letterConversion"));
            while ((line = reader.readLine()) != null) {
                String[] lineArray = line.split(" = ");
                letters.put(lineArray[0], lineArray[1]);
            }
        }
        catch (Exception e) {
            System.out.println("Reading letters: "+e);
        }
        finally {
            reader.close();
        }
    }
    
    //read Radner fortcoming > MAss conversion list
    private static void readMAssRadnerList() throws IOException {
        MAss = new TreeMap<>();
        BufferedReader reader = null;
        String line;
        String[] lineArray;
        try {
            reader = new BufferedReader(new FileReader("Lists/MAss_Radner_conversion"));
            while ((line = reader.readLine()) != null) {
                lineArray = line.split(";");
                MAss.put(lineArray[0].replaceAll("\\(forthcoming\\) ", ""), lineArray[1].replaceAll("\\+.*", ""));
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
    }
    
    //the names in the text files use ascii letters
    //convert the letters to unicode
    private static String convertName(String name) {
        String newName = "";
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
    
    //return part of the line that matches the regex pattern given
    private static String getMatch(String pattern, String line) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(line);
        String match = "";
        if (matcher.find()) {
            match = matcher.group(1);
        }
        
        return match;
    }
    
    //return a thumbprint of the document name given
    private static String getThumbprint(String name) {
        String form = name.trim().replaceAll("[0-9]", "1");
        form = form.replaceAll(" ", "_");
        form = form+"_";
        form = form.replaceAll("\\+", "X");
        String pattern = ".*1([a-zA-Z\\+]+)_.*";

        String match = getMatch(pattern, form);
        
        String replacement = "";

        for (int i=0; i<match.length(); i++) {
            if (match.charAt(i) == 'X') {
                replacement += "X";
            }
            else {
                replacement += "x";
            }
        }
        form = form.replaceAll("1"+match, "1"+replacement);
        if (form.endsWith("_")) {
            form = form.substring(0, form.length()-1);
        }
        return form;
    }
    
    //if the description indicated using the name for dating or being royal inscription
    //we do not want the documents from sections under it (eg. 1 > a 1' or a' and a > 1' or a' etc.)
    private static String getNextWantedLine(String beginning, BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (beginning.matches("[a-z]\\. .*")) {
            while (line.matches("[0-9a-z]+′\\. .*")) {
                line = reader.readLine();
            }
            
            return line;
        }
        else if (beginning.matches("[0-9]+′\\. .*")) {
            while (line.matches("[a-z]+′\\. .*")) {
                line = reader.readLine();
            }
            return line;
        }
        return line;
    }
    
    //Check the line for unwanted document names
    private static boolean checkText(String line) {
        if (line.matches("Borger 19[59]6 .*") ||
                line.matches("ADD App") ||
                line.matches("AfO [34] .*") ||
                line.matches("A 0?117 .*") ||
                line.startsWith("As14616c") ||
                line.startsWith("Istanbul A") ||
                line.matches("KAV 0?1[012] .*") ||
                line.matches("KAV 0?21 .*") ||
                line.matches("KAV 9:.*") ||
                line.matches("CT [23]6 .*") ||
                line.matches("Iraq 16 18[23].*") ||
                line.matches("Iraq 41 56.*") ||
                line.startsWith("Frahm 1997") ||
                line.startsWith("K 6109 ") ||
                line.startsWith("K 2411 ") ||
                line.startsWith("Grayson 1975") ||
                line.startsWith("ND 3466") ||
                line.startsWith("ND 5475") ||
                line.startsWith("Luckenbill 1924") ||
                line.startsWith("Cyl. D+E") ||
                line.startsWith("Sg D+E") ||
                line.matches("2 ?R .*") ||
                line.startsWith("Grayson 1991a") ||
                line.startsWith("Grayson 1992") ||
                line.matches(".*Hunger \\(1968\\) 256.*") ||
                line.startsWith("Parpola 1986") ||
                line.startsWith("Parpola 2008") ||
                line.startsWith("SAA 6 10 SAA")
                ) {
            return false;
        }
        return true;
    }
    
    //Write the names-treemap to a binary file called pdfFile.ser
    //to be used later in PNACreateNetwork.java
    private static void writeInfoToBinaryFile() throws IOException {
        OutputStream file = null;
        OutputStream buffer = null;
        ObjectOutput output = null;
        try {
            file = new FileOutputStream("pdfNames.ser", true);
            buffer = new BufferedOutputStream(file);
            output = new ObjectOutputStream(buffer);
            output.writeObject(names);
        }
        catch (IOException e) {
            System.out.println("cannot write to file:   "+e);
        }
        finally {
            output.close();
            buffer.close();
            file.close();
        }
    }

    //read the file containing all the duplicate names found in textfiles and in our concordance lists
    //(from oraccConcordances only SAAo)
    //both the name to be changed and the wanted name are added to documents-treemap (using addToDocuments-method)
    //  this so that possible names are found in the pdf file
    //the pair is then added to the duplicates-treemap
    private static void readDuplicateNames() throws FileNotFoundException, IOException {
        duplicates = new TreeMap<>();
        BufferedReader reader = null;
        String line = "", changed, textCode = "";
        String[] lineArray;
        TreeMap<Integer, ArrayList<String>> nrAndTexts;
        ArrayList<String> textVariants;
        int length;
        try {
            reader = new BufferedReader(new FileReader("Output/allDuplicates"));
            while ((line = reader.readLine()) != null) {
                lineArray = line.split("\t");
                changed = lineArray[0];
                if (!changed.equals("_")) {
                    if (changed.contains("=")) {
                        continue;
                    }
                    if (changed.contains(" ")) {
                        textCode = changed.substring(0, changed.indexOf(" "));
                        if (textCode.length() == 1 && changed.length() > 2) {
                            if (changed.substring(2, 3).matches("[0-9]")) {
                                textCode = textCode+"X";
                            }
                            else {
                                continue;
                            }
                        }
                    }
                    else {
                        textCode = changed;
                    }
                    addToDocuments(textCode, changed);
                }
                String duplicate = lineArray[1];
                 if (duplicate.contains(" ")) {
                        textCode = duplicate.substring(0, duplicate.indexOf(" "));
                        if (textCode.length() == 1 && duplicate.length() > 2) {
                            if (duplicate.substring(2, 3).matches("[0-9]")) {
                                textCode = textCode+"X";
                            }
                            else {
                                continue;
                            }
                        }
                    }
                    else {
                        textCode = duplicate;
                    }
                    addToDocuments(textCode, duplicate);
                if (!duplicates.containsKey(changed)) {
                    duplicates.put(changed, duplicate);
                }
            }
        }
        catch (Exception e) {
            System.out.println("Reading duplicates: "+e+"\t"+line);
        }
        finally {
            reader.close();
        }
        
    }
    
    //return the year numbers inside the line given
    private static String matchYears(String pattern, String line) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(line);
        String match = "";
        if (matcher.find()) {
            match = matcher.group(1);
        }
        return match;
    }
    
}
