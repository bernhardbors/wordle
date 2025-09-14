package no.uib.inf102.wordle.controller.AI;

import java.util.List;
import java.util.Map;

import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.resources.LoadFromFile5LetterEnglish;

public class Performance_ill {
	
	public static void main(String[] args) {
	    Dictionary dictionary = new Dictionary(LoadFromFile5LetterEnglish.GUESS_WORDS_LIST,LoadFromFile5LetterEnglish.ILL_WORDS_LIST);

		List<IStrategy> strategies = AIPerformance.getStrategies(dictionary);

	    Map<IStrategy, AIStatistics> stats = AIPerformance.runAll(strategies,dictionary);

	    AIPerformance.printAllResults(strategies, stats);		
	}
}
