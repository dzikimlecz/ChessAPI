package me.dzikimlecz.chessapi;

import me.dzikimlecz.chessapi.game.board.Color;

public interface ChessEventListener {
	void onMate(Color winner);
	void onDraw(DrawReason reason);
	default void onMoveHandled() {}
	default void onCheck(Color checked) {}
	default void onIllegalMove() {}
}
