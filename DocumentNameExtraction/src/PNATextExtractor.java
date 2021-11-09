/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pnatextextractor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Heidi Jauhiainen, University of Helsinki
 
 A script for extracting the document names from the text files for printing the Prosopography of the Neo-Assyrian Empire volumes I/1- III/1.
 Only functions on the original files, not available publicly.
 */
public class PNATextExtractor {

    /**
     * @param args the command line arguments
     * 0 = folder where the PNA text files are
     * 1 = path and name of file with synonymous document names
     * 2 = path and name of file with concordance list
     * 3 = path and name of file with Oracc concordance list (made with OraccPublicationsExtractor.java)
     */
    //document names found in the first round
    private static ArrayList<String> easyNames;
    private static Boolean found;
    //synonymous document names from our own list
    private static TreeMap<String, String> synos;
    //synonymous names used for documents found in PNA
    private static TreeMap<String, String> usedDuplicates;
    //all synonymous document names found in the Oracc metadata
    private static TreeMap<String, String> oraccSynos;
    //synonymous names from our own list for Assxxxxx, StAT, and BATSH
    private static TreeMap<String, String> concords;
    //list of document names after the first round
    private static TreeMap<String, ArrayList<String>> texts;
    //all synonymous names from outside lists (from Oracc concordances only SAAo) and found in the same line
    private static TreeMap<String, String> allDuplicates;
    //synonymous names found in the same line in PNA
    private static TreeMap<String, String> onlineDuplicates;
    private static String duplicateOnLine;
    
    public static void main(String[] args) throws IOException {
        //folder where the PNA texts file are
        File folder = new File(args[0]);
        
        allDuplicates = new TreeMap<>();
        onlineDuplicates = new TreeMap<>();
        //read list of synonymous document names from our own list > synos
        readSynonyms(args[1]);
        
        //read our own concordance list (mostly Asxxxx, BATSH, StAT)
        concords = readConcordances(args[2]);
        
        //read Oracc concordance list > oraccSynos
        //only SAAo texts are added to allDuplicates
        readOraccConcordances(args[3]);

        //find self evident document names from lines that start with @@
        //saves the names in 'easyNames' and all lines 'texts'
        findTexts(folder);
        
        //get a list of thumbprints from the document names in 'easyNames' (i.e. the self evident names)
        TreeSet<String> thumbprints = getThumbprints();
        
        //get document names also for the rest of the documents in texts TreeMap
        TreeMap<String, String> documents = getDocumentNames(thumbprints);
        
        //add synonymous names found on the same line to all known duplicates
        checkOnlineDuplicates(documents, thumbprints);
        
        //write the TreeMap documents with information on textnames and their original lines to a binary file
        writeToBinaryFile(documents);

        //print found document names with the original line
        //used in PNANameExtractor.java
        printOriginals(documents);

        //print only the names of final texts
        //used in PNAPdfExtractor.java
        printTextnames(documents);
        
        //FOR EXTRA INFORMATION, UNCOMMENT THE FOLLOWING PRINT COMMANDS

        //print the names of documents in PNA with the duplicate name actually used in this data
        //the information for the duplicates comes from the concordance lists (Oracc and our own list)
        //printUsedDuplicates();

        //print the textnames changed to thumbprints
        //printThumbprints(thumbprints);

        //print a list of all duplicate names from the lists and found in PNA
        //from Oracc concordances only the SAAo texts
        //printAllDuplicates();
    }
    
