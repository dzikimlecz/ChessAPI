package me.dzikimlecz.chessapi.game.moveparsing;

import me.dzikimlecz.chessapi.game.IMoveProcessor;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;
import me.dzikimlecz.chessapi.game.movestoring.MoveDatabase;

public interface IMoveValidator extends IMoveProcessor {
	MoveData validate(MoveData moveData);

	default void setMoveDatabase(MoveDatabase moveDatabase) {}
}
