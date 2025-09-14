package no.uib.inf102.wordle.controller.AI;


import no.uib.inf102.wordle.model.Dictionary;

import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.model.word.WordleWordList;

public class MyStrategy implements IStrategy {

    private Dictionary dictionary;
    protected WordleWordList currentWords;
    
    public MyStrategy(Dictionary dictionary) {
        this.dictionary = dictionary;
        reset();
    }

    @Override
    public String makeGuess(WordleWord feedback) {
        if (feedback != null) {
            currentWords.eliminateWords(feedback);
        }

        //TODO: improve this
        return currentWords.possibleAnswers().get(0);
    }


    @Override
    public void reset() {
        currentWords = new WordleWordList(dictionary);
    }
    
}
