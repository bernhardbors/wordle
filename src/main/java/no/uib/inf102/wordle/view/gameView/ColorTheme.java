package no.uib.inf102.wordle.view.gameView;

import java.awt.Color;

import no.uib.inf102.wordle.model.word.AnswerType;

public interface ColorTheme {
  
  Color getCellColor(AnswerType ansType);
  
  Color getFrameColor();

  Color getBackgroundColor();
  
}
