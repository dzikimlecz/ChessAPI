package me.dzikimlecz.chessapi;

import me.dzikimlecz.chessapi.game.ChessGame;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.events.ChessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GamesManager<K> {
	protected Map<K, ChessGame> games;
	protected Map<K, Future<?>> futures;
	protected Map<ChessGame, GameInfo<K, ?>> gameInfoMap;
	private final ExecutorService executor;

	public GamesManager() {
		games = new HashMap<>();
		gameInfoMap = new HashMap<>();
		executor = Executors.newCachedThreadPool();
	}

	public void newGame(K gameKey, ChessEventListener listener) {
		var game = new ChessGame(listener);
		if (games.containsKey(gameKey) && games.get(gameKey).isOngoing())
			throw new IllegalStateException("Game on this gameKey is already ongoing");
		futures.put(gameKey, executor.submit(game));
		games.put(gameKey, game);

	}

	public void forceClose(K gameKey) {
		var game = getGame(gameKey);
		game.stopGame();
		games.remove(gameKey);
	}

	public boolean close(K gameKey) {
		ChessGame game = getGame(gameKey);
		if (game.isOngoing()) return false;
		forceClose(gameKey);
		return true;
	}

	public void move(K gameKey, String notation) {
		var game = getGame(gameKey);
		notation = notation.replaceAll("[^\\S0-9]", "");
		try {
			var chessEvent = new ChessEvent(notation);
			game.handleEvent(chessEvent);
		} catch(Exception e) {
			throw new IllegalArgumentException("Illegal notation");
		}
	}

	@NotNull
	protected ChessGame getGame(K gameKey) {
		var noGameException = new IllegalArgumentException(
				"There is no game corresponding to gameKey: " + gameKey.toString()
		);
		var game = games.computeIfAbsent(gameKey, gameKey1 -> {
			throw noGameException;
		});
		if (game.isOngoing()) return game;
		games.remove(gameKey);
		throw noGameException;
	}

	@Deprecated public void attachInfo(K gameKey, @NotNull GameInfo<K, ?> info) {
		this.gameInfoMap.put(getGame(gameKey), info);
	}

	public void attachInfo(GameInfo<K, ?> info) {
		gameInfoMap.put(getGame(info.getKey()), info);
	}

	public GameInfo<K, ?> getInfo(K gameKey) {
		return gameInfoMap.get(getGame(gameKey));
	}

	public List<List<ChessPiece>> read(K gameKey) {
		var pieces = new ArrayList<List<ChessPiece>>(8);
		ChessGame game = games.get(gameKey);
		if (game != null) {
			var board = game.board();
			for (int row = 1; row <= 8; row++) {
				pieces.add(new ArrayList<>());
				for (char line = 'a'; line <= 'h'; line++)
					pieces.get(row - 1).set(line - 'a', board.square(line, row).piece());
			}
		}
		return pieces;
	}

	public void requestDraw(K gameKey, Color requester) {
		var game = getGame(gameKey);
		try {
			game.handleEvent(new ChessEvent("draw" + requester.name().toLowerCase()));
		} catch(InterruptedException e) {
			game.listener().onIllegalMove();
		}
	}

	public void shutdown() {
		executor.shutdownNow();
	}
}
