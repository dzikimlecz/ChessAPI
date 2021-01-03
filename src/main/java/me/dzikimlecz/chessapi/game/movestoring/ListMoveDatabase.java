package me.dzikimlecz.chessapi.game.movestoring;

import me.dzikimlecz.chessapi.game.board.pieces.Pawn;
import me.dzikimlecz.chessapi.game.board.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.dzikimlecz.chessapi.game.board.Color.BLACK;
import static me.dzikimlecz.chessapi.game.board.Color.WHITE;

public class ListMoveDatabase implements MoveDatabase {

	private final List<MoveData> whiteMoves;
	private final List<MoveData> blackMoves;
	private Color turnColor;
	private int movesWithoutPawnCount;

	public ListMoveDatabase() {
		turnColor = WHITE;
		whiteMoves = new ArrayList<>();
		blackMoves = new ArrayList<>();
	}

	@Override
	public MoveData getLastMove() {
		List<MoveData> list = (turnColor == BLACK) ? whiteMoves : blackMoves;
		return list.get(list.size() - 1);
	}

	@Override
	public MoveData getLastMove(Color color) {
		List<MoveData> list = (color == WHITE) ? whiteMoves : blackMoves;
		return list.get(list.size() - 1);
	}

	@Override
	public Stream<MoveData> stream(Color color) {
		return (color == WHITE) ? whiteMoves.stream() : blackMoves.stream();
	}

	@Override
	public Color turnColor() {
		return turnColor;
	}

	@Override
	public List<MoveData[]> getAllMoves() {
		List<MoveData[]> moves = new ArrayList<>();
		for (int i = 0; i < whiteMoves.size(); i++) {
			var line = new MoveData[2];
			line[0] = MoveData.copyOf(whiteMoves.get(i));
			try {
				line[1] = MoveData.copyOf(blackMoves.get(i));
			} catch(Exception ignored) {}
			moves.add(line);
		}
		return moves;
	}

	@Override
	public List<MoveData> getAllMoves(Color color) {
		var moves = color == WHITE ? whiteMoves : blackMoves;
		return moves.stream().map(MoveData::copyOf).collect(Collectors.toList());
	}

	@Override
	public void put(MoveData data) {
		if (data.color() != turnColor)
			throw new IllegalStateException("Data turn color does not match expected move color.");
		List<MoveData> list = (turnColor == WHITE) ? whiteMoves : blackMoves;
		list.add(data);
		turnColor = turnColor.opposite();
		if (data.getVariations().keySet().stream().anyMatch(e -> e instanceof Pawn))
			movesWithoutPawnCount = 0;
		else movesWithoutPawnCount++;
	}

	@Override
	public int movesWithoutPawnCount() {
		return movesWithoutPawnCount;
	}
}