    //Finds valid documents from lines that start with @@
    private static void findTexts(File folder) throws IOException {
        BufferedReader reader = null;
        easyNames = new ArrayList<>();
        String line = "";
        String origLine = "";
        
        texts = new TreeMap<>();
        for (File file : folder.listFiles()) {
            try {
                reader = new BufferedReader(new FileReader(file));
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("@@")) {
                        //@@ inside parenthesis are ignored
                        appendLines(line, reader);
                    }
                    else if (line.startsWith("@@")) {
                        //check if it is the kind of text we are not interested in
                        if (!checkText(line)) {
                            continue;
                        }
                        
                        //keep heed of the original form of the line (which is changed in this method)
                        origLine = line;

                        line = line.replaceAll(" +", " ");
                        
                        line = appendLines(line, reader);

                        //line = line.replaceAll("\\)[,.:;] ?$", "");
                        line = line.replaceAll("Fuchs \\{apud\\| ", "");
                        
                        duplicateOnLine = "";
                        String duplicateOnLine2 = "";
                        
                        //From lines with duplicates (indicated by '='), we sometimes want a specific one (e.g. Hunger), 
                        // but we want to know the keep information on the other duplicate as well
                        if (line.contains(" = ")) {
                            //System.out.println(line);
                            String[] lineArray = line.split(" = ");
                            
                            //if line contains Hunger, we want that name
                            if (!line.startsWith("@@Hunger") && line.contains(" = Hunger")) {
                                    line = lineArray[1].trim();
                                    if (!line.startsWith("Hunger")) {
                                        line = lineArray[2].trim();
                                    }
                                    duplicateOnLine = lineArray[0].trim();   
                            }
                            
                            //if line contains Borger (1996), we want that name
                            else if (line.contains(" = Borger (1996)")) {
                                for (int i=1; i<10; i++) {
                                    line = lineArray[i].trim();
                                    if (line.startsWith("Borger")) {
                                        break;
                                    }
                                }
                                duplicateOnLine = lineArray[0].trim();
                            }
                            //we want Frahm that we know is as third name on line
                            else if (line.matches(".* = .* = .*") && (line.contains(" = Frahm"))) {
                                line = lineArray[2].trim();
                                duplicateOnLine = lineArray[0].trim();
                                duplicateOnLine2 = lineArray[1].trim();
                            } 
                            else if (line.startsWith("@@Abr-Nahrain") || 
                                    line.contains(" = RIM") ||
                                    line.contains(" = Frahm") ||
                                    (line.contains(" = Hug") && !lineArray[0].trim().contains("@@THU1:3")) ) {
                                line = lineArray[1].trim();
                                duplicateOnLine = lineArray[0].trim();
                            }
                            //else take the first name and save the second (and third if present)
                            
                            else if (!line.contains("= Hunger") && line.contains("Hunger (1968)")) {
                                duplicateOnLine = line.substring(0, line.indexOf("Hunger")-2);
                                line = line.substring(line.indexOf("Hunger"));
                            }
                            else {
                                line = lineArray[0].trim();
                                if (lineArray.length > 1) {
                                    //System.out.println(lineArray[1]);
                                    duplicateOnLine = lineArray[1].trim();
                                }
                                if (lineArray.length == 3) {
                                    duplicateOnLine2 = lineArray[2].trim();
                                }
                            }
                        }
                        

                        line = line.replaceAll("^@@", "");
                        line = line.replaceAll(" @@.*", "");
                        
                        //if line is part of a line with '=' it might be a doc that we do not want
                        if (line.startsWith("Fuchs ") ||
                            line.startsWith("Borger (1996)") ||
                            line.startsWith("RIM") ||
                            line.contains("Prism")) {
                            continue;
                        }

                        //if not the document name contains a comma (as in 89-4-26,209:13),
                        //we can split from the comma and take the first half
                        if (!line.matches("[1-9][0-9]\\-.*")) {
                            line = line.split(",")[0];
                        }
                        
                        //in a few cases the end of the document name is marked with //
                        if (line.contains("//")) {
                            //System.out.println(line);
                            if (line.contains(" //")) {
                                line = line.split(" \\/\\/")[0];
                            }
                            else {
                                line = line.split("\\/\\/")[0];
                            }
                        }
                        
                        //Correct mistakes and inconsistences in PNA data in order to find names that refer to same document
                        if (line.startsWith("0 ")) {
                            line = line.replaceAll("^0 ", "O ");
                        }

                        if (line.startsWith("SAAB")) {
                            line = line.replaceFirst("Appendix ", "A");
                            line = line.replaceFirst("App.", "A");
                            line = line.replaceFirst("no\\. ", "");
                        }
                        
                        if (line.startsWith("Hunger") && line.contains("(text ")) {
                            String match = getMatch("\\(text ([A-Z])", line);
                            String match2 = getMatch("1968\\) ([0-9]{3,3})", line);
                            line = line.replaceFirst(match2, match2+""+match);
                        }
                        if (line.startsWith("Orient")) {
                            String match = getMatch("1993\\) (.* [1-9]\\.)", line);
                            line = line.replaceFirst(match, match.substring(match.length()-2));
                        }

                        //Remove extra information from the end of the line
                        //and clean out somethings
                        //also if it is clear that the document name has been found, mark the line with 'found' = true;
                        found = false; 
                        line = getShorterLine(line);

                        //Correct mistakes and inconsistences
                        line = correctMistakes(line);
                        if (line.equals("")) {
                            continue;
                        }

                        // dealing with the other name on the same line if there was one (e.g. lines with '=')
                        if (!duplicateOnLine.equals("")) {
                            if (duplicateOnLine.contains("=")) {
                                String[] lineArray = duplicateOnLine.split("=");
                                duplicateOnLine = lineArray[0];
                                duplicateOnLine2 = lineArray[1];
                            }
                            handleOnlineDuplicate(duplicateOnLine, origLine);
                        }
                        if (!duplicateOnLine2.equals("")) {
                            handleOnlineDuplicate(duplicateOnLine2, origLine);
                        }

                        //if the document name on line was self evident, we save it in easyNames Arraylist
                        if (found) {
                            if (!easyNames.contains(line)) {
                                easyNames.add(line);
                            }
                        }
                        //all the found documents are saved in 'texts'-map with all original lines it appears in
                        //at this point, there might still be several versions of the same document
                        ArrayList<String> originals = new ArrayList<>();
                        if (texts.containsKey(line)) {
                            originals = texts.get(line);
                        }
                        originals.add(origLine);
                        texts.put(line, originals);
                    }
                }
            }

