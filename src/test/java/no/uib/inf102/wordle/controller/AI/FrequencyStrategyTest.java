package no.uib.inf102.wordle.controller.AI;
 
import static no.uib.inf102.wordle.model.word.AnswerType.WRONG;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
 
import java.time.Duration;
 
import org.junit.jupiter.api.Test;
 
import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.model.word.AnswerType;
import no.uib.inf102.wordle.model.word.WordleWord;
 
/**
 * 
 * @author Martin Vatshelle og Joakim Hauger Sunde
 */
public class FrequencyStrategyTest {
 
	private Dictionary dictionary = new Dictionary();
 
	private static final int FIRST_GUESS_MS = 30;     // budget for makeGuess(null)
	private static final int AFTER_FEEDBACK_MS = 45;  // budget for makeGuess(feedback)
	private static final int BULK_CALLS = 100;        // number of repeated calls
	private static final int BULK_MS = 3000;           // total budget for the bulk test
 
	@Test
	public void makeGuessBetterThanSaree() {
		FrequencyStrategy ai = new FrequencyStrategy(dictionary);
		String guess = ai.makeGuess(null);
		int count = countGreen(guess);
		assertTrue(dictionary.isLegalGuess(guess),"You must guess a word from the dictionary.");
		if(dictionary.isLegalGuess("saree")) {
			int expectedCount = countGreen("saree");
			assertTrue(expectedCount<=count, "The word saree gave more green hits than "+guess);
		}
	}
 
	public int countGreen(String guess) {
		int count=0;
		for(String word : dictionary.getGuessWordsList()) {
			for(int i=0; i<guess.length(); i++) {
				if(word.charAt(i)==guess.charAt(i))
					count++;
			}
		}
		return count;
	}
 
	@Test
	public void makeGuessafterScore() {
		AnswerType[] oneYellow = {WRONG,AnswerType.MISPLACED,WRONG,WRONG,WRONG};
		WordleWord feedback = new WordleWord("score", oneYellow);
 
		FrequencyStrategy ai = new FrequencyStrategy(dictionary);
		String guess = ai.makeGuess(feedback);
		String good = "catch";
 
		assertTrue(dictionary.isLegalGuess(guess),"You must guess a word from the dictionary.");
 
		if(dictionary.isLegalGuess(good)) {
			int count = countGreen(feedback, guess);
			int expectedCount = countGreen(feedback, good);
			assertTrue(expectedCount<=count, "The word "+good+" gave more green hits than "+guess);
		}
	}
 
	public int countGreen(WordleWord feedback, String guess) {
		int count=0;
		for(String word : dictionary.getGuessWordsList()) {
			if(WordleWord.isPossibleWord(word, feedback)) {
				for(int i=0; i<guess.length(); i++) {
					if(word.charAt(i)==guess.charAt(i))
						count++;
				}				
			}
		}
		return count;
	}
 
 
	@Test
	public void firstGuessIsFast() {
		FrequencyStrategy ai = new FrequencyStrategy(dictionary);
 
		String guess = assertTimeoutPreemptively(
			Duration.ofMillis(FIRST_GUESS_MS),
			() -> ai.makeGuess(null),
			"First makeGuess(null) took too long; your FrequencyStrategy is too slow."
		);
 
		assertTrue(dictionary.isLegalGuess(guess), "You must guess a word from the dictionary.");
	}
 
	@Test
	public void guessAfterFeedbackIsFast() {
		AnswerType[] oneYellow = { WRONG, AnswerType.MISPLACED, WRONG, WRONG, WRONG };
		WordleWord feedback = new WordleWord("score", oneYellow);
 
		FrequencyStrategy ai = new FrequencyStrategy(dictionary);
 
		String guess = assertTimeoutPreemptively(
			Duration.ofMillis(AFTER_FEEDBACK_MS),
			() -> ai.makeGuess(feedback),
			"makeGuess(feedback) took too long; your FrequencyStrategy is too slow after feedback."
		);
 
		assertTrue(dictionary.isLegalGuess(guess), "You must guess a word from the dictionary.");
	}
 
	@Test
	public void manyCallsAreFast() { //this test includes time of constructor
		AnswerType[] oneYellow = { WRONG, AnswerType.MISPLACED, WRONG, WRONG, WRONG };
		WordleWord feedback = new WordleWord("score", oneYellow);
 
		assertTimeoutPreemptively(
			Duration.ofMillis(BULK_MS),
			() -> {
				for (int i = 0; i < BULK_CALLS; i++) {
					FrequencyStrategy ai = new FrequencyStrategy(dictionary);
					String g = ai.makeGuess((i & 1) == 0 ? null : feedback);
					if (!dictionary.isLegalGuess(g)) {
						throw new AssertionError("Illegal guess: " + g);
					}
				}
			},
			BULK_CALLS + " makeGuess calls exceeded time budget; implementation appears too slow."
		);
	}
}