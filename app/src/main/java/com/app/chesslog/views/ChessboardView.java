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

import android.view.MotionEvent;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ChessboardView extends View {

    private Paint darkSquarePaint;
    private Paint lightSquarePaint;
    private Paint piecePaint;
    private Paint selectedSquarePaint; // Added for selection highlight
    private int squareSize;
    private Board board; // chesslib Board object
    private boolean isFlipped = false; // Added: isFlipped flag

    private Map<Piece, String> pieceUnicodeMap;
    private Square selectedSquare; // Added to track selection
    private OnMoveListener onMoveListener; // Added listener

    public interface OnMoveListener {
        void onMove(Move move);
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.onMoveListener = listener;
    }

    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        darkSquarePaint = new Paint();
        darkSquarePaint.setColor(Color.parseColor("#769656")); // Dark green
        lightSquarePaint = new Paint();
        lightSquarePaint.setColor(Color.parseColor("#eeeed2")); // Light beige

        selectedSquarePaint = new Paint();
        selectedSquarePaint.setColor(Color.parseColor("#80FFFF00")); // Semi-transparent yellow

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
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (squareSize == 0) return false;

            int col = (int) (event.getX() / squareSize);
            int row = (int) (event.getY() / squareSize);

            if (col < 0 || col > 7 || row < 0 || row > 7) return false;

            int rank, file;
            if (isFlipped) {
                rank = row;
                file = 7 - col;
            } else {
                rank = 7 - row;
                file = col;
            }
            Square clickedSquare = Square.squareAt(rank * 8 + file);

            if (selectedSquare == null) {
                Piece piece = board.getPiece(clickedSquare);
                if (piece != Piece.NONE && piece.getPieceSide() == board.getSideToMove()) {
                    selectedSquare = clickedSquare;
                    invalidate();
                }
            } else {
                if (clickedSquare == selectedSquare) {
                    selectedSquare = null;
                    invalidate();
                } else {
                    Piece clickedPiece = board.getPiece(clickedSquare);
                    if (clickedPiece != Piece.NONE && clickedPiece.getPieceSide() == board.getSideToMove()) {
                         // Switch selection to another piece of same side
                        selectedSquare = clickedSquare;
                        invalidate();
                    } else {
                        // Attempt move
                        // Basic check: is it pseudo-legal?
                        // We rely on listener to validate fully, but we can check legal moves here if we want.
                        // For simplicity, just fire the event.
                        if (onMoveListener != null) {
                            // Detect promotion? For now, assume Queen promotion if pawn hits last rank.
                            // We can add a dialog later.
                             Move move = new Move(selectedSquare, clickedSquare);
                             // Minimal promotion handling (auto-queen)
                             Piece movingPiece = board.getPiece(selectedSquare);
                             if (movingPiece == Piece.WHITE_PAWN && clickedSquare.getRank().equals(com.github.bhlangonijr.chesslib.Rank.RANK_8)) {
                                 move = new Move(selectedSquare, clickedSquare, Piece.WHITE_QUEEN);
                             } else if (movingPiece == Piece.BLACK_PAWN && clickedSquare.getRank().equals(com.github.bhlangonijr.chesslib.Rank.RANK_1)) {
                                 move = new Move(selectedSquare, clickedSquare, Piece.BLACK_QUEEN);
                             }

                             onMoveListener.onMove(move);
                        }
                        selectedSquare = null;
                        invalidate();
                    }
                }
            }
            return true;
        }
        return true;
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

                int rank, file; // Modified: account for isFlipped
                if (isFlipped) {
                    rank = row;
                    file = 7 - col;
                } else {
                    rank = 7 - row;
                    file = col;
                }
                Square square = Square.squareAt(rank * 8 + file);
                
                // Draw selection highlight
                if (selectedSquare == square) {
                    canvas.drawRect(col * squareSize, row * squareSize,
                            (col + 1) * squareSize, (row + 1) * squareSize, selectedSquarePaint);
                }

                // Draw pieces
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

    // Added: flip() method
    public void flip() {
        isFlipped = !isFlipped;
        invalidate();
    }
}