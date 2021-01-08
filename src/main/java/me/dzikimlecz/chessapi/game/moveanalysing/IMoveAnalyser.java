package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.IMoveProcessor;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;

public interface IMoveAnalyser extends IMoveProcessor {

	MoveData analyse(MoveData data);
	default void setValidator(IMoveValidator validator) {}
}
