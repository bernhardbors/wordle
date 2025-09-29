package no.uib.inf102.wordle.controller.AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.model.word.WordleWordList;

/**
 * This strategy finds the word within the possible words which has the highest
 * expected
 * number of green matches.
 */
public class FrequencyStrategy implements IStrategy {

    protected Dictionary dictionary;
    protected WordleWordList currentWords;

    public FrequencyStrategy(Dictionary dictionary) {
        this.dictionary = dictionary;
        reset();
    }

    @Override
    public String makeGuess(WordleWord feedback) {
        if (feedback != null) {
            currentWords.eliminateWords(feedback);
        }

        List<String> candidates = currentWords.possibleAnswers();
        int n = currentWords.size();

        if (n == 0) {
            throw new IllegalStateException("No possible answers remain");
        }
        if (n == 1) {
            return candidates.get(0);
        }

        int wordLength = currentWords.wordLength();
        List<Map<Character, Integer>> positionCounts = buildPositionCounts(candidates, wordLength);

        String best = candidates.get(0);
        double bestScore = -1.0;

        for (String guess : candidates) {
            double expected = expectedGreens(guess, positionCounts, n);
            if (expected > bestScore) {
                bestScore = expected;
                best = guess;
            }
        }
        return best;
    }



    @Override
    public void reset() {
        currentWords = new WordleWordList(dictionary);
    }

    protected List<Map<Character, Integer>> buildPositionCounts(List<String> candidates, int wordLength) {
        List<Map<Character, Integer>> counts = new ArrayList<>(wordLength);
        for (int i = 0; i < wordLength; i++) {
            counts.add(new HashMap<>());
        }
        for (String word : candidates) {
            for (int i = 0; i < wordLength; i++) {
                char c = word.charAt(i);
                if (c < 'a' || c > 'z') {
                    continue;
                }
                Map<Character, Integer> positionMap = counts.get(i);
                positionMap.put(c, positionMap.getOrDefault(c, 0) + 1);
            }
        }
        return counts;
    }

    private double expectedGreens(String guess, List<Map<Character, Integer>> positionCounts, int totalWords) {
        double expected = 0.0;
        int limit = Math.min(guess.length(), positionCounts.size());
        for (int i = 0; i < limit; i++) {
            char c = guess.charAt(i);
            if (c < 'a' || c > 'z') {
                continue;
            }
            expected += positionCounts.get(i).getOrDefault(c, 0);
        }
        return expected / totalWords;
    }
}