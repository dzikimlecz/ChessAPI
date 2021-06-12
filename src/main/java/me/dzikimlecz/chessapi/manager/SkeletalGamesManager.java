package me.dzikimlecz.chessapi.manager;

import me.dzikimlecz.chessapi.ChessEventListener;
import me.dzikimlecz.chessapi.game.ChessGame;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.events.ChessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SkeletalGamesManager<K> implements GamesManager<K> {
    protected final Map<K, ChessGame> games;

    protected SkeletalGamesManager() {
        games = new HashMap<>();
    }

    @Override public ChessGame newGame(K gameKey, ChessEventListener listener) {
        var game = new ChessGame(listener);
        if (games.containsKey(gameKey) && games.get(gameKey).isOngoing())
            throw new IllegalStateException("Game on this gameKey is already ongoing");
        games.put(gameKey, game);
        return game;
    }

    @Override public void forceClose(K gameKey) {
        getGame(gameKey).stopGame();
        games.remove(gameKey);
    }

    @Override public boolean close(K gameKey) {
        var ongoing = getGame(gameKey).isOngoing();
        if (!ongoing)
            forceClose(gameKey);
        return !ongoing;
    }

    @Override public List<List<ChessPiece>> read(K gameKey) {
        var pieces = new ArrayList<List<ChessPiece>>(8);
        ChessGame game = games.get(gameKey);
        if (game != null) {
            var board = game.board();
            for (int row = 1; row <= 8; row++) {
                pieces.add(new ArrayList<>(8));
                for (char line = 'a'; line <= 'h'; line++)
                    pieces.get(row - 1).add(board.square(line, row).piece());
            }
        }
        return pieces;
    }

    @Override public void requestDraw(K gameKey, Color requester) {
        var game = getGame(gameKey);
        try {
            game.handleEvent(new ChessEvent("draw" + requester.name().toLowerCase()));
        } catch(InterruptedException e) {
            game.listener().onIllegalMove();
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


    @Override public ChessEventListener getListener(K gameKey) {
        return getGame(gameKey).listener();
    }

    @Override public Color getTurn(K gameKey) {
        return getGame(gameKey).color();
    }
}
