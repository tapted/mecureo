package foldocpos;

/**
 * <p>An interface for dictionary-style callbacks, with empty implementations</p>
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class DictAdapter {
    /**
     * Called when the end of a sentence has been reached
     */
    public void endOfSentence() {}
    /**
     * Called when the end of a paragraph has been reached.<br>
     * Always preceded by an endOfSentence()
     */
    public void endOfParagraph() {}
    /**
     * Called when a word (in a sentence) is found.<br>
     *
     * @param word The word found (multi-word prepositions are combined)
     * @param tag The part-of-speech tag, or null if it could not be determined
     */
    public void word(String word, String tag) {}
    /**
     * Called when the end of the definition has been reached<br>
     * Always preceded by an endOfParagraph
     */
    public void endOfDefinition() throws java.io.IOException {}
}

