/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnanameextractor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
  * @author Heidi Jauhiainen, University of Helsinki
 * A script for extracting the names of individual persons 
 * from the text files used for printing the Prosopography of the Neo-Assyrian Empire volumes I/1- III/1.
 */
public class PNANameExtractor {

    /**
     * 
     * @param args the command line arguments
     * args[0] folder where the PNA documentNames files are
     * Assumes a binary file named docs.ser containing the documentNames treeMap with doc names and the line they appear in
     * Assumes a folder named Lists with the following file:
     *      letterConversion
     * Assumes also a folder named Output where writes lists to be used later
     * writes the treeMap with names of individual, their info + documentNames they appear in into a binary file called textfileNames.ser
     */
    
    //document names with original lines
    private static TreeMap<String, String> documentNames;
    //names of individuals with info and documentNames they appear in
    private static TreeMap<String, TreeMap<String, ArrayList<String>>> names;
    //letter conversion from ascii to unicode
    private static TreeMap<String, String> letters;
    
    public static void main(String[] args) throws IOException {
        //folder where the PNA text files are
        File folder = new File(args[0]);
        //import documentNames treeMap from a binary-file called documentNames.ser, 
        //which has the normalised document names with the original line where found
        readBinaryFile();
        //read letter conversion file called 'letterConversion'
        readLetterConversion();
        
        names = new TreeMap<>();

        //iterate through the text files used for making the printed books of PNA
        readTextFiles(folder);
        
        //write all the information of each individual to a text file
        writeNames();
        
        //write the names
        writeInfoToBinaryFile();
    }
    
    //Read all the text files used for printing PNA books
    //the format of the files is:
    //*Name
    //(translation of name); language of name; gender
    //information on the name (can span several lines)
    //
    //Profession (time period when lived)   //Line starts with a number if several individuals with the same name, otherwise not
    //information on the individual     //can span over several lines
    //@@Document name line (year)
    //more information
    //@@Document name line (year)
    //      //This can go on for several paragraphs and be divided into numbered list with might be devided into list marked with letters etc.
    
