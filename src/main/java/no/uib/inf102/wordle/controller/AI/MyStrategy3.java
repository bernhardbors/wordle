package no.uib.inf102.wordle.controller.AI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.model.word.WordleWordList;

public class MyStrategy3 implements IStrategy {

    private final Dictionary dictionary;
    protected WordleWordList currentWords;
    private final FrequencyStrategy finisher;

    // Terskler kan justeres litt etter datasettene dine
    private static final int PARTITION_SWITCH = 100; // bruk full partisjonering under denne
    private static final int FREQ_SWITCH = 10;       // la FrequencyStrategy avslutte

    public MyStrategy3(Dictionary dictionary) {
        this.dictionary = dictionary;
        this.finisher = new FrequencyStrategy(dictionary);
        reset();
    }

    @Override
    public String makeGuess(WordleWord feedback) {
        if (feedback != null) {
            currentWords.eliminateWords(feedback);
            finisher.makeGuess(feedback); // hold finisher i sync
        }

        List<String> C = currentWords.possibleAnswers();
        int n = currentWords.size();
        if (n == 0) throw new IllegalStateException("No possible answers remain");
        if (n == 1) return C.get(0);

        if (n <= FREQ_SWITCH) {
            return finisher.makeGuess(null);
        }

        if (n <= PARTITION_SWITCH) {
            // FULL PARTISJON: minimer forventet restmengde
            return bestByPartition(C);
        } else {
            // TIDLIG FASE: entropi-light (unik-bokstav-frekvens)
            return bestByDistinctLetterFreq(C);
        }
    }

    @Override
    public void reset() {
        currentWords = new WordleWordList(dictionary);
        finisher.reset();
    }

    /* ---------- Tidlig fase: entropi-light ---------- */
    private String bestByDistinctLetterFreq(List<String> words) {
        int[] counts = buildLetterCounts(words);
        String best = words.get(0);
        double bestScore = scoreDistinct(best, counts);
        for (int i = 1; i < words.size(); i++) {
            String w = words.get(i);
            double s = scoreDistinct(w, counts);
            if (s > bestScore) { bestScore = s; best = w; }
        }
        return best;
    }

    private int[] buildLetterCounts(List<String> words) {
        int[] counts = new int[26];
        for (String w : words) {
            for (int i = 0; i < w.length(); i++) {
                char c = w.charAt(i);
                if (c >= 'a' && c <= 'z') counts[c - 'a']++;
            }
        }
        return counts;
    }

    private double scoreDistinct(String w, int[] counts) {
        boolean[] seen = new boolean[26];
        int sum = 0;
        for (int i = 0; i < w.length(); i++) {
            char c = w.charAt(i);
            if (c < 'a' || c > 'z') continue;
            int idx = c - 'a';
            if (!seen[idx]) { seen[idx] = true; sum += counts[idx]; }
        }
        return (double) sum / Math.max(1, w.length());
    }

    /* ---------- Mellomfase: ekte partisjonering ---------- */
    private String bestByPartition(List<String> candidates) {
        // Vi bruker kandidatene både som «svar» og som «gjett»-pool.
        // Score = forventet restmengde etter gjettet: sum_b p_b * |b|, der p_b = |b|/n.
        int n = candidates.size();
        String best = candidates.get(0);
        double bestScore = Double.POSITIVE_INFINITY;

        for (String guess : candidates) {
            Map<String, Integer> buckets = new HashMap<>();
            for (String ans : candidates) {
                String key = patternKey(guess, ans);
                buckets.put(key, buckets.getOrDefault(key, 0) + 1);
            }
            double expectedRemaining = 0.0;
            for (int size : buckets.values()) {
                double p = (double) size / n;
                expectedRemaining += p * size; // = sum size^2 / n
            }
            if (expectedRemaining < bestScore) {
                bestScore = expectedRemaining;
                best = guess;
            }
        }
        return best;
    }

    /**
     * Feedback-nøkkel for (guess, answer): "2"=grønn, "1"=gul, "0"=grå.
     * To-pass Wordle-logikk for riktig håndtering av duplikater.
     */
    private String patternKey(String guess, String answer) {
        int L = answer.length();
        int[] fb = new int[L]; // 2,1,0
        int[] count = new int[26];

        for (int i = 0; i < L; i++) {
            char g = guess.charAt(i), a = answer.charAt(i);
            if (g == a) {
                fb[i] = 2;
            } else {
                if (a >= 'a' && a <= 'z') count[a - 'a']++;
            }
        }
        for (int i = 0; i < L; i++) {
            if (fb[i] == 2) continue;
            char g = guess.charAt(i);
            if (g < 'a' || g > 'z') { fb[i] = 0; continue; }
            int idx = g - 'a';
            if (count[idx] > 0) { fb[i] = 1; count[idx]--; }
            else fb[i] = 0;
        }
        StringBuilder sb = new StringBuilder(L);
        for (int v : fb) sb.append((char)('0' + v));
        return sb.toString();
    }
}
