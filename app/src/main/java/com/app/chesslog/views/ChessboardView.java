package com.app.chesslog.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.Side;

import java.util.HashMap;
import java.util.Map;

public class ChessboardView extends View {

    private Paint darkSquarePaint;
    private Paint lightSquarePaint;
    private Paint piecePaint;
    private int squareSize;
    private Board board; // chesslib Board object

    private Map<Piece, String> pieceUnicodeMap;

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        darkSquarePaint = new Paint();
        darkSquarePaint.setColor(Color.parseColor("#769656")); // Dark green
        lightSquarePaint = new Paint();
        lightSquarePaint.setColor(Color.parseColor("#eeeed2")); // Light beige

        piecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        piecePaint.setTextAlign(Paint.Align.CENTER);
        piecePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        board = new Board(); // Initialize with a standard starting position

        pieceUnicodeMap = new HashMap<>();
        loadPieceUnicodeMap();
    }

    private void loadPieceUnicodeMap() {
        pieceUnicodeMap.put(Piece.BLACK_KING, "♚");
        pieceUnicodeMap.put(Piece.BLACK_QUEEN, "♛");
        pieceUnicodeMap.put(Piece.BLACK_ROOK, "♜");
        pieceUnicodeMap.put(Piece.BLACK_BISHOP, "♝");
        pieceUnicodeMap.put(Piece.BLACK_KNIGHT, "♞");
        pieceUnicodeMap.put(Piece.BLACK_PAWN, "♟");

        pieceUnicodeMap.put(Piece.WHITE_KING, "♔");
        pieceUnicodeMap.put(Piece.WHITE_QUEEN, "♕");
        pieceUnicodeMap.put(Piece.WHITE_ROOK, "♖");
        pieceUnicodeMap.put(Piece.WHITE_BISHOP, "♗");
        pieceUnicodeMap.put(Piece.WHITE_KNIGHT, "♘");
        pieceUnicodeMap.put(Piece.WHITE_PAWN, "♙");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size); // Make the view square
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        squareSize = w / 8; // Divide by 8 for an 8x8 chessboard
        piecePaint.setTextSize(squareSize * 0.7f); // Adjust text size to fit square
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // Determine color of the square
                if ((row + col) % 2 == 0) {
                    canvas.drawRect(col * squareSize, row * squareSize,
                            (col + 1) * squareSize, (row + 1) * squareSize, lightSquarePaint);
                } else {
                    canvas.drawRect(col * squareSize, row * squareSize,
                            (col + 1) * squareSize, (row + 1) * squareSize, darkSquarePaint);
                }

                // Draw pieces
                int rank = 7 - row; // Invert row to match chesslib's rank (rank 1 is bottom, rank 8 is top)
                int file = col;
                Square square = Square.squareAt(rank * 8 + file);
                Piece piece = board.getPiece(square);

                if (piece != Piece.NONE) {
                    String unicodeChar = pieceUnicodeMap.get(piece);
                    if (unicodeChar != null) {
                        float x = col * squareSize + squareSize / 2f;
                        float y = row * squareSize + squareSize / 2f - ((piecePaint.descent() + piecePaint.ascent()) / 2f);

                        // Set color for the piece (black or white)
                        if (piece.getPieceSide() == Side.WHITE) {
                            piecePaint.setColor(Color.WHITE);
                        } else {
                            piecePaint.setColor(Color.BLACK);
                        }
                        canvas.drawText(unicodeChar, x, y, piecePaint);
                    }
                }
            }
        }
    }

    public void setBoard(Board board) {
        this.board = board;
        invalidate(); // Redraw the board
    }
}