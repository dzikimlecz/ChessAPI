package me.dzikimlecz.chessapi.manager;

import me.dzikimlecz.chessapi.ChessEventListener;
import me.dzikimlecz.chessapi.GameInfo;
import me.dzikimlecz.chessapi.game.ChessGame;
import me.dzikimlecz.chessapi.game.events.ChessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class DefaultGamesManager<K> extends SkeletalGamesManager<K> {
	protected final Map<ChessGame, GameInfo<K, ?>> gameInfoMap;
	private final ExecutorService executor;

	DefaultGamesManager() {
		gameInfoMap = new HashMap<>();
		executor = Executors.newFixedThreadPool(6);
	}

	@Override public ChessGame newGame(K gameKey, ChessEventListener listener) {
		var game = super.newGame(gameKey, listener);
		executor.execute(game);
		return game;
	}

	@Override public void move(K gameKey, String notation) {
		var game = getGame(gameKey);
		var filteredNotation = notation.replaceAll("[^\\S0-9]", "");
		try {
			var chessEvent = new ChessEvent(filteredNotation);
			game.handleEvent(chessEvent);
		} catch(Exception e) {
			throw new IllegalArgumentException("Illegal notation", e);
		}
	}

	@Override public void attachInfo(GameInfo<K, ?> info) {
		gameInfoMap.put(getGame(info.getKey()), info);
	}

	@Override public GameInfo<K, ?> getInfo(K gameKey) {
		return gameInfoMap.get(getGame(gameKey));
	}

	@Override public void shutdown() {
		executor.shutdownNow();
	}
}
