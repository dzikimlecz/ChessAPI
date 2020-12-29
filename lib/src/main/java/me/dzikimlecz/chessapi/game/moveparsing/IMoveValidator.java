package me.dzikimlecz.chessapi.game.moveparsing;

import me.dzikimlecz.chessapi.game.movestoring.MoveData;

public interface IMoveValidator {
	MoveData validate(MoveData moveData);

}