    //<<Name of scholar who added to database
    private static void readTextFiles(File folder) throws IOException {
        BufferedReader reader = null;
        String line = "", name = "", proff = "", time = "";
        
        ArrayList<String> docs, nameAttributes = new ArrayList<>();
        String[] lineArray;
        
        for (File file : folder.listFiles()) {
            try {
                reader = new BufferedReader(new FileReader(file));
                while ((line = reader.readLine()) != null) {
                    //Lines starting with ** give information on names elsewhere
                    //read lines until en empty line is found
                    if (line.startsWith("**")) {
                        while (!line.isEmpty()) {
                            line = reader.readLine();
                            //System.out.println(line);
                        }
                    }
                    //* markes a new name
                    if (line.startsWith("*") && !line.startsWith("**") && !(line.endsWith(" 2.)."))) {    
                        //take out the *
                        name = line.substring(1).trim();
                        //convert the ascii letters to unicode
                        name = convertName(name);
                        //remove ';' that are not consistent
                        name = name.replaceAll(";", "");
                        //add number one to the end of name
                        name += "_1";
                        line  = reader.readLine();
                        //get the next lines
                        line = appendNameLines(line, reader);
                        //extra information not interesting to us
                        if (line.startsWith("see")) {
                            continue;
                        }
                        //Correct inconsistance in the line(s) giving information for the name
                        while (line.matches("\\([^\\)]+;[^\\(\\)]+\\).*")) {
                            line = line.replaceFirst(";", "");
                        }
                        if (line.matches("[^;]+\\):[^;]+;.*")) {
                            line = line.replaceFirst(":", ";");
                        }
                        if (line.contains(": masc")) {
                            line = line.replaceFirst(": masc", "; masc");
                        }
                        if (line.contains(": fem")) {
                            line = line.replaceFirst(": fem", "; fem");
                        }
                        line = line.replaceFirst("^[^;]*;", "");
                        if (line.contains("(") && !line.contains(")")) {
                            line = line.replaceFirst("\\([^;]*;", "");
                        }
                        
                        String line2 = "";
                        line = line.replaceAll("\\. masc", ".; masc");
                        line = line.replaceAll("\\. fem", ".; fem");
                        line = line.replaceAll("\\, masc", "; masc");
                        //Split the line
                        lineArray = line.split(";");
                        String lang = "_";
                        String gender = "_";
                        //the first point is usually language of the name (if there is any information)
                        if (lineArray.length > 0) {
                            lang = lineArray[0].trim();
                        }
                        //get the gender of the person
                        if (lineArray.length > 1) {
                            gender = lineArray[1].trim();
                        }
                        //but if the language is masc or fem, then there was no language of the name
                        if (lang.equals("masc.") || lang.equals("fem.")) {
                            gender = lang;
                            lang = "_";
                        }
                        //get next line
                        line = reader.readLine();
                        
                        proff = "";
                        time = "_";

                        //continue as long as the line does not start with << or >> which marks the end of the section for the name in question
                        //there are some exceptions
                        while (line != null && 
                                ((name.equals("Aššūr-bāni-apli_1") && !line.startsWith(">>K. Radner")) || 
                                (!name.equals("Aššūr-bāni-apli_1") && !line.startsWith("<<") && !line.startsWith(">>")) || 
                                (name.equals("Sīn-ahhē-erība_1") && !(line.startsWith("<<"))))) {
                            
                            nameAttributes = new ArrayList<>();
                            TreeMap<String, ArrayList<String>> contents = new TreeMap<>();
                            
                            //ignore empty lines
                            if (line.equals("")) {
                                line = reader.readLine();
                                continue;
                            }
                            //if line refers to several individuals continue until an individual is found or the section of the name ends
                            //or if the name is Bēl-iqbi_8 (the is a mistake in the text files
                            if (line.matches("[0-9]{1,2}\\.\\-[1-9].*") || 
                                    line.matches("[0-9]{1,2}\\. or [1-9].*") || 
                                    line.matches("[0-9]{1,2}\\. and [1-9].*") || 
                                    (name.startsWith("Bēl-iqbi_") && line.startsWith("8. "))) {
                                    line = reader.readLine();
                                    while (line != null && !line.matches("[0-9]{1,2}\\. .*") && !line.startsWith("<<") && !line.startsWith(">>")) {
                                        line = reader.readLine();
                                    }
                            }
                            else {
                                //There has to be other information before the line with the document name
                                //We want to get the profession/description and the dating of the individual
                                if (!line.startsWith("@@")) {
                                    //First do some cleaning of inconsistences
                                    //An individuals description line moslty end in '):', but there are exceptions
                                    if (line.endsWith(";")) {
                                        line = line.replaceAll(";", ":");
                                    }
                                    if (line.endsWith(")")) {
                                        line = line.replaceAll("\\)$", "):");
                                    }
                                    if (!line.contains(":")) {
                                        line2 = reader.readLine();
                                        while (line2 != null && !line2.isEmpty() && !line2.startsWith("@@") && !line.contains(":")) {
                                            line += " "+line2;
                                            line2 = reader.readLine();
                                        }
                                        
                                    }
                                    //Get the dating and profession/description of the individual
                                    time = "_";
                                    proff = line;
                                    if (line.contains("(") && line.matches(".*\\)[:,\\.].*")) {
                                        if (line.lastIndexOf(")") > line.lastIndexOf("(")) {
                                            time = line.substring(line.lastIndexOf("(")+1, line.lastIndexOf(")"));
                                        }
                                        proff = line.substring(0, line.lastIndexOf("(")-1);
                                    }

                                    else if (line.contains("(") && !line.contains("):")) {
                                        line += reader.readLine();
                                        if (line.contains(":") && (line.lastIndexOf(":") > line.lastIndexOf("("))) {
                                            time = line.substring(line.lastIndexOf("(")+1, line.lastIndexOf(":"));
                                        }
                                        else if (line.contains(")") && (line.lastIndexOf(")") > line.lastIndexOf("("))) {
                                            time = line.substring(line.lastIndexOf("(")+1, line.lastIndexOf(")"));
                                        }
                                        else {
                                            time = "_";
                                        }
                                        proff = line.substring(0, line.lastIndexOf("(")-1);
                                    }
                                    if (time.trim().isEmpty()) {
                                        time = "_";
                                    }
                                    if (line.matches(".*[0-9]{3,3}\\-[0-9]{3,3}.*") && time.equals("_")) {
                                        //get the year numbers from the line
                                        time = matchYears("([0-9]{3,4}\\-[0-9]{3,4})", line);
                                    }
                                    
                                    //if line starts with number + full stop (eg. 2.), there are several individuals with the name name
                                    if (line.matches("[0-9]{1,2}\\. .*") && !name.equals("Aššūr-bāni-apli_1") && !name.equals("Sīn-ahhē-erība_1")) {
                                        //and exception
                                        if (name.equals("Nabû-šarru-uṣur_1")) {
                                            while (!line.startsWith("2. ")) {
                                                //System.out.println(line);
                                                line = reader.readLine();
                                            }
                                        }
                                        //get the number from the beginning of the line and change the number at the end of the name
                                        name = name.replaceFirst("_[0-9]+$", "_"+line.substring(0, line.indexOf(".")));
                                        //the rest of the line is the profession/description of the individual
                                        proff = proff.substring(proff.indexOf(" ")+1);
                                    }
                                    
                                    if (proff.contains("Name given to Esarhaddon, son of Sennacherib") || proff.contains("an alternate name for Esarhaddon")) {
                                        name = "Aššūr-ahu-iddina_7";
                                        proff = names.get("Aššūr-ahu-iddina_7").get("info").get(3);
                                    }
                                    //save the information we have so far into the names-treemap
                                    if (!names.containsKey(name)) {
                                        contents = new TreeMap<>();
                                        nameAttributes.add(lang);
                                        nameAttributes.add(gender);
                                        nameAttributes.add(proff);
                                        nameAttributes.add(time);
                                        contents.put("info", nameAttributes);
                                        names.put(name, contents);
                                    }
                                }
                                
                                //if next line or the line after that start with @@ i.e. it is a document name
                                if (line2.startsWith("@@")) {
                                    line = line2;
                                    
                                }
                                //else check the line after that
                                else {
                                    String line3 = reader.readLine();
                                    if (line3.contains("<<")) {
                                        break;
                                    }
                                    else {
                                        if (line3.startsWith("@@")) {
                                            line = line3;
                                        }
                                    }
                                }
                                line2 = "";
                                //check the line and the following lines for document names 
                                line = findDocs(proff, line, reader, name);
                            }
                            //move on to next line
                        }
                    }
                }
            }
            
            catch (Exception e) {
                System.out.println("Reading names: "+e+"\t"+line+"\t"+name);
            }
            finally {
                reader.close();
            }

        }
    }
    
