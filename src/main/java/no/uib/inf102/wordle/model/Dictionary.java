package no.uib.inf102.wordle.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.uib.inf102.wordle.resources.LoadFromFile5LetterEnglish;

public class Dictionary {

    private List<String> guessWordsList;
    private List<String> answerWordsList;

    private Set<String> guessWordsSet;
    private Set<String> answerWordsSet;

    public final int WORD_LENGTH;

    public Dictionary() {
        this(LoadFromFile5LetterEnglish.GUESS_WORDS_LIST, LoadFromFile5LetterEnglish.ANSWER_WORDS_LIST);
    }

    public Dictionary(List<String> guessWords, List<String> answerWords) {
        this.guessWordsList = guessWords;
        this.answerWordsList = answerWords;

        this.guessWordsSet = new HashSet<String>(guessWords);
        this.answerWordsSet = new HashSet<String>(answerWords);
        
        // Assumes that all words in list are the same size
        this.WORD_LENGTH = guessWordsList.get(0).length();
        checkWordLength(guessWordsList);
        checkWordLength(answerWordsList);
    }

	private void checkWordLength(List<String> words) {
		for(String word : words) {
        	if(word.length()!=WORD_LENGTH)
        		throw new IllegalArgumentException("All words in dictionary must have same length, expected: "+WORD_LENGTH+" but found: "+word.length());
        }
	}

    public List<String> getGuessWordsList() {
        return Collections.unmodifiableList(guessWordsList);
    }

    public List<String> getAnswerWordsList() {
        return Collections.unmodifiableList(answerWordsList);
    }

    /**
     * Checks if the given wordGuess is part of the set of all Wordle words.
     * @param wordGuess
     * @return
     */
	public boolean isLegalGuess(String wordGuess) {
		return guessWordsSet.contains(wordGuess.toLowerCase());
	}

	/**
     * Checks if the given wordGuess is part of the set of all Wordle words.
     * @param wordGuess
     * @return
     */
	public boolean isLegalAnswer(String wordGuess) {
		return answerWordsSet.contains(wordGuess.toLowerCase());
	}

}
