package me.dzikimlecz.chessapi.game.movestoring;

import me.dzikimlecz.chessapi.game.board.Square;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveParser;
import me.dzikimlecz.chessapi.game.moveparsing.IMoveValidator;
import me.dzikimlecz.chessapi.game.moveanalysing.MoveAnalyser;
import me.dzikimlecz.chessapi.game.board.Color;
import me.dzikimlecz.chessapi.game.board.pieces.Piece;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MoveData {
	private String notation;
	private final Map<Piece, Square> variations;
	private final Color color;
	private boolean toFurtherCheck;
	private final boolean doingCastling;

	public MoveData(String notation, Map<? extends Piece, Square> variations, Color color) {
		this.notation = notation;
		this.doingCastling = IMoveParser.castling.matcher(notation).matches();
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

	public MoveData analyse(MoveAnalyser analyzer) {
		return analyzer.analyse(this);
	}

	public String getNotation() {
		return notation;
	}

	public void setNotation(String notation) {
		this.notation = notation;
	}

	public Map<Piece, Square> getVariations() {
		return variations;
	}

	public Color getColor() {
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
