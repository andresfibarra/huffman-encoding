import java.io.*;
import java.util.*;

/**
 * Uses Huffman Encoding to compress files and decompress them
 * @author Andres Ibarra, Spring 2021, CS10 Problem Set 3
 */
public class HuffmanEncoder {
    Map<Character, Integer> charFrequencies;    //map of character -> the number of times it appears
    Map<Character, String> codeWords;           //map of charcter -> its respective "bit word"
    BinaryTree<HuffmanTreeData> tree;           //big tree used to find the code words

    public HuffmanEncoder() {
        charFrequencies = new TreeMap<Character, Integer>();
        codeWords = new TreeMap<Character, String>();
    }

    /**
     * Add items to the charFrequencies instance variable, which maps Character -> # times it appears
     * @param fileName - path name of the file to be read and compressed
     */
    public void createFreqMap(String fileName) {
        BufferedReader input;

        try {
            input = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            System.err.println("Cannot open file. \n" + e.getMessage());
            return;
        }

        try {
            int c = input.read();
            while (c != -1) {                               //while there is still a character to be read
                Character temp = new Character((char)c);
                if (charFrequencies.containsKey(temp)) {                        //if character already in map
                    charFrequencies.put(temp, charFrequencies.get(temp) +1);    //mark as one more occurence
                }
                else {                                                          //if not in the map
                    charFrequencies.put(temp, 1);                               //put in map with a frequency of 1
                }
                c = input.read();
            }
        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        try {
            input.close();
        }
        catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
    }

    /**
     * Create the tree used to find the code words
     */
    public void createHuffmanTree () {
        //create trees
        List<BinaryTree<HuffmanTreeData>> singleCharTrees = new ArrayList<BinaryTree<HuffmanTreeData>>();

        for (char charKey: charFrequencies.keySet()) {     //build up list of binary Trees holding data
            singleCharTrees.add(new BinaryTree<HuffmanTreeData>(new HuffmanTreeData(charKey, charFrequencies.get(charKey))));
        }

        /**
         * Comparator for PQ with BinaryTree <HuffmanTreeData> that compares the frequencies of two trees
         */
        class FrequencyComparator implements Comparator<BinaryTree<HuffmanTreeData>> {
            public int compare(BinaryTree<HuffmanTreeData> t1, BinaryTree<HuffmanTreeData> t2) {
                return t1.data.getFrequency() - t2.data.getFrequency();
            }
        }

        Comparator<BinaryTree<HuffmanTreeData>> freqCompare = new FrequencyComparator();
        PriorityQueue<BinaryTree<HuffmanTreeData>> pq = new PriorityQueue<BinaryTree<HuffmanTreeData>>(freqCompare);

        pq.addAll(singleCharTrees);

        //create big tree
        if (pq.size() > 0) {
            BinaryTree<HuffmanTreeData> t1;
            BinaryTree<HuffmanTreeData> t2;
            //while there are at least 2 trees in pq, join the two smallest trees with a newRoot of the sum of
                //their frequencies and add this new tree to the priority queue
            while (pq.size() > 1) {
                t1 = pq.remove();
                t2 = pq.remove();
                BinaryTree<HuffmanTreeData> newRoot =
                        new BinaryTree<HuffmanTreeData> (new HuffmanTreeData(t1.data.getFrequency()
                                + t2.data.getFrequency()), t1, t2);
                pq.add(newRoot);
            }

            tree = pq.remove();     //set the tree instance variable to the big tree just created
        }
        else {  //if there are no trees in priority queue (happens if it is an empty file)
            tree = null;
        }

    }

    /**
     * Recursive function to add values to the codeWords map
     * @param tree - the node being examined
     * @param codeSoFar - the code word up until that point
     */
    public void createCodeWords (BinaryTree<HuffmanTreeData> tree, String codeSoFar) {
        if (tree != null) {
            if (tree.isLeaf()) {    //if no more digits to add to the code word, add to teh map
                codeWords.put(tree.data.getCharacter(), codeSoFar);
            }
            else {
                if (tree.hasLeft()) {   //recurse to left child
                    createCodeWords(tree.getLeft(), codeSoFar + "0");
                }
                if (tree.hasRight()) {  //recurse to right child
                    createCodeWords(tree.getRight(), codeSoFar + "1");
                }
            }
        }
    }

    /**
     * Compress the provided file
     * @param fileName - path to file that will be compressed
     * @throws IOException
     */
    public void compress(String fileName) throws IOException{
        BufferedReader input;           //the file reader
        BufferedBitWriter bitOutput;    //what will write the bits in the compressed file

        createFreqMap(fileName);        //set up the charFrequencies map

        createHuffmanTree();            //set up the tree that will be used to find the code words
        System.out.println(tree);       //print check

        createCodeWords(tree, "");  //add values to the map of characters -> code words
        if (tree != null) {
            if (tree.size() == 1) {
                codeWords.put(tree.data.getCharacter(), "0");
            }
        }

        try{
            input = new BufferedReader(new FileReader(fileName));
            bitOutput = new BufferedBitWriter(fileName.substring(0, fileName.length()-4) + "_compressed.txt");

       }
        catch (FileNotFoundException e) {
            System.err.println("Cannot open file. \n" + e.getMessage());
            return;
        }

        try {
            int c = input.read();   //int representing next character

            while (c != -1) {       //while there is another character to read
                char charRead = (char) c;
                //get code word
                String charCodeWord = codeWords.get(charRead);
                //loop over code word
                if (charCodeWord != null) {
                    for (int i = 0; i < charCodeWord.length(); i++) {
                        char digit = charCodeWord.charAt(i);
                        //write bit for each digit in codeWord
                        if (digit == '0') {
                            bitOutput.writeBit(false);
                        }
                        else if (digit == '1') {
                            bitOutput.writeBit(true);
                        }
                    }
                }
                c = input.read();

            }
        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }
        finally {
            try {
                input.close();
                bitOutput.close();
            }
            catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
            }
        }
    }

