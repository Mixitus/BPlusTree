package application;

import java.util.Arrays;

public class InternalNode extends Node {
    private Node[] childPointers;


    public InternalNode(int maxDegree){
        this.minDegree = (maxDegree/2) - 1;
        this.maxDegree = maxDegree;
        this.keys = new String[maxDegree];
        this.childPointers = new Node[maxDegree+1];
        this.currentDegree = 0;
    }

    //Search for specified key and return the index of the childPointers array.
    //Used to traverse through the array for insertion and deletion
    @Override
    public int search(String key) {
        int index = 0;
        //check if first position in keys array is null
        if (this.getKey(index) == null) {
            return index;
        }

        int compareKeys = key.compareTo(this.getKey(index));
        //While the specified key is greater than the keys in the node, keep looping.
        while(compareKeys >= 0) {
            if(compareKeys == 0) {
                return index+1;
            }
            index++;
            //break the loop if we're at the end of the array
            if(index == this.getCurrentDegree()) {
                break;
            }
            compareKeys = key.compareTo(this.getKey(index));
        }
        return index;
    }



    /**
     * Methods for insertion:
     */

    //Split current node when it exceeds the currentDegree.
    @Override
    protected InternalNode splitNode() {
        int midIndex = this.getCurrentDegree()/2;
        InternalNode newInternalNode = new InternalNode(this.getMaxDegree());

        //splitting the keys
        for(int i = midIndex + 1; i < this.getMaxDegree(); ++i) {
            int newPosition = i - midIndex - 1  ;

            newInternalNode.setKey(this.getKey(i), newPosition);
            this.setKey(null, i);

            this.decrementCurrentDegree();
            newInternalNode.incrementCurrentDegree();
        }

        //splitting the child pointers
        for(int i = midIndex + 1; i <= this.getMaxDegree(); ++i) {
            int newPosition = i - midIndex - 1;

            //set the child pointers
            newInternalNode.setChild(this.getChild(i), newPosition);
            //set the parent of each child to the new internal node
            newInternalNode.getChild(newPosition).setParentNode(newInternalNode);
            this.setChild(null, i);
        }

        //set key at midIndex to null since it will be pushed up to the parent
        this.setKey(null, midIndex);
        this.decrementCurrentDegree();
        incrementInternalSplitCount();

        return  newInternalNode;
    }

    //Stores the new key value after a node is split
    protected Node pushUpKey(String key, Node leftSibling, Node rightSibling) {
        int index = this.search(key);
        this.insertAt(key, index, leftSibling, rightSibling);

        //check for overflow after adding a key to parent.
        if(this.exceedsMaximum()) {
            return this.dealWithOverflow();
        }
        else {
            //if this node doesn't have a parent, then return it and make it the root.
            if(this.getParentNode() == null) {
                return this;
            }
            return null;
        }
    }

    //Insert a new key and child pointer to the keys array
    protected void insertAt(String key, int index, Node leftChild, Node rightChild) {
        //make space for the new child pointers and key
        int currentIndex = this.getMaxDegree()-2;

        while(currentIndex >= index) {
            this.setChild(this.getChild(currentIndex+1), currentIndex+2);
            this.setKey(this.getKey(currentIndex), currentIndex+1);

            currentIndex--;
        }

        //set the new pointers and key at the specified index.
        this.setKey(key, index);
        this.setChild(leftChild, index);
        this.setChild(rightChild, index + 1);
        this.incrementCurrentDegree();
    }



    /**
     * Methods for deletion:
     */

    private void deleteAt(int index) {
        int i = 0;
        for(i = index; i < this.getCurrentDegree()-1; i++) {
            this.setKey(this.getKey(i+1), i);
            this.setChild(this.getChild(i+2), i+1);
        }

        this.setKey(null, i);
        this.setChild(null, i+1);
        this.decrementCurrentDegree();
    }

    @Override
    protected void transferChild(Node borrower, Node lender, int borrowIndex, String oldKey) {
        int indexOfBorrower = 0;

        //find the index where borrower is stored in the parent childPointers
        for(int i = 0; i < this.getCurrentDegree()+1; i++) {
            if(this.getChild(i) == borrower) {
                indexOfBorrower = i;
                break;
            }
        }

        //check if we're borrowing from right or left sibling
        if(borrowIndex == 0) {
            //borrow from right sibling and set new key in the parent
            String newKey = borrower.transferFromSibling(this.getKey(indexOfBorrower), lender, borrowIndex);
            this.setKey(newKey, indexOfBorrower);
            this.updateParentKey(borrower.getKey(0), oldKey);
        }
        else {
            //borrow from left sibling and set new key in the parent
            if(indexOfBorrower == 0) {
                indexOfBorrower = 1;
            }

            String newKey = borrower.transferFromSibling(this.getKey(indexOfBorrower-1), lender, borrowIndex);
            this.setKey(newKey, indexOfBorrower-1);
        }
    }

