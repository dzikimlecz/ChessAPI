package me.dzikimlecz.chessapi;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;

public interface ChessEventListener {
	void onMate(Color winner);
	void onDraw(DrawReason reason);
	default void onMoveHandled() {}
	default void onCheck(Color checked) {}
	default void onIllegalMove() {}
	default boolean onDrawRequest(Color requestor) {return false;}
	Class<? extends Piece> onPawnExchange();
}
