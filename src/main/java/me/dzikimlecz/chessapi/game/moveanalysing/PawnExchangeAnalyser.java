package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.Pawn;
import me.dzikimlecz.chessapi.game.movestoring.GameState;
import me.dzikimlecz.chessapi.game.movestoring.MoveData;

public class PawnExchangeAnalyser implements IMoveAnalyser {
	private GameState gameState;

	@Override
	public MoveData analyse(MoveData data) {
		var variations = data.getVariations();
		var pieces = variations.keySet();
		if (pieces.size() != 1) return data;
		var piece = pieces.stream().findFirst().get();
		var square = variations.get(piece);
		if (!(piece instanceof Pawn)) return data;
		int endRow = (piece.color() == Color.WHITE) ? 8 : 1;
		if (square.row() == endRow) data.setNotation(data.notation() + ":exchange");
		return data;
	}
}
