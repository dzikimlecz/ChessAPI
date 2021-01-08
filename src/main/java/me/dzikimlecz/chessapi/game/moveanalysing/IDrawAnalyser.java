package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.IMoveProcessor;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.MoveDatabase;

public interface IDrawAnalyser extends IMoveProcessor {
	DrawReason lookForDraw();

	default void setMoveDatabase(MoveDatabase moveDatabase) {}

	default void setValidator(IMoveValidator validator) {}
}
