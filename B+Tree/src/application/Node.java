package application;
import java.util.Arrays;

abstract class Node {
    protected String[] keys;
    protected Node leftSibling;
    protected Node rightSibling;
    protected Node parentNode;
    protected int maxDegree;
    protected int minDegree;
    protected int currentDegree;
    protected static int leafSplitCount;
    protected static int leafMergeCount;
    protected static int internalSplitCount;
    protected static int internalMergeCount;

    /**
     *  Methods for insertion:
     */

    //Spits up leaf and internal node when there are too many keys.
    public Node dealWithOverflow() {
        int midIndex = this.getCurrentDegree()/2;
        String middleKey = this.getKey(midIndex);

        Node newRightNode = this.splitNode();

        //create a new InternalNode if this node doesn't have a parent
        if(this.getParentNode() == null) {
            this.setParentNode(new InternalNode(this.getMaxDegree()));
        }
        newRightNode.setParentNode(this.getParentNode());

        //set the relations of the new node
        newRightNode.setLeftSibling(this);
        newRightNode.setRightSibling(this.rightSibling);

        //manage the relations of the original nodes
        if(this.getRightSibling() != null) {
            this.getRightSibling().setLeftSibling(newRightNode);
        }
        this.setRightSibling(newRightNode);

        return this.getParentNode().pushUpKey(middleKey, this, newRightNode);
    }

    public boolean exceedsMaximum() {
        if(this.getMaxDegree() <= this.getCurrentDegree())
            return true;
        return false;
    }

    protected abstract Node splitNode();

    protected abstract int search(String key);

    //put key of new leaf or internal node up to the parent node. Only used in InternalNode
    protected abstract Node pushUpKey(String key, Node leftSibling, Node rightSibling);



    /**
     * Methods for deletion:
     */

    public Node dealWithUnderflow(String oldKey) {
        //check if node is root
        if(this.getParentNode() == null) {
            return null;
        }

        Node leftSibling = this.getLeftSibling();
        //check if we can borrow a key from left sibling
        if(leftSibling != null && leftSibling.canGiveKey() && this.hasSameParent(leftSibling)) {
            this.getParentNode().transferChild(this, leftSibling, leftSibling.getCurrentDegree()-1, oldKey);
            return null;
        }

        //if we can't borrow from left, check if we can borrow from right sibling
        Node rightSibling = this.getRightSibling();
        if(rightSibling != null && rightSibling.canGiveKey() && this.hasSameParent(rightSibling)) {
            this.getParentNode().transferChild(this, rightSibling, 0, oldKey);
            return null;
        }

        //merge siblings if they can't lend a key
        if(leftSibling != null) {
            return this.getParentNode().mergeChildren(leftSibling, this);
        }
        else {
            return this.getParentNode().mergeChildren(this, rightSibling);
        }
    }

    public boolean isInsufficient() {
        if(this.getMinDegree() > this.getCurrentDegree())
            return true;
        return false;
    }

    public Boolean canGiveKey() {
        if(this.getCurrentDegree() > this.getMinDegree()) {
            return true;
        }
        return false;
    }

    protected Boolean hasSameParent(Node node) {
        if(this.getParentNode() == node.getParentNode()) {
            return true;
        }
        return false;
    }

    protected abstract void transferChild(Node borrower, Node lender, int borrowIndex, String oldKey);

    protected abstract Node mergeChildren(Node leftChild, Node rightChild);

    protected abstract void mergeWithSibling(String sinkKey, Node rightSibling);

    protected abstract String transferFromSibling(String sinkKey, Node sibling, int borrowIndex);


    /*
    Setters/Getters:
     */

    public Node getLeftSibling() {
        return leftSibling;
    }

    public Node getRightSibling() {
        return rightSibling;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public int getMinDegree() {
        return minDegree;
    }

    public int getCurrentDegree() {
        return currentDegree;
    }

    public String[] getKeys() {
        return keys;
    }

    public String getKey(int i) {
        return keys[i];
    }

    public int getLeafMergeCount() {
        return leafMergeCount;
    }

    public int getLeafSplitCount() {
        return leafSplitCount;
    }

    public int getInternalMergeCount() {
        return internalMergeCount;
    }

    public int getInternalSplitCount() {
        return internalSplitCount;
    }



    public void setLeftSibling(Node leftSibling) {
        this.leftSibling = leftSibling;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public void setRightSibling(Node rightSibling) {
        this.rightSibling = rightSibling;
    }

    public void setKeysArray(String[] keys) {
        this.keys = keys;
    }

    public void setKey(String key, int index) {
        keys[index] = key;
    }

    public void incrementCurrentDegree() {
        currentDegree++;
    }

    public void decrementCurrentDegree() {
        currentDegree--;
    }

    public void incrementLeafSplitCount() {
        leafSplitCount++;
    }

    public void incrementLeafMergeCount() {
        leafMergeCount++;
    }

    public void incrementInternalSplitCount() {
        internalSplitCount++;
    }

    public void incrementInternalMergeCount() {
        internalMergeCount++;
    }

    /*
    Other useful methods
     */

    public void displayKeys() {
        System.out.println(Arrays.toString(this.getKeys()));
    }

    public int getIndexOfKey(String key) {
        for(int i = 0; i < this.getCurrentDegree(); i++) {
            if(this.getKey(i).equals(key)) {
                return i;
            }
        }
        return -1;
    }
}