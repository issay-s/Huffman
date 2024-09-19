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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;


public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private HashMap<Integer, Integer> freqMap;
    private HashMap<Integer, String> newCodes;
    private int header; 
    private HuffmanTree tree; 
    private int newBits; 
    private int originalBits; 

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        header = headerFormat; 
        BitInputStream inputStream = new BitInputStream(in); 

        // key: Integer representation of a byte, value: frequency
        freqMap = new HashMap<>(); 

        int nextByte = inputStream.readBits(BITS_PER_WORD);
        while(nextByte != -1){
            freqMap.put(nextByte, (!freqMap.containsKey(nextByte) ? 1 : freqMap.get(nextByte) + 1));
            nextByte = inputStream.readBits(BITS_PER_WORD); 
        }

        // add the pseudo EOF constant. 
        freqMap.put(PSEUDO_EOF, 1);

        // add values to nodes and those nodes to the queue
        PriorityQueue<TreeNode> nodeQueue = new PriorityQueue<>();
        for(int i = 0; i < ALPH_SIZE + 1; i++){
            if(freqMap.containsKey(i)){
                nodeQueue.enqueue(new TreeNode(i, freqMap.get(i)));
            }
        }
        // turn the individual nodes into a big tree using the huffman algorithm
        tree = processNodes(nodeQueue); 

        // key: old representation, value: new binary representation
        newCodes = makeNewCodes(tree);
        inputStream.close(); 

        originalBits = (tree.getRoot().getFrequency() - 1) * BITS_PER_WORD; 
        newBits = newBits(headerFormat);

        int saved = originalBits - newBits;
        return saved; 
    }

    /**
     * pre: none
     * @return the number of new bits the compression will write. 
     * @param headerFormat the type of header the compressed file will write. 
     */
    private int newBits(int headerFormat){
        int newBits = BITS_PER_INT + BITS_PER_INT;
        if (headerFormat == STORE_COUNTS) {
            newBits += BITS_PER_INT * (ALPH_SIZE);
        } else {
            newBits += tree.size() + newCodes.size() * (BITS_PER_WORD + 1) + BITS_PER_INT; 
        }
        for(int key : freqMap.keySet()){
            int numBytes = freqMap.get(key); 
            int length = newCodes.get(key).length();
            newBits += numBytes * length;
        }
        return newBits;
    }

    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        BitInputStream inputStream = new BitInputStream(in); 

        createOutput(out, freqMap, newCodes, inputStream, tree);

        inputStream.close();
        int saved = originalBits - newBits;

        if(saved < 0 && !force){
            myViewer.showError("Compressed file has " + -saved + " more bits than uncompressed " + 
            "file. Select \"force compression\" option to compress.");
        }
        return newBits;
    }

    /**
     * recurse through the tree, adding zeroes and ones based on going left and right to get
     *  to the old value
     * @param tree the huffman tree to traverse to make the new encodings. 
     * @return a hashmap mapping the old ascii decimal value to the new binary encoding. 
     */
    private HashMap<Integer, String> makeNewCodes(HuffmanTree tree){
        HashMap<Integer, String> newCodes = new HashMap<>();
        String currentCode = "";
        return makeNewCodesHelp(tree.getRoot(), newCodes, currentCode);
    }

    /**
     * Helper method for makeNewCodes. 
     * @param n the current node to assess
     * @param newCodes the new encodings to add to
     * @param currentCode the String containing the current code,  
     * @return a hashmap mapping the old ascii decimal value to the new binary encoding. 
     */
    private HashMap<Integer, String> makeNewCodesHelp(TreeNode n, 
            HashMap<Integer, String> newCodes, String currentCode){
        // base case: 
        if(n.getValue() != -1){
            newCodes.put(n.getValue(), currentCode);

        // recursive case: 
        } else {

            // go left
            makeNewCodesHelp(n.getLeft(), newCodes, currentCode + "0");

            // go right 
            makeNewCodesHelp(n.getRight(), newCodes, currentCode + "1");
        }
        return newCodes; 
    }

    /**
     * method to execut the huffman algorithm, dequeueing first two nodes, combining them, 
     * and requeueing. Repeating the process until one node remains in the queue. 
     * @param nodeQueue with which to process the nodes. 
     * @return a HuffmanTree with the last node in the queue as its root. 
     */
    private HuffmanTree processNodes(PriorityQueue<TreeNode> nodeQueue){
        while(nodeQueue.size() > 1){
            TreeNode node1 = nodeQueue.dequeue();
            TreeNode node2 = nodeQueue.dequeue();
            TreeNode newNode = new TreeNode(node1, -1, node2); 
            nodeQueue.enqueue(newNode);
        }
        HuffmanTree result = new HuffmanTree(nodeQueue.getFirst());
        return result; 
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        BitInputStream inputStream = new BitInputStream(in);

        // key: Integer representation of a byte, value: frequency
        int magicNumber = inputStream.readBits(BITS_PER_INT);
        HuffmanTree tree = null;
        if (magicNumber != MAGIC_NUMBER) {
            inputStream.close();
            myViewer.showError("Error reading compressed file. \n" +
                    "File did not start with the huff magic number.");
            return -1;
        }
        magicNumber = inputStream.readBits(BITS_PER_INT);
        if (magicNumber == STORE_COUNTS) {
            tree = uncompressSCF(inputStream);
        } else {
            tree = uncompressSTF(inputStream); 
        }
        int numWritten = decode(tree.getRoot(), new BitOutputStream(out), inputStream);
        inputStream.close();
        return numWritten;
    }

    /**
     * Decode the compressed file using the binary tree of encodings. 
     * 
     * @param root the root of the tree of encodings
     * @param bitOut the file to write to 
     * @param bitIn the file to read/decode
     * @return an int representing the number of bits written. 
     */
    private int decode(TreeNode root, BitOutputStream bitOut, BitInputStream bitIn) 
        throws IOException {
        int numWritten = 0;
        TreeNode n = root; 
        boolean done = false;
        while(!done){
            int bit = bitIn.readBits(1);
            if(bit == -1){
                throw new IOException("Error reading compressed file. \n" +
                        "unexpected end of input. No PSEUDO_EOF value.");
            } else {
                if(bit == 0){
                    n = n.getLeft(); 
                } else {
                    n = n.getRight(); 
                }
                if(n.getValue() != -1){
                    if(n.getValue() == PSEUDO_EOF){
                        done = true;
                    } else {
                        numWritten += BITS_PER_WORD; 
                        bitOut.writeBits(BITS_PER_WORD, n.getValue());
                        n = root; 
                    }
                } 
            }
        }
        return numWritten;
    }

    /**
     * decode the SCF header information and make a tree of it. 
     * @param inputStream the stream to read from
     * @return a Tree of encodings with which to rewrite the uncompressed file. 
     */
    private HuffmanTree uncompressSCF(BitInputStream inputStream) throws IOException {
        int next; 
        PriorityQueue<TreeNode> nodeQueue = new PriorityQueue<>();
        for(int i = 0; i < ALPH_SIZE; i++){
            next = inputStream.readBits(BITS_PER_INT);
            if(next != 0){
                nodeQueue.enqueue(new TreeNode(i, next));
            }
        }
        nodeQueue.enqueue(new TreeNode(PSEUDO_EOF, 1));
        return processNodes(nodeQueue);
    }

    /**
     * decode the STF header information and make a tree of it. 
     * @param inputStream the stream to read from
     * @return a Tree of encodings with which to rewrite the uncompressed file. 
     */
    private HuffmanTree uncompressSTF(BitInputStream inputStream) throws IOException {
        inputStream.readBits(BITS_PER_INT); // eat the size value
        TreeNode root = getNode(inputStream);
        return new HuffmanTree(root);
    }

    /**
     * recursive helper method for uncompressSTF
     * @param inputStream the stream to read from
     * @return the root node of the tree of encodings with which to rewrite the uncompressed file. 
     */
    private TreeNode getNode(BitInputStream inputStream) throws IOException{
        TreeNode newNode; 
        if(inputStream.readBits(1) == 0){
            newNode = new TreeNode(-1, -1);
            newNode.setLeft(getNode(inputStream));
            newNode.setRight(getNode(inputStream));
        } else {
            int value = inputStream.readBits(BITS_PER_WORD + 1); 
            newNode = new TreeNode(value, -1);
        }
        return newNode;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s){
        if (myViewer != null) {
            myViewer.update(s);
        }
    }

    /**
     * encode the SCF header information. write 256 32 bit integers representing the 
     * frequencies of each distinct byte.
     * @param bitOut the file to write to 
     * @param map the map of ascii values and frequencies.
     */
    private void createSCF(BitOutputStream bitOut, HashMap<Integer, Integer> map) {
        for(int i = 0; i < ALPH_SIZE; i++){
            if(map.containsKey(i)){
                bitOut.writeBits(BITS_PER_INT, map.get(i));
                
            } else {
                bitOut.writeBits(BITS_PER_INT, 0);
            }
        }
    }

    /**
     * encode the STF header information. traverse the tree, writing a 0 if the node is a leaf, 
     * 1 otherwise. If 1, write the node's value. 
     * @param bitOut the stream to write the STF to 
     * @param tree the tree of encodings to traverse. 
     */
    private void createSTF(BitOutputStream bitOut, HuffmanTree tree){
        TreeNode n = tree.getRoot();
        createSTFHelp(bitOut, n);
    }

    /**
     * helper method for createSTF. 
     * @param bitOut the stream to write the STF to 
     * @param n the current node to assess. 
     */
    private void createSTFHelp(BitOutputStream bitOut, TreeNode n){
        if(n.isLeaf()){
            bitOut.writeBits(1, 1);
            int value = n.getValue();
            bitOut.writeBits(BITS_PER_WORD + 1, value);
        } else {
            bitOut.writeBits(1, 0);
            createSTFHelp(bitOut, n.getLeft());
            createSTFHelp(bitOut, n.getRight());
        }
    }

    /**
     * write the compressed file. (magic number, counts/tree, encodings, peof)
     * @param out output stream to be converted into a BitOutputStream
     * @param map map of frequencies 
     * @param newCodes the new encodings
     * @param in input stream to be converted into a BitInputStream
     * @param tree the tree of encodings to traverse. 
     */
    private void createOutput(OutputStream out, HashMap<Integer, 
                                Integer> map, HashMap<Integer, String> newCodes, InputStream in, 
                                HuffmanTree tree) throws IOException{
        BitOutputStream bitOut = new BitOutputStream(out);

        // write magic number
        bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);

        // write header
        if(header == STORE_COUNTS){
            bitOut.writeBits(BITS_PER_INT, STORE_COUNTS);
            createSCF(bitOut, map);
        } else {
            bitOut.writeBits(BITS_PER_INT, STORE_TREE);
            bitOut.writeBits(BITS_PER_INT, tree.size() + (newCodes.size() * (BITS_PER_WORD + 1)));
            createSTF(bitOut, tree);
        }
        writeEncodings(in, bitOut);
        bitOut.close();
    }



    /**
     * write just the encodings/peof in the compressed. 
     * @param in input stream to be converted into a BitInputStream
     * @param bitOut BitOutputStream to write the encodings to. 
     */
    private void writeEncodings(InputStream in, BitOutputStream bitOut) throws IOException{
        // write new encoded data
        BitInputStream inputStream = new BitInputStream(in);
        int nextByte = inputStream.readBits(BITS_PER_WORD);
        while (nextByte != -1) {
            String newEncoding = newCodes.get(nextByte);
            for (int c = 0; c < newEncoding.length(); c++) {
                bitOut.writeBits(1, Integer.parseInt(newEncoding.charAt(c) + ""));
            }
            nextByte = inputStream.readBits(BITS_PER_WORD);
        }
        String peof = newCodes.get(PSEUDO_EOF);

        // write peof
        for (int c = 0; c < peof.length(); c++) {
            bitOut.writeBits(1, Integer.parseInt(peof.charAt(c) + ""));
        }

        inputStream.close();
    }
}
