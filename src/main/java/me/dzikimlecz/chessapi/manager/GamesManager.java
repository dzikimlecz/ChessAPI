package me.dzikimlecz.chessapi.manager;

import me.dzikimlecz.chessapi.ChessEventListener;
import me.dzikimlecz.chessapi.GameInfo;
import me.dzikimlecz.chessapi.game.ChessGame;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;

import java.util.List;

public interface GamesManager<K> {
    @SuppressWarnings("all")
    static<E> GamesManager<E> newManager() {
        return new DefaultGamesManager<E>();
    }

    ChessGame newGame(K gameKey, ChessEventListener listener);

    void forceClose(K gameKey);

    boolean close(K gameKey);

    void move(K gameKey, String notation);

    void attachInfo(GameInfo<K, ?> info);

    GameInfo<K, ?> getInfo(K gameKey);

    List<List<ChessPiece>> read(K gameKey);

    void requestDraw(K gameKey, Color requester);

    void shutdown();

    ChessEventListener getListener(K gameKey);

    Color getTurn(K gameKey);
}
