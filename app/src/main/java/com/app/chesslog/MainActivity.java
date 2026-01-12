package com.app.chesslog;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.app.chesslog.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.app.chesslog.R;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.util.Log;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
 
        setSupportActionBar(binding.topAppBar);

        // Setup Drawer Toggle
        toggle = new ActionBarDrawerToggle(
                this, 
                binding.drawerLayout, 
                binding.topAppBar,
                R.string.navigation_drawer_open, 
                R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup Navigation View Listener
        binding.navView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_analysis) {
                selectedFragment = new AnalysisFragment();
            } else if (itemId == R.id.navigation_games) {
                selectedFragment = new GamesFragment();
            } else if (itemId == R.id.navigation_import) {
                selectedFragment = new ImportFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            binding.navView.setCheckedItem(R.id.navigation_analysis);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AnalysisFragment())
                    .commit();
        }

        // Extract and get engine path on app startup
        String enginePath = extractAndGetEnginePath(this);
        Log.d(TAG, "Stockfish engine path: " + enginePath);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Extracts the Stockfish binary from assets to internal storage and returns its path.
     * Sets execute permissions.
     * @param context The application context.
     * @return The absolute path to the executable Stockfish binary.
     */
    public static String extractAndGetEnginePath(Context context) {
        File filesDir = context.getFilesDir();
        File stockfishFile = new File(filesDir, "stockfish");

        // Check if the file already exists and is executable
        if (stockfishFile.exists() && stockfishFile.canExecute()) {
            Log.d(TAG, "Stockfish binary already exists and is executable at: " + stockfishFile.getAbsolutePath());
            return stockfishFile.getAbsolutePath();
        }

        Log.d(TAG, "Extracting Stockfish binary to: " + stockfishFile.getAbsolutePath());
        try (InputStream is = context.getAssets().open("stockfish"); // Assuming R.raw.stockfish
             OutputStream os = new FileOutputStream(stockfishFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            // Set execute permissions
            if (!stockfishFile.setExecutable(true, false)) {
                Log.e(TAG, "Failed to set execute permissions on Stockfish binary.");
            }

            Log.d(TAG, "Stockfish binary extracted and permissions set successfully.");
            return stockfishFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error extracting Stockfish binary: " + e.getMessage());
            return null;
        }
    }

    public void navigateTo(int itemId) {
        binding.navView.setCheckedItem(itemId);
        Fragment selectedFragment = null;
        if (itemId == R.id.navigation_analysis) {
            selectedFragment = new AnalysisFragment();
        } else if (itemId == R.id.navigation_games) {
            selectedFragment = new GamesFragment();
        } else if (itemId == R.id.navigation_import) {
            selectedFragment = new ImportFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }
    }
}
