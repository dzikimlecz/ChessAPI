package me.dzikimlecz.chessapi;

import me.dzikimlecz.chessapi.game.board.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static me.dzikimlecz.chessapi.game.board.Color.WHITE;

public class GameInfo<Key, Player> {
	private final Key key;
	private final Player whitePlayer;
	private final Player blackPlayer;
	private @Nullable Player winner;
	private @Nullable Player loser;

	public GameInfo(@NotNull Key key, Player whitePlayer, Player blackPlayer) {
		this.key = key;
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
	}

	public final Key getKey() {
		return key;
	}

	public void setWinner(@NotNull Color color) {
		if (color == WHITE) {
			winner = whitePlayer;
			loser = blackPlayer;
		} else {
			winner = blackPlayer;
			loser = whitePlayer;
		}
	}

	@NotNull public Player getWinner() {
		return Objects.requireNonNull(winner, "Winner not set");
	}

	@NotNull public Player getLoser() {
		return Objects.requireNonNull(loser, "Loser not set");
	}

	public final Player getPlayer(@NotNull Color color) {
		return (color == WHITE) ? whitePlayer : blackPlayer;
	}
}
