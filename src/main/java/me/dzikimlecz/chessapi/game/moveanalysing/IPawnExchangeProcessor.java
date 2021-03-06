package me.dzikimlecz.chessapi.game.moveanalysing;

import me.dzikimlecz.chessapi.game.IMoveProcessor;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.square.Square;

public interface IPawnExchangeProcessor extends IMoveProcessor {
	void exchange(Class<? extends ChessPiece> type, Color color, Square square);
}
