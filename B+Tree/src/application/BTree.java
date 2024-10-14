package application;

import java.util.ArrayList;
import java.util.Arrays;

public class BTree {
    final private int order; //max degree
    private Node root;
    private Exception Exception;

    public BTree(int order) {
        this.order = order;
        this.root = new LeafNode(order);
    }

    public void insertEntry(String key, String value) {
        LeafNode leafNode = getLeafThatContainsKey(key);
        leafNode.addEntry(key, value);

        //check if the that LeafNode exceeds the maximum degree
        //and deal with it in dealWithOverflow()
        if(leafNode.exceedsMaximum()) {
            Node node = leafNode.dealWithOverflow(); //dealWithOverflow() returns a null if root doesn't need to change
            if(node != null) {
                this.root = node;
            }
        }
    }

    public String searchTree(String key) {
        LeafNode leaf = this.getLeafThatContainsKey(key);

        int index = leaf.search(key); //search(key) returns -1 if key doesn't exist.
        if(index >= 0) {
            return leaf.getValue(index);
        }
        return null;
    }

    public void deleteEntry(String key) throws Exception {
        LeafNode leafNode = getLeafThatContainsKey(key);
        //throw exception if key doesn't exist in tree
        if(leafNode.search(key) == -1) {
            throw Exception;
        }

        //deal with the underflow if the key was deleted, and the leaf goes below the minimum degree.
        if(leafNode.deleteEntry(key) && leafNode.isInsufficient()) {
            Node node = leafNode.dealWithUnderflow(key);
            if (node != null) {
                this.root = node;
            }
        }
    }

    //Change the value given a key
    public void modifyEntry(String key, String value) {
        LeafNode leaf = this.getLeafThatContainsKey(key);

        int index = leaf.search(key);
        try{
            leaf.setValue(value, index);
            System.out.println("Key modified successfully");
            System.out.println("The value of " +key+ " is now " + this.searchTree(key));
        }
        catch (Exception e) {
            System.out.println("Modify Error: Key not found in tree");
        }

    }

    //This method return a LeafNode within the B+ Tree that either contains the key
    //or should contain the specified key.
    private LeafNode getLeafThatContainsKey(String key) {
        Node node = this.root;

        //While the node is an instance of an InternalNode, keep looping until
        //the LeafNode that should contain the key is found.
        while(node instanceof InternalNode) {
            node = ((InternalNode)node).getChild(node.search(key));
        }
        return (LeafNode) node;
    }

    //output every single key/value pair in order
    public void outputTreeLeaves() {
        Node node = this.root;

        //traverse the tree to get to the 1st leaf node.
        while(node instanceof InternalNode) {
            node = ((InternalNode) node).getChild(0);
        }

        while(node != null) {
            System.out.print(Arrays.toString(node.getKeys()));
            node = node.getRightSibling();
        }
        System.out.println();
    }

    //display the whole tree
    public void outputTree(Node node) {
        if(node == this.root) {
            System.out.print("Root: ");
            node.displayKeys();
        }

        if(node instanceof InternalNode) {
            System.out.print("Parent: " + Arrays.toString(node.getKeys()) + " -> Children: ");
            for(int i = 0; i < node.getCurrentDegree()+1; i++) {
                System.out.print(Arrays.toString(((InternalNode) node).getChild(i).getKeys()) + " ");
            }
            System.out.println();
            for(int i = 0; i < node.getCurrentDegree()+1; i++) {
                if(((InternalNode) node).getChild(0) instanceof InternalNode) {
                    outputTree(((InternalNode) node).getChild(i));
                }
            }
        }
    }

    public int getTreeHeight() {
        Node node = this.root;

        int treeHeight = 1;
        while(node instanceof InternalNode) {
            node = ((InternalNode) node).getChild(0);
            treeHeight++;
        }

        return treeHeight;
    }

    public void displayNextTenKeys(String key) {
        String[] keys = new String[11];
        String[] values = new String[11];
        LeafNode leafNode = getLeafThatContainsKey(key);
        int arrayIndex = 0;
        int keyIndex = leafNode.getIndexOfKey(key);

        //Go through the leaf nodes to find the next 10 keys after the searched key.
        while(arrayIndex < 11) {
            keys[arrayIndex] = leafNode.getKey(keyIndex);
            values[arrayIndex] = leafNode.getValue(keyIndex);
            arrayIndex++;
            keyIndex++;

            //check if we've reached the end of the leaf node or there are no more keys in it.
            if(keyIndex >= leafNode.getMaxDegree() || leafNode.getKey(keyIndex) == null) {
                if(leafNode.getRightSibling() == null) {
                    //get out of the loop if there are no more right siblings
                    break;
                }
                else {
                    //set leafNode to the right siblings
                    leafNode = (LeafNode) leafNode.getRightSibling();
                    keyIndex = 0;
                }
            }
        }
        String[] keysArray = new String[10];
        String[] valuesArray = new String[10];

        //get rid of the first key in the keys/values array
        //since we only need to display the next 10 keys/values
        for(int i = 1; i < keys.length; i++) {
            keysArray[i-1] = keys[i];
            valuesArray[i-1] = values[i];
        }

        System.out.println(Arrays.toString(keys));
        System.out.println(Arrays.toString(values));
    }

    public ArrayList<ArrayList<String>> getAllKeysAndValues() {
        Node node = this.root;
        ArrayList<ArrayList<String>> keysAndValues = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        //traverse the tree to get to the 1st leaf node.
        while(node instanceof InternalNode) {
            node = ((InternalNode) node).getChild(0);
        }

        //Add key and value to arrayLists if the value is not null
        while(node != null) {
            for(int i = 0; i < node.getCurrentDegree(); i++) {
                if(node.getKey(i) != null) {
                    keys.add(node.getKey(i));
                    values.add(((LeafNode) node).getValue(i));
                }
            }
            node = node.getRightSibling();
        }

        //An array of ArrayList's
        keysAndValues.add(keys);
        keysAndValues.add(values);

        return keysAndValues;
    }

    public Node getRoot() {
        return this.root;
    }
}
