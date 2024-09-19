/*  Student information for assignment:
 *
 *  On OUR honor, Issay and Bruno, this programming assignment is OUR own work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: ics552
 *  email address: issay422@gmail.com
 *  Grader name: Devon
 *
 *  Student 2
 *  UTEID: bhz87
 *  email address: bzavaleta4@yahoo.com
 *
 */

public class HuffmanTree {
    TreeNode root;
    int size; 

    /**
     * constructor for HuffmanTree
     * @param root the node to represent the root of the tree. 
     * pre: root != null 
     */
    public HuffmanTree(TreeNode root){
        if(root == null){
            throw new IllegalArgumentException("Illegal Argument: root cannot equal null");
        }
        this.root = root;
        size = getSize(root);
    }

    /**
     * return the size of the tree. should only be called internally since O(N) time. 
     * @param n the node to get the number of children of (plus itself)
     * pre: n != null
     */
    private int getSize(TreeNode n){
        if(n == null){
            throw new IllegalArgumentException(
                "Illegal Argument for getSize: n cannot equal null");
        }
        int temp = 1;
        if(!n.isLeaf()){
            temp += getSize(n.getLeft());
            temp += getSize(n.getRight());
        }
        return temp; 
    }

    /**
     * O(1) time size method used by client
     * @return the number of nodes in the tree. 
     */
    public int size(){
        return size; 
    }

    /**
     * get the root of the tree
     * @return the root
     */
    public TreeNode getRoot(){
        return root; 
    }

    /**
     * use the IHuffViewer to print out this tree. 
     * @param myViewer the viewer to print the tree to.
     * pre: myViewer != null
     */
    public void printTree(IHuffViewer myViewer) {
        if(myViewer == null){
            throw new IllegalArgumentException("Illegal Argument: myViewer != null");
        }
        printTree(root, "", myViewer);
    }

    /**
     * recursive helper for printTree
     * @param n the current node to print
     * @param spaces space bewteen trees
     * @param myViewer the viewer to print the tree to.
     */
    private void printTree(TreeNode n, String spaces, IHuffViewer myViewer) {
        if(n != null){
            printTree(n.getRight(), spaces + "  ", myViewer);
            myViewer.update(spaces + n.getValue());
            printTree(n.getLeft(), spaces + "  ", myViewer);
        }
    }
}