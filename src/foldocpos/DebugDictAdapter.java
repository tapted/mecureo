package foldocpos;

/**
 * <p>A DictAdapter that provides debugging output of tokens to stdout</p>
 * <p> </p>
 * @author Trent Apted
 * @version 0.4
 */
public class DebugDictAdapter extends DictAdapter {
    public void endOfSentence() {
	System.out.print(". ");
    }
    public void endOfParagraph() {
    	System.out.print("\n   ");
    }
    public void word(String word, String tag) {
    	System.out.print(" " + word + "[" + tag + "]");
    }
}

