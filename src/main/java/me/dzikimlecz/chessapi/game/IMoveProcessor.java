package me.dzikimlecz.chessapi.game;

import me.dzikimlecz.chessapi.game.movestoring.GameState;

public interface IMoveProcessor {
	default void setGameState(GameState state) {}
}
