package com.app.chesslog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.app.chesslog.data.ChessGame;
import com.app.chesslog.databinding.FragmentImportBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;

public class ImportFragment extends Fragment implements GameListAdapter.OnItemClickListener {

    private FragmentImportBinding binding;
    private MainViewModel viewModel;
    private GameListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentImportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        adapter = new GameListAdapter(false, this);
        binding.importedGamesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.importedGamesRecyclerView.setAdapter(adapter);

        binding.fetchGamesButton.setOnClickListener(v -> {
            String username = binding.usernameEditText.getText().toString();
            if (!username.isEmpty()) {
                binding.loadingIndicator.setVisibility(View.VISIBLE);
                viewModel.fetchGames(username);
            } else {
                Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getImportedGames().observe(getViewLifecycleOwner(), games -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            if (games != null && !games.isEmpty()) {
                adapter.submitList(games);
                binding.fetchedGamesCountTextView.setText(games.size() + " games fetched.");
                binding.fetchedGamesCountTextView.setVisibility(View.VISIBLE);
            } else {
                adapter.submitList(new ArrayList<>()); // Clear the list
                binding.fetchedGamesCountTextView.setText("No games fetched.");
                binding.fetchedGamesCountTextView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onItemClick(ChessGame game) {
        viewModel.insertGame(game);
        viewModel.setSelectedGame(game);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_analysis);
    }

    @Override
    public void onLoadClick(ChessGame game) {
        viewModel.setSelectedGame(game);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_analysis);
    }

    @Override
    public void onDeleteClick(ChessGame game) {
        // Not used in this fragment
    }
}