package application;

import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        BTree partsTree = new BTree(4);
        String partsFile = "src/partfile.txt";
        insertAllKeyIntoTree(partsTree, partsFile);

        runUserProgram(partsTree);
    }

    private static void runUserProgram(BTree partsTree) {
        Boolean userQuits = false;
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        while(!userQuits) {
            System.out.println("Please enter a key to do one of the following: ");
            System.out.println("I: Insert a new part ID and description into tree");
            System.out.println("S: Search for the description of the specified part ID");
            System.out.println("D: Delete a key and description from the tree");
            System.out.println("M: Modify a description in the tree");
            System.out.println("SA: Save to file");
            System.out.println("H: Display the height of the tree");
            System.out.println("O: Display the whole tree");
            System.out.println("Q: Quit the program");

            try {
                String input = keyboard.readLine();
                System.out.println("**********");
                switch (input.toLowerCase()) {
                    //insert case
                    case "I":
                        System.out.println("Enter a part ID (example format: AAA-392)");
                        String key = keyboard.readLine();
                        System.out.println("Enter a description");
                        String value = keyboard.readLine();
                        try{
                            partsTree.insertEntry(key, value);
                            System.out.println(key + " and " + value + " were inserted into the tree");
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        break;
                    //search case
                    case "s":
                        System.out.println("Enter a part ID (example format: AAA-392)");
                        key = keyboard.readLine();
                        outputValue(key, partsTree.searchTree(key));

                        System.out.println("The next 10 part ID's and descriptions are: ");
                        partsTree.displayNextTenKeys(key);
                        break;
                    //delete case
                    case "d":
                        System.out.println("Enter a part ID (example format: AAA-392)");
                        key = keyboard.readLine();
                        try {
                            partsTree.deleteEntry(key);
                            System.out.println(key + " deleted successfully");
                        }
                        catch (Exception e) {
                            System.out.println("Part ID doesn't exist");
                        }
                        break;
                    //modify case
                    case "m":
                        System.out.println("Enter a part ID (example format: AAA-392)");
                        key = keyboard.readLine();
                        System.out.println("Enter a description");
                        value = keyboard.readLine();
                        partsTree.modifyEntry(key, value);
                        break;
                    case "h":
                        System.out.println("Height of the tree is " + partsTree.getTreeHeight());
                        break;
                    case "o":
                        partsTree.outputTree(partsTree.getRoot());
                        break;
                    case "q":
                        userQuits = true;
                        System.out.println("Quitting Program");
                        System.out.println("Tree Stats: ");
                        System.out.println("Leaf Node Splits: " + partsTree.getRoot().getLeafSplitCount());
                        System.out.println("Leaf Node Merges: " + partsTree.getRoot().getLeafMergeCount());
                        System.out.println("Internal Node Splits: " + partsTree.getRoot().getInternalSplitCount());
                        System.out.println("Internal Node Merges: " + partsTree.getRoot().getInternalMergeCount());
                        break;
                    case "sa":
                        saveToFile(partsTree);
                        break;
                    default:
                        System.out.println("Invalid command");
                        break;
                }
                if(!userQuits) {
                    System.out.println("**********");
                    System.out.println();
                    System.out.println("Press enter to continue");
                    keyboard.readLine();
                }
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private static void insertAllKeyIntoTree(BTree partsTree, String partsFile) {
        try {
            FileReader readFile = new FileReader(partsFile);
            BufferedReader br = new BufferedReader(readFile); //get contents from file
            String line;

            //if line isn't empty, split the string into an array and store them in an object
            while ((line = br.readLine()) != null) {
                String str = line;

                //id and description is split by 8 spaces in the file
                String[] strArray = str.split("        ",2);
                String id = strArray[0];
                String desc = strArray[1];

                partsTree.insertEntry(id, desc);
            }
            System.out.println("Part file read and inserted into tree");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void saveToFile(BTree partsTree) {
        ArrayList<ArrayList<String>> keysAndValues = partsTree.getAllKeysAndValues();

        String fileName = "src/partfilecopy.txt";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            String key;
            String value;
            for(int i = 0; i < keysAndValues.get(0).size(); i++){
                key = keysAndValues.get(0).get(i);
                value = keysAndValues.get(1).get(i);
                writer.write(key + "        " + value + "\r\n");
            }
            writer.close();
            System.out.println("Part ID's and descriptions saved to partfilecopy.txt");
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    private static void outputValue(String key, String value) {
        if (value != null) {
            System.out.println("The value of " + key + " is " + value);
        } else {
            System.out.println("Key is not in the array");
        }
    }
}
