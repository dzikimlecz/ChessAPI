package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.IMoveProcessor;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;

public interface IPawnExchangeProcessor extends IMoveProcessor {
	void exchange(Class<? extends Piece> type, Color color, Square square);
}
