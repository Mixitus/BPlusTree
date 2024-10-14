package application;

import java.util.Arrays;

public class LeafNode extends Node {
    private String[] values;

    public LeafNode(int maxDegree) {
        this.keys = new String[maxDegree];
        this.values = new String[maxDegree];
        this.maxDegree = maxDegree;
        this.minDegree = (maxDegree/2);
        this.currentDegree = 0;
    }

    //return the index of where key is located. Used for insertion and deletion.
    @Override
    protected int search(String key) {
        int index = 0;
        int compareKeys = key.compareTo(this.getKey(index));
        while(compareKeys >= 0) {
            if(compareKeys == 0) {
                return index;
            }
            index++;
            //break the loop if we're at the end of the array
            if(index == this.getCurrentDegree()) {
                break;
            }
            compareKeys = key.compareTo(this.getKey(index));
        }
        return -1;
    }



    /**
     * Methods for insertion:
     */

    //Finds the index where the key and value pair should be inserted.
    public void addEntry(String key, String value) {

        //Find where the new entry should be added by comparing keys
        int index = 0;
        for(int i=0; i<this.getCurrentDegree(); i++) {
            if(key.compareTo(getKey(i)) >= 0) {
                index++;
            }
        }
        insertAtIndex(key, value, index);
    }

    //called by addEntry and adds a key and value at the specified index
    private void insertAtIndex(String key, String value, int index) {
        String[] keysArray = this.getKeys();
        String[] newKeysArray = new String[this.getMaxDegree()];
        String[] values = new String[this.getMaxDegree()];

        //Runs through the key and value arrays from 0 to index-1.
        //Copies the contents of the original array to the new array.
        for(int i = 0; i < index; i++){
            newKeysArray[i] = keysArray[i];
            values[i] = this.getValue(i);
        }

        //Add the new key and value pair in the new arrays at index.
        newKeysArray[index] = key;
        values[index] = value;

        //Copy the rest of the elements
        for(int i = index; i < this.getCurrentDegree(); i++) {
            newKeysArray[i+1] = keysArray[i];
            values[i+1] = this.getValue(i);
        }

        //set all the new arrays and increment currentDegree.
        this.setKeysArray(newKeysArray);
        this.values = values;
        this.incrementCurrentDegree();
    }

    //Splits the internal node whenever it exceeds the maximum degree.
    @Override
    protected LeafNode splitNode() {
        int midIndex = this.getCurrentDegree()/2;

        LeafNode newRightNode = new LeafNode(this.getMaxDegree());
        for(int i = midIndex; i < this.getMaxDegree(); i++) {
            int newPosition = i-midIndex;

            //copy 2nd half of the original array into the new array
            newRightNode.setKey(this.getKey(i), newPosition);
            newRightNode.setValue(this.getValue(i), newPosition);

            //set 2nd half of the original array to null;
            this.setKey(null, i);
            this.setValue(null, i);

            this.decrementCurrentDegree();
            newRightNode.incrementCurrentDegree();
        }

        incrementLeafSplitCount();
        return newRightNode;
    }

    //pushUpKey is not applicable to leaf nodes since they do not have any children
    @Override
    public Node pushUpKey(String key, Node leftSibling, Node rightSibling){
        throw new UnsupportedOperationException();
    }



    /**
     * Methods for deletion:
     */

    //delete the key in the leaf node, return null if it doesn't exist.
    public Boolean deleteEntry(String key) {
        int index = this.search(key);

        //check if the key exists
        if(index == -1) {
            return false;
        }
        int i = 0;
        //fill in the empty space in the array
        for(i = index; i < this.getCurrentDegree()-1; i++) {
            this.setKey(this.getKey(i+1), i);
            this.setValue(this.getValue(i+1), i);
        }
        this.setKey(null, i);
        this.setValue(null, i);
        this.decrementCurrentDegree();
        return true;
    }

    @Override
    protected void mergeWithSibling(String sinkKey, Node rightSibling) {
        LeafNode rightSiblingNode = (LeafNode) rightSibling;

        for(int i = 0; i < rightSiblingNode.getCurrentDegree(); i++) {
            this.addEntry(rightSiblingNode.getKey(i), rightSiblingNode.getValue(i));
        }

        //manage siblings relationships
        this.setParentNode(rightSiblingNode.getParentNode());
        this.setRightSibling(rightSiblingNode.getRightSibling());
        if(rightSiblingNode.getRightSibling() != null) {
            rightSibling.getRightSibling().setLeftSibling(this);
        }
        incrementLeafMergeCount();
    }

    @Override
    protected String transferFromSibling(String sinkKey, Node sibling, int borrowIndex) {
        LeafNode siblingNode = (LeafNode) sibling;
        //Borrow from sibling at borrowIndex.
        //We don't need to check if it's the right or left sibling for inserting.
        String newKey = siblingNode.getKey(borrowIndex);
        String newValue = siblingNode.getValue(borrowIndex);
        this.addEntry(newKey, newValue);
        siblingNode.deleteEntry(newKey);

        /*
        Return the first key of the borrower if we borrowed from the left,
        otherwise return the first key of the right sibling after deleting
        the previous first key.
        */
        if(borrowIndex == 0) {
            return siblingNode.getKey(0);
        }
        else {
            return newKey;
        }
    }

    @Override
    protected void transferChild(Node borrower, Node lender, int borrowIndex, String oldKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Node mergeChildren(Node leftChild, Node rightChild) {
        throw new UnsupportedOperationException();
    }



    /*
        Setters/Getters:
     */

    public String getValue(int index){
        return this.values[index];
    }

    public void setValue(String value, int index) {
        values[index] = value;
    }

}