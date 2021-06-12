package me.dzikimlecz.chessapi.manager;

import me.dzikimlecz.chessapi.ChessEventListener;
import me.dzikimlecz.chessapi.DrawReason;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.pieces.Pawn;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;
import me.dzikimlecz.chessapi.game.board.pieces.Queen;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GamesManagerTest {

    @Test
    @DisplayName("Should parse a move and perform it")
    public void MoveTest() {
        //Given
        GamesManager<String> manager = GamesManager.newManager();
        final var key = "key";
        final var handler = new Handler<>(manager, key);
        manager.newGame(key, handler);
        //When
        manager.move(key, "e4");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Then
        final var chessPiece = manager.read(key).get(3).get(4);
        System.out.println("chessPiece = " + chessPiece);
        assertTrue(chessPiece instanceof Pawn);
    }

}

class Handler<E> implements ChessEventListener {

    private final GamesManager<E> manager;
    private final E key;

    Handler(GamesManager<E> manager, E key) {
        this.manager = manager;
        this.key = key;
    }

    @Override
    public void onMate(Color winner) {
        System.out.println(winner + " won");
    }

    @Override
    public void onDraw(DrawReason reason) {
        System.out.println("draw: " + reason);
    }

    @Override
    public void onMoveHandled() {
        var read = manager.read(key);
        for (List<ChessPiece> row : read) {
            for (ChessPiece chessPiece : row) {
                String st = (chessPiece == null) ? " " : chessPiece.toString();
                System.out.printf(" %s ", st);
            }
            System.out.println();
        }
    }

    @Override
    public void onCheck(Color checked) {
        System.out.println(checked + " is in check");
    }

    @Override
    public void onIllegalMove() {
        System.out.println("Illegal move!");
    }

    @Override
    public boolean onDrawRequest(Color requester) {
        System.out.println(requester + " requested draw");
        return false;
    }

    @Override
    public Class<? extends Piece> onPawnExchange() {
        return Queen.class;
    }
}