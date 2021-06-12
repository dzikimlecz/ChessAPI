package me.dzikimlecz.chessapi.game.board;

import me.dzikimlecz.chessapi.game.board.pieces.*;
import me.dzikimlecz.chessapi.game.board.square.Square;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;

import static java.text.MessageFormat.format;
import static me.dzikimlecz.chessapi.game.board.Color.BLACK;
import static me.dzikimlecz.chessapi.game.board.Color.WHITE;
import static org.junit.jupiter.api.Assertions.fail;

class BoardTest {

    private boolean checkPlacement(Square square, ChessPiece piece) {
        switch (square.row()) {
            case 1 -> {
                if (piece == null || piece.color() != WHITE) return false;
            }
            case 2 -> {
                if (!(piece instanceof Pawn) || piece.color() != WHITE) return false;
            }
            case 8 -> {
                if (piece == null || piece.color() != BLACK) return false;
            }
            case 7 -> {
                if (!(piece instanceof Pawn) || piece.color() != BLACK) return false;
            }
            default -> {
                return piece == null;
            }
        }
        return switch (square.line()) {
            case 'a', 'h', 'A', 'H' -> piece instanceof Rook || piece instanceof Pawn;
            case 'b', 'g', 'B', 'G' -> piece instanceof Knight || piece instanceof Pawn;
            case 'c', 'f', 'C', 'F' -> piece instanceof Bishop || piece instanceof Pawn;
            case 'd', 'D' -> piece instanceof Queen || piece instanceof Pawn;
            case 'e', 'E' -> piece instanceof King || piece instanceof Pawn;
            default -> throw new AssertionError();
        };
    }


    @Test
    @DisplayName("Should create default board with right pieces on the right squares")
    public void defaultBoardTest() {
        //Given
        BiPredicate<Square, ChessPiece> filtersPerSquare = this::checkPlacement;
        //When
        var board = Board.create();
        //Then
        board.applyOnSquares(square -> {
            if (!filtersPerSquare.test(square, square.piece())) {
                System.out.println(format("Square: {0}, piece: {1}", square, square.piece()));
                fail();
            }
        });
    }


}