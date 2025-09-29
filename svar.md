# Runtime Analysis
For each method of the tasks give a runtime analysis in Big-O notation and a description of why it has this runtime.

**If you have implemented new methods not listed you must add these as well, e.g. any helper methods. You need to show how you analyzed any methods used by the methods listed below.**

The runtime should be expressed using these three parameters:
   * `n` - number of words in the list allWords
   * `m` - number of words in the list possibleWords
   * `k` - number of letters in the wordleWords


## Task 1 - matchWord
* `WordleAnswer::matchWord`: O(k)
    * For this method the only parameter that matters is k, as we do not search through allWords or possible words. We loop through the letters in the guess word twice, but searching through a HashMap usually uses O(1), so the total runtime will be O(k). 

## Task 2 - EliminateStrategy
* `WordleWordList::eliminateWords`: O(mk)
    * eliminateWords loops through the m possible answers. For each word it calls WordleWord.isPossibleWord, which in turn calls WordleAnswer.matchWord taking O(k). Multiplying gives O(mk). Adding to the survivors list takes O(1) and does not affect Big O.

## Task 3 - FrequencyStrategy
* `FrequencyStrategy::makeGuess`: O(mk + mk) = O(mk)
    * The method builds positional frequency tables by scanning all m survivors across k slots (O(mk)). It then evaluates each candidate with expectedGreens, which scans the k slots per word, giving another O(mk). Summing constants gives O(mk).



# Task 4 - Make your own (better) AI
For this task you do not need to give a runtime analysis. 
Instead, you must explain your code. What was your idea for getting a better result? What is your strategy?

*My strategy improves Frequency Strategy by dividing a game into three different phases, with the aim of gaining as much information as possible each guess.
When the list of candidates is still large, it chooses a "probe"-word, (not necessarily valid answers), chosen by a hybrid score that rewards distinct-letter coverage and positional frequencies. This way, I avoid duplicate letters which provide little information and exclude wrong answers more rapidly.
When the possibleAnswers list reaches a given size (for example 150), I switch to partitioning over a chosen pool of candidates and highly scored probe-words, and pick the guess that minimizes the expected remaining candidates, with a worst-case tie-break.
When just a few candidates are left (for example 10), I use composition to switch to FrequencyStrategy to finish quickly.
Parameters like thresholds, pool-size and weight between letter- and positional information controls the balance between speedy and accurate guesses.
The result is usually fewer and equally precise guesses as FrequencyStrategy for performance5letters, and considerably better for performance_ill.*
