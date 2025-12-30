package com.app.chesslog;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.ArrayList;
import java.util.List;

public class MoveListAdapter extends RecyclerView.Adapter<MoveListAdapter.MoveViewHolder> {

    private final List<MoveRow> moveRows = new ArrayList<>();
    private int selectedPosition = -1;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int moveIndex);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_move, parent, false);
        return new MoveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveViewHolder holder, int position) {
        MoveRow moveRow = moveRows.get(position);
        holder.bind(moveRow);

        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return moveRows.size();
    }

    public void setMoves(MoveList moves) {
        moveRows.clear();
        for (int i = 0; i < moves.size(); i += 2) {
            String moveNumber = (i / 2 + 1) + ".";
            String whiteMove = moves.get(i).toString();
            String blackMove = (i + 1 < moves.size()) ? moves.get(i + 1).toString() : "";
            moveRows.add(new MoveRow(moveNumber, whiteMove, blackMove));
        }
        notifyDataSetChanged();
    }

    public void setSelectedMove(int moveIndex) {
        if (moveIndex < 0) {
            selectedPosition = -1;
        } else {
            selectedPosition = moveIndex / 2;
        }
        notifyDataSetChanged();
    }

    class MoveViewHolder extends RecyclerView.ViewHolder {
        private final TextView moveNumberText;
        private final TextView whiteMoveText;
        private final TextView blackMoveText;

        public MoveViewHolder(@NonNull View itemView) {
            super(itemView);
            moveNumberText = itemView.findViewById(R.id.move_number_text);
            whiteMoveText = itemView.findViewById(R.id.white_move_text);
            blackMoveText = itemView.findViewById(R.id.black_move_text);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    // Notify the listener with the index of the white move
                    listener.onItemClick(position * 2);
                }
            });

            whiteMoveText.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position * 2);
                }
            });

            blackMoveText.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION && !blackMoveText.getText().toString().isEmpty()) {
                    listener.onItemClick(position * 2 + 1);
                }
            });
        }

        public void bind(MoveRow moveRow) {
            moveNumberText.setText(moveRow.moveNumber);
            whiteMoveText.setText(moveRow.whiteMove);
            blackMoveText.setText(moveRow.blackMove);
        }
    }

    // Helper class to hold data for a single row
    private static class MoveRow {
        final String moveNumber;
        final String whiteMove;
        final String blackMove;

        MoveRow(String moveNumber, String whiteMove, String blackMove) {
            this.moveNumber = moveNumber;
            this.whiteMove = whiteMove;
            this.blackMove = blackMove;
        }
    }
}
