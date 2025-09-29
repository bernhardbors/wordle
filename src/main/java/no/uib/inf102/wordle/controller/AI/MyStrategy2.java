package no.uib.inf102.wordle.controller.AI;

import java.util.*;
import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.model.word.WordleWordList;

public class MyStrategy2 implements IStrategy {

    private final Dictionary dictionary;
    protected WordleWordList currentWords;
    private final FrequencyStrategy finisher;

    private static final int PARTITION_SWITCH = 250;
    private static final int FREQ_SWITCH = 10;
    private static final int PROBE_K = 150; // antall «billig-utvalgte» probe-ord

    public MyStrategy2(Dictionary dictionary) {
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

        if (n <= FREQ_SWITCH) return finisher.makeGuess(null);
        if (n > PARTITION_SWITCH) return bestByDistinctLetterFreq(C); // tidlig fase

        // Mellomfase: full partisjonering over kandidater + smått probe-sett fra hele gjettelista
        List<String> guessPool = buildGuessPool(C);
        return bestByPartitionOverPool(C, guessPool);
    }

    @Override
    public void reset() {
        currentWords = new WordleWordList(dictionary);
        finisher.reset();
    }

    /* ---------- Probe-pool ---------- */

    private List<String> buildGuessPool(List<String> candidates) {
        // 1) Start med alle kandidater (må alltid vurderes)
        HashSet<String> poolSet = new HashSet<>(candidates);

        // 2) Hent hele gjettelista (tilpass metodenavn om nødvendig)
        List<String> guessWords;
        try {
            // Bytt til riktig metode i ditt Dictionary (f.eks. getAllWordsList)
            guessWords = dictionary.getGuessWordsList();
        } catch (Throwable t) {
            // Fallback hvis dere ikke har separat gjetteliste
            guessWords = candidates;
        }

        // 3) Billig pre-score: velg de PROBE_K beste ordene etter «unik-bokstav»-frekvens
        int[] counts = buildLetterCounts(candidates); // frekvenser fra KANDIDATER, ikke alle ord
        // Min-heap på størrelse PROBE_K
        PriorityQueue<Scored> heap = new PriorityQueue<>(Comparator.comparingDouble(s -> s.score));
        for (String w : guessWords) {
            double s = scoreDistinct(w, counts);
            if (heap.size() < PROBE_K) heap.add(new Scored(w, s));
            else if (s > heap.peek().score) { heap.poll(); heap.add(new Scored(w, s)); }
        }

        while (!heap.isEmpty()) poolSet.add(heap.poll().word);

        // 4) Returner deterministisk rekkefølge
        return new ArrayList<>(poolSet);
    }

    private static class Scored {
        final String word; final double score;
        Scored(String w, double s) { word = w; score = s; }
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

    /* ---------- Partisjonering med tidlig avbrytelse + base-3 mønster ---------- */

    private String bestByPartitionOverPool(List<String> answers, List<String> guessPool) {
        final int n = answers.size();
        String best = answers.get(0);
        long bestSumSq = Long.MAX_VALUE; // vi sammenligner sum(size^2), ikke normalisert

        for (String guess : guessPool) {
            long sumSq = 0L;
            // bucket: key = base-3 pattern (int), value = count
            HashMap<Integer, Integer> buckets = new HashMap<>();
            for (int i = 0; i < n; i++) {
                int key = patternKeyBase3(guess, answers.get(i));
                int newSize = buckets.getOrDefault(key, 0) + 1;
                // oppdater sumSq inkrementelt: (old+1)^2 - old^2 = 2*old + 1
                sumSq += (2L * (newSize - 1)) + 1L;
                buckets.put(key, newSize);
                if (sumSq >= bestSumSq) {
                    // tidlig avbrytelse: dette gjettet blir ikke bedre
                    break;
                }
            }
            if (sumSq < bestSumSq) {
                bestSumSq = sumSq;
                best = guess;
            }
        }
        return best;
    }

    // 2-pass Wordle-tilbakemelding kodet som base-3 heltall (0/1/2 per posisjon)
    private int patternKeyBase3(String guess, String answer) {
        int L = answer.length();
        int[] fb = new int[L];
        int[] count = new int[26];

        for (int i = 0; i < L; i++) {
            char g = guess.charAt(i), a = answer.charAt(i);
            if (g == a) fb[i] = 2;
            else if (a >= 'a' && a <= 'z') count[a - 'a']++;
        }
        for (int i = 0; i < L; i++) {
            if (fb[i] == 2) continue;
            char g = guess.charAt(i);
            if (g < 'a' || g > 'z') { fb[i] = 0; continue; }
            int idx = g - 'a';
            if (count[idx] > 0) { fb[i] = 1; count[idx]--; } else fb[i] = 0;
        }
        int key = 0;
        for (int i = 0; i < L; i++) key = key * 3 + fb[i];
        return key;
    }
}
