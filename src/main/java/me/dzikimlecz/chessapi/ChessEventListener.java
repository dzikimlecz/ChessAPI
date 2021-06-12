package me.dzikimlecz.chessapi;

import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;

public interface ChessEventListener {
	void onMate(Color winner);
	void onDraw(DrawReason reason);
	default void onMoveHandled() {}
	default void onCheck(Color checked) {}
	default void onIllegalMove() {}
	default boolean onDrawRequest(Color requester) {return false;}
	Class<? extends Piece> onPawnExchange();
}
