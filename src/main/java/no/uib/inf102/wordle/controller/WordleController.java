package no.uib.inf102.wordle.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import no.uib.inf102.wordle.view.gameView.WordleView;

public abstract class WordleController implements KeyListener {

    protected ControllableWordleModel model;
    protected WordleView view;

    public WordleController(ControllableWordleModel model, WordleView view) {
        this.model = model;
        this.view = view;

        view.addKeyListener(this);
        view.setFocusable(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
