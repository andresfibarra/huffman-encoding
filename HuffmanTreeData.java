public class HuffmanTreeData {
    private char character;
    private int frequency;

    public HuffmanTreeData(int frequency) {
        character = '*'; //arbitrary character to fill the space
        this.frequency = frequency;
    }

    public HuffmanTreeData (char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    public char getCharacter() {
        return character;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return character + ":" + frequency;
    }
}
