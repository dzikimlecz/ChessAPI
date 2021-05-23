package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.IMoveProcessor;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.movestoring.MoveDatabase;

import java.util.Optional;

public interface IDrawAnalyser extends IMoveProcessor {
	Optional<DrawReason> lookForDraw();

	default void setMoveDatabase(MoveDatabase moveDatabase) {}

	default void setValidator(IMoveValidator validator) {}
}