    //When a key deleted from a leafNode, update all the parent nodes so the old key
    //doesn't exist in the tree anymore
    private void updateParentKey(String newKey, String oldKey) {
        Node node = this;
        while(node.getParentNode() != null) {
            if(node.getParentNode().getIndexOfKey(oldKey) != -1) {
                int indexOfKey = node.getParentNode().getIndexOfKey(oldKey);
                node.getParentNode().setKey(newKey, indexOfKey);
            }
            node = node.getParentNode();
        }
    }

    //used in transferFromSibling()
    private void deleteAtLeft(int index) {
        int i = 0;
        for(i = index; i < this.getCurrentDegree(); i++) {
            this.setKey(this.getKey(i+1), i);
            this.setChild(this.getChild(i+1), i);
        }

        this.setKey(null, i);
        this.setChild(null, i+1);
        this.decrementCurrentDegree();
    }

    @Override
    protected String transferFromSibling(String sinkKey, Node sibling, int borrowIndex) {
        InternalNode siblingNode = (InternalNode) sibling;
        String newKey = null;
        //check if we're borrowing from right or left sibling
        if(borrowIndex == 0) {
            //push up the first key from the right sibling to the parent, and bring down a key from
            //the parent and store it in the borrower.
            this.setKey(sinkKey, this.getCurrentDegree());
            this.setChild(siblingNode.getChild(borrowIndex), this.getCurrentDegree()+1);

            //set the child's new parent to the borrower
            siblingNode.getChild(borrowIndex).setParentNode(this);
            this.incrementCurrentDegree();

            newKey = siblingNode.getKey(borrowIndex);
            //We use deleteAtLeft() instead of deleteAt() because we need to
            //delete the first child of the rightSibling
            siblingNode.deleteAtLeft(borrowIndex);
        }
        else {
            //borrow last key from left sibling
            Node newLeftChild = siblingNode.getChild(borrowIndex + 1);
            Node newRightChild = this.getChild(0);

            //set the child's new parent to the borrower
            newLeftChild.setParentNode(this);
            this.insertAt(sinkKey, 0, newLeftChild, newRightChild);

            newKey = siblingNode.getKey(borrowIndex);
            siblingNode.deleteAt(borrowIndex);
        }

        //return the key that's going to be pushed to the parent
        return newKey;
    }

    @Override
    protected Node mergeChildren(Node leftChildNode, Node rightChildNode) {
        int indexOfChild = 0;
        //find index of the child in the parent node
        for(int i = 0; i < this.getCurrentDegree()+1; i++) {
            if(this.getChild(i) == leftChildNode) {
                indexOfChild = i;
                break;
            }
        }

        leftChildNode.mergeWithSibling(this.getKey(indexOfChild), rightChildNode);
        String oldKey = this.getKey(indexOfChild);

        //remove the leftChildNode key from the parent
        if(indexOfChild == 0 && this.getChild(0).isInsufficient()) {
            //remove the left child when leftSibling has a different parent.
            this.deleteAtLeft(indexOfChild);
        }
        else {
            //remove of the right child in other cases
            this.deleteAt(indexOfChild);
        }

        //we need to check if parent node is below the minimum degree after removing a key
        if(this.isInsufficient()) {

            //If this is the root node, delete it if it has no more keys. Otherwise, keep it as the root
            if(this.getParentNode() == null) {
                if(this.getCurrentDegree() == 0) {
                    leftChildNode.setParentNode(null);
                    //return leftChildNode and make it the root;
                    return leftChildNode;
                }
                else {
                    return null;
                }
            }
            return this.dealWithUnderflow(oldKey);
        }

        return null;
    }

    @Override
    protected void mergeWithSibling(String sinkKey, Node rightSibling) {
        InternalNode rightSiblingNode = (InternalNode) rightSibling;

        //Add the sinkKey from the parent into the left sibling
        //Increase the current degree since it was previously decreased
        int leftChildIndex = this.getCurrentDegree();
        this.setKey(sinkKey, leftChildIndex);
        leftChildIndex++;
        this.incrementCurrentDegree();

        //Transfer rightSibling keys into left child node
        for(int i = 0; i < rightSiblingNode.getCurrentDegree(); i++)  {
            this.setKey(rightSiblingNode.getKey(i), leftChildIndex + i);
            this.incrementCurrentDegree();
        }

        //Transfer rightSibling children pointers into left child node and reassign parent
        for(int i = 0; i < rightSiblingNode.getCurrentDegree() + 1; i++) {
            this.setChild(rightSiblingNode.getChild(i), leftChildIndex + i);
            rightSiblingNode.getChild(i).setParentNode(this);
        }

        //managing sibling relationships
        this.setRightSibling(rightSiblingNode.getRightSibling());
        if(rightSiblingNode.getRightSibling() != null) {
            //if the right sibling had a right sibling, set its left sibling to this node.
            rightSiblingNode.getRightSibling().setLeftSibling(this);
        }
        incrementInternalMergeCount();
    }

    /*
    Setters/Getters:
     */

    public void setChild(Node pointer, int index) {
        this.childPointers[index] = pointer;
    }

    public Node getChild(int index) {
        return this.childPointers[index];
    }

    public Node[] getChildren() {
        return this.childPointers;
    }
}