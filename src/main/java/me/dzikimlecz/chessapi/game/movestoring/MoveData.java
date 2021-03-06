package me.dzikimlecz.chessapi.game.movestoring;

import me.dzikimlecz.chessapi.game.board.pieces.ChessPiece;
import me.dzikimlecz.chessapi.game.board.pieces.Movable;
import me.dzikimlecz.chessapi.game.board.square.Square;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveParser;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.moveanalysing.IMoveAnalyser;
import me.dzikimlecz.chessapi.game.board.square.Color;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MoveData {
	private String notation;
	private final Map<ChessPiece, Square> variations;
	private final Color color;
	private boolean toFurtherCheck;
	private final boolean doingCastling;

	public MoveData(String notation, Map<? extends ChessPiece, Square> variations, Color color) {
		this.notation = notation;
		this.doingCastling = IMoveParser.castling.matcher(notation).matches();
		if (variations.keySet().stream().anyMatch(piece -> !(piece instanceof Movable)))
			throw new IllegalArgumentException("Can't put non-movable piece to move data");
		this.variations = new HashMap<>(variations);
		this.color = color;
	}

	public boolean isDoingCastling() {
		return doingCastling;
	}

	public boolean toFurtherCheck() {
		return toFurtherCheck;
	}

	public void setToFurtherCheck(boolean toFurtherCheck) {
		this.toFurtherCheck = toFurtherCheck;
	}

	public MoveData validate(IMoveValidator validator) {
		return validator.validate(this);
	}

	public MoveData analyse(IMoveAnalyser analyzer) {
		return analyzer.analyse(this);
	}

	public String notation() {
		return notation;
	}

	public void setNotation(String notation) {
		this.notation = notation;
	}

	public Map<ChessPiece, Square> getVariations() {
		return variations;
	}

	public Color color() {
		return color;
	}

	public static MoveData copyOf(MoveData toCopy) {
		return new MoveData(toCopy.notation, toCopy.variations, toCopy.color);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MoveData moveData = (MoveData) o;
		return notation.equals(moveData.notation) && variations.equals(
				moveData.variations) && color == moveData.color;
	}

	@Override
	public int hashCode() {
		return Objects.hash(notation, variations, color);
	}

	public boolean doingCastling() {
		return doingCastling;
	}
}
