package no.uib.inf102.wordle.controller.AI;

import java.util.*;
import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.model.word.WordleAnswer;

public class MyStrategy extends FrequencyStrategy {

    private static final int EXPLOIT_THRESHOLD = 12;
    private static final int TOP_GUESSES_LIMIT = 40;
    private static final int ENTROPY_SAMPLE_SIZE = 150;

    public MyStrategy(Dictionary dictionary) {
        super(dictionary);
        reset();
    }

    @Override
    public String makeGuess(WordleWord feedback) {
        if (feedback != null) {
            currentWords.eliminateWords(feedback);
        }

        int m = currentWords.size();
        if (m == 0) {
            throw new IllegalStateException("No candidates left");
        }
        if (m == 1) {
            return currentWords.possibleAnswers().get(0);
        }
        if (m <= EXPLOIT_THRESHOLD) {
            return super.makeGuess(null);
        }
        List<String> candidates = currentWords.possibleAnswers();
        int wordLength = currentWords.wordLength();

        List<String> guesses = (dictionary.getGuessWordsList() != null)
                ? dictionary.getGuessWordsList()
                : candidates;

        boolean[] allowed = allowedLettersMask(candidates);
        guesses = filterByAllowedLetters(guesses, allowed);

        List<Map<Character, Integer>> posCounts = buildPositionCounts(candidates, wordLength);
        int[] letterPresence = buildLetterPresenceCounts(candidates);
        List<Map<Character, Double>> posEntropy = new ArrayList<>(wordLength);
        double[] letterEntropy = new double[26];
        for (int i = 0; i < wordLength; i++) {
            Map<Character, Integer> counts = posCounts.get(i);
            Map<Character, Double> entropyByLetter = new HashMap<>(counts.size());
            for (Map.Entry<Character, Integer> e : counts.entrySet()) {
                double probability = e.getValue() / (double) m;
                entropyByLetter.put(e.getKey(), probability * (1 - probability));
            }
            posEntropy.add(entropyByLetter);
        }
        for (int i = 0; i < 26; i++) {
            double probability = letterPresence[i] / (double) m;
            letterEntropy[i] = probability * (1 - probability);
        }

        double posWeight = (m > 50) ? 0.50 : 0.65;
        List<String> topGuesses = rankTopK(guesses, posEntropy, letterEntropy, posWeight, wordLength, m,
                TOP_GUESSES_LIMIT);

        String chosen = pickByBucketEntropy(topGuesses, candidates, ENTROPY_SAMPLE_SIZE);

        if (m <= 3 && !candidates.contains(chosen))
            return candidates.get(0);
        return chosen;
    }

    private boolean[] allowedLettersMask(List<String> candidates) {
        boolean[] allowed = new boolean[26];
        for (String candidate : candidates)
            for (int i = 0; i < candidate.length(); i++) {
                int pos = candidate.charAt(i) - 'a';
                if (0 <= pos && pos < 26) {
                    allowed[pos] = true;
                }
            }
        return allowed;
    }

    private List<String> filterByAllowedLetters(List<String> guesses, boolean[] allowed) {
        ArrayList<String> filtered = new ArrayList<>(guesses.size());
        for (String g : guesses) {
            boolean ok = true;
            for (int i = 0; i < g.length(); i++) {
                int idx = g.charAt(i) - 'a';
                if (0 <= idx && idx < 26 && !allowed[idx]) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                filtered.add(g);
            }
        }
        return filtered;
    }

    private List<String> rankTopK(List<String> guessPool, List<Map<Character, Double>> positionEntropy,
            double[] globalEntropy,
            double localWeight, int wordLength, int m, int maxResults) {
        ArrayList<Map.Entry<String, Double>> scoredGuesses = new ArrayList<>(guessPool.size());
        for (String guess : guessPool) {
            boolean[] seen = new boolean[26];
            int uniqueCount = 0;
            double posScore = 0, letterScore = 0;
            for (int pos = 0; pos < wordLength; pos++) {
                char letter = guess.charAt(pos);
                int idx = letter - 'a';
                if (0 <= idx && idx < 26) {
                    Double contrib = positionEntropy.get(pos).get(letter);
                    if (contrib != null) {
                        posScore += contrib;
                    }
                    if (!seen[idx]) {
                        seen[idx] = true;
                        uniqueCount++;
                        letterScore += globalEntropy[idx];
                    }
                }
            }
            double duplicatePenalty = (guess.length() - uniqueCount) * (m > 25 ? 2.0 : 1.0);
            double score = localWeight * posScore + (1 - localWeight) * letterScore - duplicatePenalty;
            scoredGuesses.add(Map.entry(guess, score));
        }
        scoredGuesses.sort((entryA, entryB) -> Double.compare(entryB.getValue(), entryA.getValue()));
        int limit = Math.min(maxResults, scoredGuesses.size());
        ArrayList<String> topGuesses = new ArrayList<>(limit);
        for (int idx = 0; idx < limit; idx++)
            topGuesses.add(scoredGuesses.get(idx).getKey());
        return topGuesses;
    }

    private String pickByBucketEntropy(List<String> guesses, List<String> answers, int cap) {
        int answerCount = answers.size();
        int step = (answerCount > cap) ? Math.max(1, answerCount / cap) : 1;
        String bestGuess = guesses.get(0);
        double bestEntropy = -1.0;
        double bestExpectedRemaining = Double.POSITIVE_INFINITY;
        final double INV_LN2 = 1.0 / Math.log(2);

        for (String guess : guesses) {
            HashMap<WordleWord, Integer> buckets = new HashMap<>();
            for (int i = 0; i < answerCount; i += step) {
                String answer = answers.get(i);
                WordleWord pattern = WordleAnswer.matchWord(guess, answer);
                buckets.merge(pattern, 1, Integer::sum);
            }
            double entropy = 0.0;
            long sumSquares = 0;
            int totalSamples = 0;
            for (int count : buckets.values()) {
                totalSamples += count;
            }
            for (int count : buckets.values()) {
                double probability = count / (double) totalSamples;
                entropy += -probability * Math.log(probability) * INV_LN2;
                sumSquares += 1L * count * count;
            }
            double expectedRemain = sumSquares / (double) totalSamples;
            if (entropy > bestEntropy
                    || (entropy == bestEntropy && (expectedRemain < bestExpectedRemaining
                            || (expectedRemain == bestExpectedRemaining && answers.contains(guess))))) {
                bestEntropy = entropy;
                bestExpectedRemaining = expectedRemain;
                bestGuess = guess;
            }
        }
        return bestGuess;
    }

    private int[] buildLetterPresenceCounts(List<String> words) {
        int[] counts = new int[26];
        for (String word : words) {
            boolean[] seen = new boolean[26];
            for (int c = 0; c < word.length(); c++) {
                int index = word.charAt(c) - 'a';
                if (0 <= index && index < 26 && !seen[index]) {
                    seen[index] = true;
                    counts[index]++;
                }
            }
        }
        return counts;
    }
}
