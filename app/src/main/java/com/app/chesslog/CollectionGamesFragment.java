package com.app.chesslog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.app.chesslog.data.ChessGame;
import com.app.chesslog.databinding.FragmentGameListBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CollectionGamesFragment extends Fragment implements GameListAdapter.OnItemClickListener {

    private FragmentGameListBinding binding;
    private MainViewModel viewModel;
    private GameListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new GameListAdapter(true, this);
        binding.gamesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.gamesRecyclerView.setAdapter(adapter);

        viewModel.getSavedGames().observe(getViewLifecycleOwner(), games -> {
            if (games != null) {
                adapter.submitList(games);
            }
        });
    }

    @Override
    public void onItemClick(ChessGame game) {
        viewModel.setSelectedGame(game);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_analysis);
    }

    @Override
    public void onDeleteClick(ChessGame game) {
        viewModel.deleteGame(game);
    }

    @Override
    public void onLoadClick(ChessGame game) {
        viewModel.setSelectedGame(game);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_analysis);
    }
}