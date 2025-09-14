package no.uib.inf102.wordle.controller.AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.model.word.WordleWord;

/**
 * This strategy guesses a new, random and legal word whether the word is a
 * possible answer or not.
 */
public class RandomStrategy implements IStrategy {

    private Dictionary dictionary;
    private List<String> POSSIBLE_WORDS;
    private static Random random;

    public RandomStrategy(Dictionary dictionary) {
        this.dictionary = dictionary;
        reset();
    }

    @Override
    public String makeGuess(WordleWord feedback) {
        return selectRandomLegalWord();
    }

    /**
     * Selects a random legal word from the wordle legal word list.
     * 
     * @return a random Wordle word
     */
    private String selectRandomLegalWord() {
        return removeWord(POSSIBLE_WORDS);
    }

    /**
     * Removes a random word to be guessed from POSSIBLE_WORDS in O(1)
     * 
     * @return the removed word
     */
    public static String removeWord(List<String> words) {
        int n = words.size();
        int randomIndex = random.nextInt(n);
        int lastIndex = n - 1;

        Collections.swap(words, randomIndex, lastIndex);
        return words.remove(lastIndex);
    }

    @Override
    public void reset() {
        POSSIBLE_WORDS = new ArrayList<>(dictionary.getAnswerWordsList());
        random = new Random();
    }

}