    /**
     * Decompress the provided file
     * @param fileName - path to compressed file to be decompressed
     * @throws IOException
     */
    public void decompress(String fileName) throws IOException {
        BufferedBitReader bitInput;
        BufferedWriter output;

        try{
            bitInput = new BufferedBitReader(fileName);
            output = new BufferedWriter(new FileWriter(fileName.substring(0, fileName.length() - 15) + "_decompressed.txt"));

        }
        catch (FileNotFoundException e) {
            System.err.println("Cannot open file. \n" + e.getMessage());
            return;
        }

        try {
            String constructedWord = "";
            BinaryTree<HuffmanTreeData> tempTree = tree;

            //System.out.println(codeWords);        //print check

            while (bitInput.hasNext()) {
                boolean bit = bitInput.readBit();

                // Loop over bits in compressed file
                if (bit) { //if next bit is a 1
                    tempTree = tempTree.getRight();
                    constructedWord += "1";
                }
                else { //if next bit is a 0
                    if (tempTree.getLeft() != null) {
                        tempTree = tempTree.getLeft();
                    }
                    constructedWord += "0";
                }
                if (tempTree.isLeaf()) {
                    output.write((int)tempTree.data.getCharacter());
                    tempTree = tree;    //reset tree
                }
            }
        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }
        finally {
            try {
                bitInput.close();
                output.close();
            }
            catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
            }
        }
    }


    public static void main(String[] args) {
        HuffmanEncoder h1 = new HuffmanEncoder();

        try {
            h1.compress("inputs/testTextFile_poem.txt");
            h1.decompress("inputs/testTextFile_poem_compressed.txt");

            h1.compress("inputs/testTextFile_emptyFile.txt");
            h1.decompress("inputs/testTextFile_emptyFile_compressed.txt");

            h1.compress("inputs/testTextFile_oneCharacter.txt");
            h1.decompress("inputs/testTextFile_oneCharacter_compressed.txt");

            h1.compress("inputs/testTextFile_repeatCharacter.txt");
            h1.decompress("inputs/testTextFile_repeatCharacter_compressed.txt");

            //h1.compress("inputs/WarAndPeace.txt");
            //h1.decompress("inputs/WarAndPeace_compressed.txt");

            //h1.compress("inputs/USConstitution.txt");
            //h1.decompress("inputs/USConstitution_compressed.txt");
        }
        catch (IOException e) {
            System.err.println("Error.\n" + e.getMessage());
        }




    }
}