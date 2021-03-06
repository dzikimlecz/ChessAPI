package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.square.Square;


public class PawnExchangeProcessor implements IPawnExchangeProcessor {
	@Override
	public void exchange(Class<? extends ChessPiece> type, Color color, Square square) {
		try {
			square.putPiece(null);
			type.getConstructor(Color.class, Square.class).newInstance(color, square);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
