package no.uib.inf102.wordle.resources;

import java.util.List;

import no.uib.inf102.util.ReadFile;

/**
 * This class contains the list of guess words and answer words in Wordle.
 */
public class LoadFromFile5LetterEnglish {

     private static final String FOLDER_PATH = "src/main/java/no/uib/inf102/wordle/resources";
     private static final String GUESS_WORDS_PATH = FOLDER_PATH + "/allWords.txt";
     private static final String ANSWER_WORDS_PATH = FOLDER_PATH + "/answerWords.txt";
     private static final String ILL_WORDS_PATH = FOLDER_PATH + "/answers_ill_.txt";
     
     /**
      * These words are words that can be guessed in a game of Wordle.
      */
     public static final List<String> GUESS_WORDS_LIST = toList(GUESS_WORDS_PATH);

     public static final List<String> ILL_WORDS_LIST = toList(ILL_WORDS_PATH);

     /**
      * These words are a limited subset of the guess words.
      * This is to make the game a little simpler so you don't have to answer very
      * obscure words.
      * All words will have a length equal to WORD_LENGTH
      */
     public static final List<String> ANSWER_WORDS_LIST = toList(ANSWER_WORDS_PATH);

     private static List<String> toList(String path) {
          return ReadFile.readLines(path);
     }

}
