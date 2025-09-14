package no.uib.inf102.wordle.model.word;

/**
 * This enum represents the answer type a WordleCharacter can have. The possible
 * answer types are BLANK, WRONG, MISPLACED and CORRECT.
 */
public enum AnswerType {

    BLANK('b'),
    WRONG('w'),
    MISPLACED('m'),
    CORRECT('c');

    public final char character;

    private AnswerType(char c) {
        this.character = c;
    }

}