            catch (Exception e) {
                System.out.println("Reading texts: "+e+"\t"+line+"\t"+origLine);
            }
            finally {
                reader.close();
            }
        }
    }
    
    //Not interested in Eponymous lists, Royal annals and such texts that do not tell about people who might actually have known each other
    private static Boolean checkText(String line) {
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
            line.matches(".*Frahm \\(?1997\\)?.'") ||
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
            return false;
        }
        return true;
    }
    
    //Correct mistakes and inconsistences in PNA data in order to find names that refer to same document
    private static String correctMistakes(String line) {
        String match;
        if (line.startsWith("AAA 20")) {
            match = getMatch("AAA 20 (.*)", line);
            line = line.replace(" "+match, "");
        }
        if (line.startsWith("As ")) {
            //String[] lineArray = line.split(" ");
            /*if ((lineArray[0]+" "+lineArray[1]).length() == 7) {
                line = line.replaceFirst("^As ", "As0");
            }
            else {*/
                line = line.replaceFirst("^As ", "As");
            //}
        }
        if (line.startsWith("As0")) {
            line = line.replaceFirst("^As0", "As");
        }
        if (line.startsWith("Ass ")) {
            if (line.matches("Ass [1-9]\\/.*")) {
                line = line.replaceAll("^Ass ", "Assur ");
            }
            else {
                line = line.replaceAll("^Ass ", "As");
            }
        }
        if (line.matches("Ass[ur]{0,2}[1-9].*")) {
            line = line.replaceAll("^Ass[ur]{0,2}", "Assur ");
        }
        if (line.startsWith("BagM")) {
            line = line.replaceFirst("BagM", "BaM");
        }
        if (line.startsWith("BaM")) {
            //System.out.println("täällä\t"+line);
            line = line.replaceFirst("BaM ", "BaM");
            line = line.replaceFirst("24 11", "24 L11");
            line = line.replaceFirst("24 L0?", "24 ");
            line = line.replaceFirst("Taf\\. ", "");
            //System.out.println("\t"+line);
        }
        if (line.startsWith("Bagh. Mitt. 5")) {
            line = "BaM5 275";
        }
        if (line.startsWith("BATSH")) {
            line = line.replaceFirst("BATSHT", "BATSH");
            line = line.replaceFirst("BATSH 89", "BATSH 6 89");
            if (!line.matches("BATSH [1-9] [1-9].*")) {
                line = "";
            }
        }
        if (line.startsWith("Borger")) {
            line = line.replaceFirst("\\. ", ".");
            line = line.replace("AssA", "Ass.A");
        }
        if (line.startsWith("CTN 2 ")) {
            duplicateOnLine = line;
            line = line.replaceFirst("CTN 2", "GPA");
        }
        line = line.replaceFirst("GPA pl\\.[1-9][1-9] no\\.", "GPA");

        if (line.startsWith("G�ttesbrief")) {
            line = line.replaceFirst("G�ttesbrief", "Gottesbrief");
        }
        if (line.startsWith("IAsb")) {
            line = line.substring(0, 10);
        }
        if (line.startsWith("Iraq")) {
            if (line.matches("Iraq ?16 .*") || line.matches("Iraq 32 174.*") || line.matches("Iraq 32 1970.*")) {
                line = "";
            }
            if (line.matches("Iraq[0-9].*")) {
                match = getMatch("Iraq([0-9][0-9] [0-9][0-9][0-9a-z])", line);
                line = line.replaceFirst(match, " "+match);
                line = line.substring(0, "Iraq ".length()+match.length());
            }
        }
        if (line.startsWith("Hug ")) {
            line = line.replaceFirst(" 1993", "");
            if (line.matches("Hug [0-9\\-]+[:f]* .*")) {
                match = getMatch("Hug( [0-9\\-]+f* )", line);
                //System.out.println(match+"\t"+line);
                line = line.replace(match, " ");
            }
            if (line.matches("Hug \\p{L}+ [0-9A-Z]")) {
                match = getMatch("Hug \\p{L}+ ([0-9A-Z])", line);
                line = line.replace(" "+match, match);
            }
        }
        if (line.startsWith("BBSt")) {
            line = line.replaceFirst(" p\\. [0-9]+ ", " ");
        }

        if (line.startsWith("AAA") || line.startsWith("BBSt")) {
            line = line.replaceFirst(" no\\. ", " ");
        }
        if (line.contains("App")) {
            line = line.replaceFirst("App\\.? ?", "App ");
        }
        if (line.startsWith("K. ")) {
            line = line.replaceFirst("^K. ", "K ");
        }
        if (line.startsWith("Ladders")) {
            line = line.substring(0,13);
        }
        if (line.startsWith("Grayson 1981") || line.startsWith("Ladders")) {
            if (line.startsWith("Ladders")) {
                duplicateOnLine = line;
            }
            line = "Grayson 1981 84";
        }
        if (line.startsWith("Landsberger") || line.startsWith("TKSM")) {
            if (line.startsWith("TKSM")) {
                duplicateOnLine = line;
            }
            line = "Landsberger 1965";
        }
        if (line.startsWith("NALK")) {
            line = "NALK App I";
        }
        if (line.matches("Ot ?29 .*")) {
            line = line.replaceFirst("Ot ?29", "Orient 29 1993");
            line = line.replaceAll("0", "");
        }
        if (line.startsWith("Orient") && line.matches(".*[0-9]\\.[0-9]+ [0-9]+")) {
            match = getMatch("([0-9]\\.[0-9]+ [0-9]+$)", line);
            line = line.replaceFirst(match, match.substring(0, match.lastIndexOf(" ")));
        }
        if (line.startsWith("RA ")) {
            line = line.replaceFirst("\\.", " ");
        }
        if (line.startsWith("RE")) {
            line = line.replaceFirst("no\\. ", "");
        }
        if (line.startsWith("Rb ")) {
            line = line.replaceFirst("A ", "A");
        }
        if (line.startsWith("Hunger 1968")) {
            line = line.replaceFirst("no\\. ", "");
            line = line.replaceFirst(" [0-9]{0,2} ", " ");

        }
        if (line.startsWith("Sem")) {
            line = line.replaceFirst(" 1996", "");
            if (line.startsWith("Sem ")) {
                line = line.replaceFirst("Sem", "Semitica");
            }
        }
        if (line.startsWith("Sumer30")) {
            line = line.replaceFirst("Sumer30", "Sumer 30");
        }
        if (line.contains("Hadid")) {
            if (line.startsWith("Tel")) {
                line = line.replaceFirst("Tel", "T.");
            }
            if (!line.startsWith("T. Hadid 2")) {
                line = line.replaceFirst("T. Hadid", "T. Hadid 1");
            }
        }
        if (line.startsWith("Trade1")) {
            line = line.replaceFirst("Trade1", "Trade 1");
        }
        if (line.startsWith("TIM")) {
            if (!line.matches("TIM [0-9]{1,2} [0-9].*")) {
                line = "";
            }
        }
        if (line.startsWith("W. ")) {
            line = line.replaceFirst("W. ", "W ");
        }
        if (line.startsWith("CT ")) {
            line = line.replaceFirst("A", "");
            if (line.matches("CT [0-9] [0-9] .*")) {
                match = getMatch("CT ([0-9] [0-9]) ", line);
                line = "CT "+match;
            }
        }
        if (line.startsWith("CTN ")) {
            line = line.replaceFirst("A", "");
        }
        if (line.matches("[1-9] [A-Z].*")) {
            line = line.replaceFirst(" ", "");
        }
        if (line.startsWith("GPA")) {
            line = line.replaceAll("99[A-B]", "99");
        }
        if (line.startsWith("JCS 39 159")) {
            line = "JCS 39 159";
        }
        if (line.startsWith("McEwan")) {
            line = "McEwan 1983";
        }
        if (line.startsWith("ND ")) {
            if (line.contains("3466") || line.contains("5475")) {
                line = "";
            }
            //remove e.g. B from ND 3488B but not from ND 3479b (different date than ND 3479)
            if (line.matches("ND [0-9]+[A-Z].*")) {
                match = getMatch("ND ([0-9]+[A-Z])", line);
                line = line.replaceFirst(match, match.substring(0, match.length()-1));
            }
        }
        if (line.matches("O [0-9]+[a-z].*")) {
            match = getMatch("O ([0-9]+[a-z])", line);
            line = line.replaceFirst(match, match.substring(0, match.length()-1));
        }
        if (line.startsWith("TB ")) {
            if (line.matches("TB [0-9]+[a-z].*")) {
                match = getMatch("TB ([0-9]+[a-z])", line);
                line = line.replaceFirst(match, match.substring(0, match.length()-1));
            }
        }
        if (line.matches("ZA 73 [0-9]+ no\\. .*")) {
            match = getMatch("73( [0-9]+ no\\.)", line);
            line = line.replaceFirst(match, "");
        }
        if (line.startsWith("KAH 116")) {
            line = line.replaceFirst("KAH", "KAH 2");
        }
        if (line.startsWith("SAAB")) {
            line = line.replaceFirst(" p\\.", "");
            if (line.matches("SAAB [0-9]+ A?[0-9]+[A-Ba-z].*")) {
                match = getMatch("SAAB [0-9]+ A?([0-9]+[A-Ba-z]).*", line);
                line = line.split(match)[0]+""+match.substring(0, match.length()-1);
            }
        }
        if (line.startsWith("DeZ no")) {
            line = "";
        }
        if (line.startsWith("Trade 1998")) {
            line = "Trade 2";
        }
        if (line.startsWith("SAA 2 6")) {
            line = "SAA 2 6";
        }
        if (line.matches("BT [0-9]{3,3}a.*")) {
            line = line.substring(0, line.indexOf("a"));
        }
        if (line.startsWith("StAt")) {
            line = line.replaceFirst("StAt", "StAT");
        }
        return line;
    }
    
    //add synonymous names found of the line to 'onlineDuplicates' list
    //first do some cleaning
    private static void handleOnlineDuplicate(String duplicateOnLine, String origLine) {
        Boolean usedBefore = found;
        duplicateOnLine = duplicateOnLine.replaceFirst("@@", "");
        duplicateOnLine = duplicateOnLine.split("\\/\\/")[0];
        duplicateOnLine = duplicateOnLine.replaceAll(" 0+", " ");
        duplicateOnLine = duplicateOnLine.split(",")[0];
        duplicateOnLine = getShorterLine(duplicateOnLine);
        if (!duplicateOnLine.equals("")) {
            onlineDuplicates.put(origLine, duplicateOnLine);
        }
        found = usedBefore;
    }
    
    //make a list of thumbsprints of the document names that were easy to recognise
    //thumbprint for SAA 14 25 is SAA_11_11
    //the thumbprints are used for finding the document name from the lines where it is less easy
    //the thumbprint tells about the possible forms and lengths of the name with the specific letter compination
    private static TreeSet<String> getThumbprints() {
        TreeSet<String> thumbprints = new TreeSet<>();
        String formula;
        for (String form : easyNames) {
            formula = makeThumbprints(form);
            boolean removeFormul = false, shorter = false;
            String formulToRemove = "";
            for (String formul : thumbprints) {
                //if thumbprints already contains a form of the name that is longer
                //mark that as to be removed
                if (formul.matches(formula+"[^_]+_")) {
                    formulToRemove = formul;
                    removeFormul = true;
                    break;
                }
                //if thumbprints contains form that is shorter
                else if (formula.matches(formul+"[^_]+_")) {

                    shorter = true;
                    break;
                }

            }
            if (removeFormul) {
                thumbprints.remove(formulToRemove);
            }
            //if thumbprints already contains a shorter form, no need to add
            if (!thumbprints.contains(formula) && shorter == false) {
                thumbprints.add(formula);
            }
        }
        return thumbprints;
    }
    
    //iterate throught the document names
    //there may be several versions of the name as only the obvious ones have been shortened to the actual name part
    //use the thumbprints to
    private static TreeMap<String, String> getDocumentNames(TreeSet<String> thumbprints) {
        TreeMap<String, String> docs = new TreeMap<>();
        for (Map.Entry<String, ArrayList<String>> entry : texts.entrySet()) {       
            String text = entry.getKey();
            //check if there is a preferred duplicate name
            if (allDuplicates.containsKey(text)) {
                text = allDuplicates.get(text);
            }
            if (text.equals("")) {
                continue;
            }
            String thumbprint = makeThumbprints(text);
            
            //some texts require manual rules to get the proper names
            if (text.startsWith("TCL 9")) {
                if (text.lastIndexOf(" ") > 5) {
                    String match = getMatch("9 ([0-9][0-9][A-Z]?)", text);
                    text = "TCL 9 "+match;
                }
            }
            else if (text.startsWith("TIM 11 ")) {
                String match = getMatch(" 11 ([0-9][0-9]?)", text);
                text = "TIM 11 "+match;
            }
            else if (text.startsWith("AfO") && !text.matches("AfO 8 .*") && !text.matches("AfO 32 .*")) {
                if (text.matches("AfO [0-9]+ [0-9t].*")) {
                    String match = getMatch("AfO [0-9]+( [0-9t].*)", text); 
                    text = text.substring(0, text.lastIndexOf(match));
                }
                if (text.contains("A9")) {
                    text = text.substring(0, text.lastIndexOf("9")+1);
                }
            }
            else if (text.startsWith("Asb 21") || text.startsWith("Asb 22")) {
                text = "Asb 216+222";
            }
            //...the others are checked against the thumbprints
            else {
                String text2 = checkThumbprints(text, thumbprint, thumbprints);
                if (text2.length() < text.length()) {
                    text = text2;
                }
            }
            //remove the letters after the document name number + all text after it
            if (text.matches(".*[0-9]+[a-zA-Z\\+]+ .*")) {
                String match = getMatch(" [0-9]+([a-zA-Z]+ .*)", text);
                String text2 = text.split(match)[0];
                thumbprint = makeThumbprints(text2);
                if (thumbprints.contains(thumbprint)) {
                    text = text.substring(0, thumbprint.length()-1+match.split(" ")[0].length());
                }

            }
            //Always use only first part of a multidocument text name as that is sometimes used in PNA
            if (text.matches(".*[0-9]+\\+.*")) {
                text = text.split("\\+")[0];
            }
            //remove commas from all documents, even 82-3-23,35:5
            //until now it has kept the document name intact (82-3-23 35)
            //but oracc duplicates do not have the comma
            text = text.replaceAll(",", " ");
            
            String origText = text;
            //check our own list of synonyms
            //if found there, add the "new name" to usedDuplicates with the old one
            if (synos.containsKey(text)) {
                String syno = synos.get(text);
                if (!usedDuplicates.containsKey(text)) {
                    usedDuplicates.put(text, syno);
                }
                text = syno;
            }
            
            String alternative = "";
            // check the BATSH-StAT-concordance list and use the designation given there
            //if found there, add the "new name" to usedDuplicates with the old one
            if (concords.containsKey(text)) { 
                alternative = concords.get(text);
                if (!usedDuplicates.containsKey(text)) {
                    usedDuplicates.put(text, alternative);
                }
                text = alternative;
            }
            
            //Check the Oracc duplicate-list and use the main designation found there
            //if found there, add the "new name" to usedDuplicates with the old one
            if (oraccSynos.containsKey(text)) {
                alternative = oraccSynos.get(text);
                if (!usedDuplicates.containsKey(alternative)) {
                    usedDuplicates.put(text, alternative);
                    text = alternative;
                }
            }
            if (!alternative.isEmpty()) {
                text = alternative;
            }

            //get all the lines where the texts was found in
            //add them to 'docs' with the "new name" of the document
            ArrayList<String> originals = entry.getValue();
            for (String orig : originals) {
                docs.put(orig, text);
            }
            //if a duplicate name was found in previous steps
            //add the older name saved in origText to duplicates
            if (!text.equals(origText)) {
                addToAllDuplicates(origText, text);
            }
        }
        return docs;
    }
    
    //add synonymous names found on the same line to all known duplicates list
    private static void checkOnlineDuplicates(TreeMap<String, String> readied, TreeSet<String> formulas) {
        for (Map.Entry<String, String> entry : onlineDuplicates.entrySet()) {
            String endDoc = readied.get(entry.getKey());
            String doc = entry.getValue();
            //check thumbprints and get the shortest fitting one
            String text = checkThumbprints(doc, makeThumbprints(doc), formulas);
            addToAllDuplicates(text, endDoc);
        }
    }
    
    //check the thumbprint of a text against thumbprints of the easily found document names
    //if found, shorten the text to the length of that thumbprint
    private static String checkThumbprints(String text, String thumbprint, TreeSet<String> thumbprints) {
        for (String formul : thumbprints) {
            if (thumbprint.matches(formul+"[^ ]+_")) {
                text = text.substring(0, formul.length()-1);
                break;
            }
        }
        return text;
    }
    
    //print the textnames changed to thumbprints
    private static void printThumbprints(TreeSet<String> thumbprints) throws IOException {
        MyWriter writer = new MyWriter("Output/thumbprints");
        for (String thumbprint : thumbprints) {
            writer.write(thumbprint);
        }
        writer.end();
    }
    
    //print found textnames with the original line
    private static void printOriginals(TreeMap<String, String> documents) throws IOException {
        MyWriter writer = new MyWriter("Output/textsWithOriginals");
        for (Map.Entry<String, String> entry : documents.entrySet()) {
            writer.write(entry.getValue()+"\t"+entry.getKey());
        }
        writer.end();
    }
    
    //print the names of documents in PNA with the duplicate name actually used in this data
    //the information for the duplicates comes from the concordance lists (Oracc and our own list)
    private static void printUsedDuplicates() throws IOException {
        MyWriter writer = new MyWriter("Output/usedDuplicates");
        for (Map.Entry<String, String> entry : usedDuplicates.entrySet()) {
            writer.write(entry.getKey()+"\t"+entry.getValue());
        }
        writer.end();
    }
    
    //print a list of document names found in PNA text files
    //the name might be a synonym for the ones in the actual text
    private static void printTextnames(TreeMap<String, String> documents) throws IOException {
        MyWriter writer = new MyWriter("Output/docsInTextfiles");
        TreeSet<String> textnames = new TreeSet<>();
        for (Map.Entry<String, String> entry : documents.entrySet()) {
            textnames.add(entry.getValue());
            //System.out.println(entry.getValue());
        }
        for (String name : textnames) {
            writer.write(name);
        }
        writer.end();
    }
    
    //print a list of all duplicate names from the lists and found in PNA
    //from Oracc concordances only the SAAo texts
    private static void printAllDuplicates() throws IOException {
        MyWriter writer = new MyWriter("Output/allDuplicates");
        for (Map.Entry<String, String> entry : allDuplicates.entrySet()) {
            writer.write(entry.getKey()+"\t"+entry.getValue());
        }
        writer.end();
    }
    
    //Append lines to longer string if they contain '(' but not ')'
    //@@ inside parenthesis are ignored
    private static String appendLines(String line, BufferedReader reader) throws IOException {
        if (line.contains("(") && line.lastIndexOf("(") > line.lastIndexOf(")")) {
            line += " "+reader.readLine();
            while (line.contains("(") && line.lastIndexOf("(") > line.lastIndexOf(")")) {
                line += " "+reader.readLine();
            }
        }
        return line;
    }
    
    //make thumbsprints of the document names that were easy to recognise
    //thumbprint for SAA 14 25 is SAA_11_11
    //'+' signs are replaced with X and letters after numbers with x
    private static String makeThumbprints(String word) {
        String match;
        String pattern = "[0-9](-[0-9]+)";
        String form = word.trim().replaceAll("[0-9]", "1");
        form = form.replaceAll(" ", "_");
        form = form+"_";
        form = form.replaceAll("\\+", "X");
        pattern = ".*1([a-zA-Z\\+]+)_.*";
        match = getMatch(pattern, form);
        
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
        return form;
    }
    
    //return the part of string that matches the regex pattern given in the call
    private static String getMatch(String pattern, String form) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(form);
        String match = "";
        if (matcher.find()) {
            match = matcher.group(1);
        }
        return match;
    }
    
    //Remove extra information from the end of the line
    //and clean out somethings
    //also if it is clear that the document name has been found, mark the line with 'found' = true;
    private static String getShorterLine(String line) {
         //remove everything from the end of line after last number + white space
        if (line.matches("^.* ?[0-9]+.*")) {
            if (line.matches(".*[0-9] .*[^\\d]*$")) {
                line = line.replaceAll(" [^\\df]*$", "");
            }
        }
        //remove extra spaces
        line = line.replaceAll(" +", " ");
        
        //First get the easily recognizable document names and mark those as 'found'
        //things that tell that here starts the line number
        //e.g. the name of the document is before this point
        
        //e.g. R123
        if (line.matches(".*[0-9]+.*[RSE][0-9]{2,}.*")) {
            line = line.replaceAll("[RSE][0-9]{3,}.*", "");
            line = line.replaceAll("[RSE][0-9]{2,}.*", "");
            found = true;
        }
        //e.g. ': 123' or ', 123'
        if (line.contains(":")) {
            if (line.contains("Hug (1993) 24:")) {
                line = line.replaceFirst(":", "");
            }
            line = line.split(":")[0];
            found = true;
        }
        //e.g. 'r. 7' or 'v 61'
        if (line.matches(".*[0-9][a-z]*[\\*\\.]* [rvl]+\\.?.*")) {
            line = line.split(" [rvl]")[0];
            found = true;
        }
        //e.g. 'ii 25'
        if (line.matches(".* [ivx]+ .*")) {
            line = line.split(" [ivx]")[0];
            found = true;
        }
        //Whether an actual document name has been found or not
        //trim the line from extra information
        line = line.trim();
        //some trailing texts remain dispite the previous efforts
        if (line.matches("[a-zA-Z\\d]+ \\d+.*")) {
           line = line.replaceAll(" [^\\df\\+\\?]+$", "");
        }
        //Taking out explanatory text from the end of the document name
        //will take out also numbers but only after a 10 letter or longer string of words
        if (line.matches(".*[0-9]+.*[a-zA-Z ]{10,}.*") && !line.contains("Reliefbeischrift") && !line.contains("Annalen")) {
            line = line.split("[a-zA-Z ]{10,}")[0];
        }
        //Pl. > pl.
        line = line.replaceAll("[Pp]l\\. ?", "pl.");
        //remove parentheses, brackets, *, ?
        line = line.replaceAll("[\\(\\)\\[\\]\\*\\{\\?]", "");
        //remove from end of line all character that are not letters or numbers
        line = line.replaceAll("[^\\p{L}0-9\\?]+$", "");
        //remove +-signs since the use of them is not consistent
        line = line.replaceAll("\\+ ", " ");
        //remove zeros from beginning of a number
        line = line.replaceAll(" 0{1,3}", " ");
        //There are a few cases with a '~' after the document number
        line = line.replaceAll("[\\$~]", "");
        //remove 'f' from the end of the name as the use of it is not consistent
        line = line.replaceAll("f+$", "");
        return line;
    }
    
    //read our own list of synonymous document names to synos
    private static void readSynonyms(String filename) throws FileNotFoundException, IOException {
        synos = new TreeMap<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("==")) {
                    String[] lineArray = line.split(" == ");
                    if (!synos.containsKey(lineArray[0])) {
                        synos.put(lineArray[0], lineArray[1]);
                    }
                    addToAllDuplicates(lineArray[0], lineArray[1]);
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
    
    //add document names to a list of all known duplicates
    private static Boolean addToAllDuplicates(String doc1, String doc2) {
        //check again if the new name is in our list of not wanted documents
        if (!checkText("@@"+doc1) || !checkText("@@"+doc2)) {
            return false;
        }
        doc1 = doc1.trim();
        doc2 = doc2.trim();
        if (!allDuplicates.containsKey(doc1)) {
            if (!allDuplicates.containsKey(doc2)) {
                allDuplicates.put(doc1, doc2);
                return true;
            }
        }
        return false;
    }
    
    
    
    //read our own list of synonymous names for BATCH, StAT and Assxxxx
    private static TreeMap<String, String> readConcordances(String filename) throws IOException {
        TreeMap<String, String> list = new TreeMap<>();
        BufferedReader reader = null;
        String line = "", key="", other="";
        String[] lineArray;
        try {
            reader = new BufferedReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                lineArray = line.split(";");
                key = lineArray[0].trim();
                for (int i=1; i<lineArray.length; i++) {
                    other = lineArray[i].trim();
                    other = other.replaceFirst("As0", "As");
                    list.put(other, key);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            reader.close();
        }
        allDuplicates = list;
        return list;
    }
    
    //read Oracc concordances to oraccSynos
    //SAAo concordance are also added to allDuplicates
    private static void readOraccConcordances(String filename) throws IOException {
        BufferedReader reader = null;
        String line = "", key, other;
        oraccSynos = new TreeMap<>();
        usedDuplicates = new TreeMap<>();
        String[] lineArray;
        try {
            reader = new BufferedReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                lineArray = line.split(";");
                key = lineArray[1].trim();
                for (int i=2; i<lineArray.length; i++) {
                    other = lineArray[i].trim();
                    if (!other.equals(key) && !other.equals("_")) {
                        oraccSynos.put(other, key);
                        if (line.startsWith("saao")) {
                            allDuplicates.put(other, key);
                        }
                    }
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

    //write the found document names (with whole lines they were found in) to a binary file
    //the binary file is used in PNANameExtractor
    private static void writeToBinaryFile(TreeMap<String, String> documents) throws FileNotFoundException, IOException {
        OutputStream file = null;
        OutputStream buffer = null;
        ObjectOutput output = null;
        try {
            file = new FileOutputStream("texts.ser");
            buffer = new BufferedOutputStream(file);
            output = new ObjectOutputStream(buffer);
            output.writeObject(documents);
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
