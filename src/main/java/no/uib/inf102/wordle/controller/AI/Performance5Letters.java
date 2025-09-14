package no.uib.inf102.wordle.controller.AI;

import java.util.List;
import java.util.Map;

import no.uib.inf102.wordle.model.Dictionary;

public class Performance5Letters {

	public static void main(String[] args) {
    	
        Dictionary dictionary = new Dictionary();

    	List<IStrategy> strategies = AIPerformance.getStrategies(dictionary);

        Map<IStrategy, AIStatistics> stats = AIPerformance.runAll(strategies,dictionary);

        AIPerformance.printAllResults(strategies, stats);
	}

}
