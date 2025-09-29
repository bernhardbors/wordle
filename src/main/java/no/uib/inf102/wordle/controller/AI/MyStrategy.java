package no.uib.inf102.wordle.controller.AI;

import java.util.*;
import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.model.word.WordleAnswer;
import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.model.word.WordleWordList;

public class MyStrategy implements IStrategy {

    private final Dictionary dictionary;
    private WordleWordList currentWords;
    private final FrequencyStrategy freqStrategy;

    private static final int PARTITION_SWITCH = 150;
    private static final int FREQ_SWITCH = 10;
    private static final int PROBE_K = 40;
    private static final double POS_WEIGHT = 0.3;

    public MyStrategy(Dictionary dictionary) {
        this.dictionary = dictionary;
        this.freqStrategy = new FrequencyStrategy(dictionary);
        reset();
    }

    @Override
    public String makeGuess(WordleWord feedback) {
        if (feedback != null) {
            currentWords.eliminateWords(feedback);
            freqStrategy.currentWords.eliminateWords(feedback);
        }

        List<String> possibleAnswers = currentWords.possibleAnswers();
        int n = currentWords.size();
        if (n == 0)
            throw new IllegalStateException("No possible answers remain");
        if (n == 1)
            return possibleAnswers.get(0);
        if (n <= FREQ_SWITCH) {
            return freqStrategy.makeGuess(null);
        }
        if (n <= PARTITION_SWITCH) {
            List<String> pool = buildGuessPool(possibleAnswers, PROBE_K);
            return bestByPartitionOverPool(possibleAnswers, pool);
        } else {
            return bestByDistinctLetterFreq(possibleAnswers);
        }
    }

    @Override
    public void reset() {
        currentWords = new WordleWordList(dictionary);
        freqStrategy.reset();
    }

    private String bestByDistinctLetterFreq(List<String> candidates) {
        int[] letterCounts = buildLetterCounts(candidates);
        List<Map<Character, Integer>> posCounts = freqStrategy.buildPositionCounts(candidates,
                currentWords.wordLength());

        List<String> pool = dictionary.getGuessWordsList();

        String best = pool.get(0);
        double bestScore = scoreHybrid(best, letterCounts, posCounts);

        for (int i = 1; i < pool.size(); i++) {
            String w = pool.get(i);
            double s = scoreHybrid(w, letterCounts, posCounts);
            if (s > bestScore) {
                bestScore = s;
                best = w;
            }
        }
        return best;
    }

    private double scoreHybrid(String word, int[] letterCounts, List<Map<Character, Integer>> posCounts) {
        boolean[] seen = new boolean[26];
        int wordLength = word.length();
        int sumLetters = 0;
        int sumPos = 0;

        for (int i = 0; i < wordLength; i++) {
            char c = word.charAt(i);
            if (c < 'a' || c > 'z')
                continue;
            int idx = c - 'a';

            if (!seen[idx]) {
                seen[idx] = true;
                sumLetters += letterCounts[idx];
            }

            sumPos += posCounts.get(i).getOrDefault(c, 0);
        }
        return (sumLetters + POS_WEIGHT * sumPos) / Math.max(1, wordLength);
    }

    private int[] buildLetterCounts(List<String> words) {
        int[] counts = new int[26];
        for (String w : words) {
            for (int i = 0; i < w.length(); i++) {
                char c = w.charAt(i);
                if (c >= 'a' && c <= 'z')
                    counts[c - 'a']++;
            }
        }
        return counts;
    }

    private double scoreDistinct(String w, int[] counts) {
        boolean[] seen = new boolean[26];
        int sum = 0;
        for (int i = 0; i < w.length(); i++) {
            char c = w.charAt(i);
            if (c < 'a' || c > 'z')
                continue;
            int idx = c - 'a';
            if (!seen[idx]) {
                seen[idx] = true;
                sum += counts[idx];
            }
        }
        return (double) sum / Math.max(1, w.length());
    }

    private List<String> buildGuessPool(List<String> candidates, int k) {
        LinkedHashSet<String> pool = new LinkedHashSet<>(candidates);
        int[] counts = buildLetterCounts(candidates);
        List<String> allGuesses = new ArrayList<>(dictionary.getGuessWordsList());

        Map<String, Double> score = new HashMap<>(allGuesses.size());
        for (String w : allGuesses) {
            score.put(w, scoreDistinct(w, counts));
        }

        allGuesses.sort((a, b) -> Double.compare(score.get(b), score.get(a)));

        int added = 0;
        for (String w : allGuesses) {
            if (pool.add(w)) {
                if (++added >= k)
                    break;
            }
        }
        return new ArrayList<>(pool);
    }

    private String bestByPartitionOverPool(List<String> answers, List<String> guessPool) {
        int n = answers.size();
        String best = guessPool.get(0);
        double bestExpected = Double.POSITIVE_INFINITY;
        int bestWorst = Integer.MAX_VALUE;

        for (String guess : guessPool) {
            Map<WordleWord, Integer> buckets = new HashMap<>();
            for (String ans : answers) {
                WordleWord fb = WordleAnswer.matchWord(guess, ans);
                buckets.put(fb, buckets.getOrDefault(fb, 0) + 1);
            }
            double expected = 0.0;
            int worst = 0;
            for (int size : buckets.values()) {
                double p = (double) size / n;
                expected += p * size;
                worst = Math.max(worst, size);
            }
            if (expected < bestExpected || (expected == bestExpected && worst < bestWorst)) {
                bestExpected = expected;
                bestWorst = worst;
                best = guess;
            }
        }
        return best;

    }
}
