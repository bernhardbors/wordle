package no.uib.inf102.wordle.controller;

import java.awt.event.KeyEvent;

import javax.swing.Timer;

import no.uib.inf102.wordle.controller.AI.*;
import no.uib.inf102.wordle.model.Dictionary;
import no.uib.inf102.wordle.model.GameState;
import no.uib.inf102.wordle.model.word.WordleWord;
import no.uib.inf102.wordle.view.gameView.WordleView;

import java.awt.event.ActionEvent;

public class WordleAIController extends WordleController {

    private IStrategy AI;
    private WordleWord feedback;

    private Timer timer;

    public WordleAIController(ControllableWordleModel model, WordleView view) {
        super(model, view);

        Dictionary dictionary = model.getDictionary();
        this.timer = new Timer(model.getTimerDelay(), this::clockTick);
        this.AI = new RandomStrategy(dictionary);
        //this.AI = new EliminateStrategy(dictionary);
        //this.AI = new FrequencyStrategy(dictionary);
        //this.AI = new MyStrategy(dictionary);

        view.addKeyListener(this);
        view.setFocusable(true);

        this.timer.start();
    }

    public void clockTick(ActionEvent e) {
        if (model.getGameState() == GameState.GAME_OVER)
            return;

        String guess = AI.makeGuess(feedback);
        for (int i = 0; i < guess.length(); i++) {
            char c = guess.charAt(i);
            model.addCharacter(c);
        }
        feedback = model.makeGuess();
        // Stop guessing if it was correct
        if (feedback.allMatch())
            timer.stop();

        timer.setDelay(model.getTimerDelay());
        model.clockTick();
        view.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_1) {
            AI.reset();
            model.reset();
            feedback = null;
            this.timer.start();
            view.repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

}
