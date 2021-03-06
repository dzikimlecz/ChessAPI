package me.dzikimlecz.chessapi.game.movestoring;

import me.dzikimlecz.chessapi.game.board.square.Color;

import java.util.List;
import java.util.stream.Stream;

public interface MoveDatabase {
	MoveData getLastMove();
	MoveData getLastMove(Color color);

	Stream<MoveData> stream(Color color);

	Color turnColor();

	List<MoveData[]> getAllMoves();
	List<MoveData> getAllMoves(Color color);

	void put(MoveData data);
	int movesWithoutPawnCount();
}