    //write all the information of each individual in names-treemap to the file 'individualsInTextiles'
    private static void writeNames() throws IOException {
        MyWriter writer = new MyWriter("Output/individualsInTextfiles");
        String name;
        ArrayList<String> toRemove = new ArrayList<>();
        //iterate through the individuals in names-treemap
        for (Map.Entry<String, TreeMap<String, ArrayList<String>>> entry : names.entrySet()) {
            name = entry.getKey();
            TreeMap<String, ArrayList<String>> content = entry.getValue();
            ArrayList<String> infos = content.get("info");
            ArrayList<String> textNames = new ArrayList<>();
            //ArrayList<String> text = new ArrayList<>();
            if (content.containsKey("texts")) {
                textNames = content.get("texts");
                //text = content.get("text");
                writer.write(name+"\n");
                writer.write("\t");
                //add the language of name, gender, profession/short description, and dating on one line
                for (String info : infos) {
                    writer.write(info+"\t");
                }
                writer.write("\n");
                //list all the documentNames where the individual's name is found in
                for (String doc : textNames) {
                    writer.write("\t"+doc+"\n");
                }
            }
            //if no documentNames were found for the the individual, add the name to list of names to removed from the names-treemap
            else {
                toRemove.add(name);
            }
        }
        writer.end();
        //remove the names without documentNames from the names-treemap
        for (String nameToRemove : toRemove) {
            names.remove(nameToRemove);
        }
    }
    
    
    //read in the treemap 'documentNames' the document names with their original lines
    //that were found with PNATextsExtractor.java
    private static void readBinaryFile() throws IOException {
        FileInputStream file = null;
        BufferedInputStream buffer = null;
        ObjectInputStream input = null;
        try {
            file = new FileInputStream("docs.bin");
            buffer = new BufferedInputStream(file);
            input = new ObjectInputStream(buffer);
            documentNames = (TreeMap<String, String>) input.readObject(); 
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
    
    //make the lines after the name into one long line, because sometimes the information looked for spans over several lines
    private static String appendNameLines(String line, BufferedReader reader) throws IOException {
        String line2 = reader.readLine();
        while (!line2.isEmpty()) {
            line += " "+line2;
            line2 = reader.readLine();
        }
        return line;
    }
    
    //if line contains (, but not ), read and append the next line
    //if line starts with @@, return line
    private static String appendLines(String line, BufferedReader reader) throws IOException {
        if (!line.startsWith("@@") && line.contains("(") && line.lastIndexOf("(") > line.lastIndexOf(")")) {
            line += " "+reader.readLine();
        }
        return line;
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
    
    //the names in the text files use ascii letters
    //convert the letters to unicode
    private static String convertName(String name) {
        String newName = "";
        //For each letter in the name
        for (int i=0; i<name.length(); i++) {
            String letter = String.valueOf(name.charAt(i));
            //if the letter is in the conversion list, use the unicode
            if (letters.containsKey(letter)) {
                newName += letters.get(letter);
            }
            //most ascii letters are a combination of two signs
            //check for those
            else if (i<name.length()-1 && letters.containsKey(letter+""+String.valueOf(name.charAt(i+1)))) {
                newName += letters.get(letter+""+String.valueOf(name.charAt(i+1)));
                i++;
            }
            //else use the letter at it is
            else {
                newName += letter;
            }
        }
        return newName;
    }
    
    //get the name of the document from the line starting with @@
    //and add it for the individual
    private static void getDocName(String line, String name) {
        String text;
        //first check againt the same list of not wanted documentNames as in PNATextExtractor.java
        ArrayList<String> docs;
        if (line.startsWith("@@AnSt") || 
            line.startsWith("@@Canon") || 
            line.startsWith("@@Eponym") || 
            line.startsWith("@@JNES 13") || 
            line.startsWith("@@JNES13") ||
            line.matches(".*Hunger \\(1968\\) 256.*") ||
            line.matches(".*Iraq 32 \\(1970\\) 175.*") ||
            line.startsWith("@@LET") ||
            line.startsWith("@@Onasch") ||
            line.startsWith("@@Prism") ||
            line.startsWith("@@Fuchs") ||
            line.matches(".*Luckenbill \\(?1924\\)?.*") ||
            line.startsWith("@@Luckenbill 30") ||
            line.matches(".*Frahm \\(?1997\\)?.*") ||
            line.matches("@@Borger \\(19[59]6\\) .*") ||
            line.startsWith("@@IIT") ||
            line.startsWith("@@RIM") ||
            line.matches("@@2 ?R .*") ||
            line.startsWith("@@ADD App") ||
            line.startsWith("@@Cyl. D+E") ||
            line.startsWith("@@Sg D+E") ||
            line.startsWith("@@KAH") ||
            line.startsWith("@@Tadmor") ||
            line.matches(".*Grayson \\(?1975\\)?.*") ||
            line.matches(".*Iraq 16 179.*") ||
            line.matches(".*Iraq 16 18[23].*") ||
            line.startsWith("@@Iraq 41 56") ||
            line.matches("@@AfO [34] .*") ||
            line.matches("@@A 0?117 .*") ||
            line.startsWith("@@As14616c") ||
            line.startsWith("@@Istanbul A") ||
            line.matches("@@KAV 0?1[012][: ].*") ||
            line.matches("@@KAV 0?21[: ].*") ||
            line.matches("@@KAV 9[: ].*") ||
            line.matches("@@CT [23]6 .*") ||
            line.startsWith("@@K 6109 ") ||
            line.startsWith("@@K 2411 ") ||
            line.startsWith("@@Sg D+E") ||
            line.startsWith("@@Bab. Chron.") ||
            line.startsWith("@@Najafehabad") ||
            line.startsWith("@@Levine")
            ) {
            return;
        }
        //get the document name from the dataset build with PNATextExtractor.java
        //using the whole line as the key
        text = documentNames.get(line);
        //if a document was found, add it to the individual in names-treemap
        if (text != null) {
            docs = new ArrayList<>();
            TreeMap<String, ArrayList<String>> contents = new TreeMap<>();
            if (names.containsKey(name)) {
                contents = names.get(name);
                if (contents.containsKey("texts")) {
                    docs = contents.get("texts");
                }
            }
            if (!docs.contains(text)) {
                docs.add(text);
            }
            contents.put("texts", docs);
            names.put(name, contents);
        }
    }
    
    
    //return a string depending on what was found in the original string with the regex pattern given in the call
    private static String getMatch(String pattern, String form) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(form);
        String match = "", code = "";
        if (matcher.find()) {
            match = matcher.group(1);
        }
        if (match.matches("[0-9]+")) {
            code = "number";
        }
        else if (match.matches("[0-9]+\\'")) {
            code = "number'";
        }
        else if (match.matches("[a-z]")) {
            code = "letter";
        }
        else if (match.matches("[a-z]\\'")) {
            code = "letter'";
        }
        return code;
    }
    
    //return the part of string that matches the regex pattern given in the call
    private static String matchYears(String pattern, String form) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(form);
        String match = "";
        if (matcher.find()) {
            match = matcher.group(1);
        }
        return match;
    }
    
    //Check the line and the following lines for document names
    private static String findDocs(String proff, String line, BufferedReader reader, String name) throws IOException {
        
        if (!line.startsWith("@@")) {
            line = reader.readLine();
        }
        //if the individual is described as eponymous ancestor, look for the next individula or name
        if (proff.contains("ponymous ancestor")) {
            while (line != null && !line.matches("[0-9]{1,2}\\.[ -].*") && !line.startsWith("<<") && !line.startsWith(">>")) {
                line = reader.readLine();
            }
            return line;
        }
        //if the description of the individual contains eponym, look for next paragraph or next name
        //an Eponym might have been mentioned in documentNames also for other reasons than dating (then usually in a paragraph on their own)
        if (proff.matches(".*[Ee]ponym.*")) {
            //line = reader.readLine();
            while (line != null && !line.equals("") && !line.startsWith("<<") && !line.startsWith(">>")) {
                if (line.startsWith("<<") || line.startsWith(">>")) {
                    return line;
                }
                line = reader.readLine();
            }
        }
        // go through the possible paragraphs starting with letters (a), numbers' (1'), or letters' (a')
        //while the line does not contain the next individual or the next name
        while (line != null && !line.matches("[0-9]{1,2}\\.[ -].*") && !line.startsWith("<<") && !line.startsWith(">>")) {
            //if line contains (, but not ), read and append the next line
            line = appendLines(line, reader);
            boolean found = false;
            //handle 1' and a
            if (line.matches("[0-9a-z]{1,2}\\'\\. .*") || line.matches("[a-z]\\. .*")) {
                found = false;
                //get the pattern at the beginning of line, either number', letter, or letter'
                String match = getMatch("^([0-9a-z\\']+)\\. ", line);
                //if the line indicates eponymous dating, continue until next number/letter of the same type is found or the next name starts
                if (line.contains("date formula") ||
                        line.contains("Dating") ||
                        line.contains("In dates") ||
                        (line.contains("Document") && line.contains("dated")) ||
                        line.contains("Eponym") ||
                        line.contains("eponym")) {
                    while (!line.startsWith("<<") && !line.startsWith(">>")) {
                        line = reader.readLine();
                        if (line.matches("[a-z]\\. .*")) {
                            found = true;
                            break;
                        }
                        if (line.matches("[0-9a-z\\']+\\. .*")) {
                            found = true;
                            //get the pattern at the beginning of line, either number', letter, or letter'
                            String match2 = getMatch("^([0-9a-z\\']+)\\. ", line);
                            //compare the patters found with getMatch()
                            if (match2.equals(match) || line.matches("[0-9]{1,2}\\.[ -].*")) {
                                break;
                            }  
                        }
                    }
                    if (line.startsWith("<<") || line.startsWith(">>")) {
                        return line;
                    }
                }
            }
            //if lines starts with @@, get the name of the document
            if (line.startsWith("@@")) {
                getDocName(line, name);
            }
            //if not the next name or next description was found, continue reading line
            //else the loop is finished, and line returned
            if (!found) {
                line = reader.readLine();
            }
        }
        return line;
    }
    
    //write the names-treemap to a binary file called textfiles.ser
    private static void writeInfoToBinaryFile() throws IOException {
        
        OutputStream file = null;
        OutputStream buffer = null;
        ObjectOutput output = null;
        try {
            file = new FileOutputStream("textfileNames.bin", true);
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
    
}
