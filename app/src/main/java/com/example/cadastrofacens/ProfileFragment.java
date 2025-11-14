package com.example.cadastrofacens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.List;

public class ProfileFragment extends Fragment {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleSignIn";
    private static final String DEBUG_TAG = "Database_DEBUG";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private MainActivity mainActivity;
    private AppDatabase db;

    private ImageView profileImage;
    private TextView profileName, profileEmail;
    private SignInButton signInButton;
    private Button signOutButton, debugDbButton;
    private View profileInfoLayout;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = AppDatabase.getDatabase(getContext());

        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        signInButton = view.findViewById(R.id.sign_in_button);
        signOutButton = view.findViewById(R.id.sign_out_button);
        debugDbButton = view.findViewById(R.id.debug_db_button);
        profileInfoLayout = view.findViewById(R.id.profile_info);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        mAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(v -> signIn());
        signOutButton.setOnClickListener(v -> signOut());
        debugDbButton.setOnClickListener(v -> logDatabaseContents());

        return view;
    }

    private void logDatabaseContents() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Log.d(DEBUG_TAG, "--- USERS ---");
            List<User> users = db.userDao().getAllUsers();
            for (User user : users) {
                Log.d(DEBUG_TAG, user.toString());
            }

            Log.d(DEBUG_TAG, "--- PLAYLISTS ---");
            List<Playlist> playlists = db.playlistDao().getAllPlaylists();
            for (Playlist playlist : playlists) {
                Log.d(DEBUG_TAG, playlist.toString());
            }

            Log.d(DEBUG_TAG, "--- SONGS ---");
            List<Song> songs = db.songDao().getAllSongs();
            for (Song song : songs) {
                Log.d(DEBUG_TAG, song.toString());
            }

            Log.d(DEBUG_TAG, "--- PLAYLIST-SONG-CROSSREF ---");
            List<PlaylistSongCrossRef> refs = db.playlistDao().getAllCrossRefs();
            for (PlaylistSongCrossRef ref : refs) {
                Log.d(DEBUG_TAG, ref.toString());
            }
            
            Log.d(DEBUG_TAG, "--- RECENTLY PLAYED ---");
            List<PlayedSong> playedSongs = db.playedSongDao().getAllPlayedSongs();
            for (PlayedSong playedSong : playedSongs) {
                Log.d(DEBUG_TAG, playedSong.toString());
            }

            Log.d(DEBUG_TAG, "--- DATABASE DEBUG COMPLETE ---");
        });
        Toast.makeText(getContext(), "Verifique o Logcat para o debug do banco.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> {
            if (mainActivity != null) {
                mainActivity.checkUserStatus();
            }
            updateUI(null);
            Toast.makeText(getContext(), "Você saiu da sua conta.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Falha no Google Sign In, código: " + e.getStatusCode(), e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User userToSave = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail(), firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                db.userDao().insertOrUpdateUser(userToSave);
                                if (mainActivity != null) {
                                    mainActivity.runOnUiThread(() -> mainActivity.checkUserStatus());
                                }
                            });
                        }
                        updateUI(firebaseUser);
                    } else {
                        Log.w(TAG, "Falha na autenticação com Firebase", task.getException());
                        Toast.makeText(getContext(), "Falha na autenticação com Firebase.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) {
            profileInfoLayout.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.GONE);

            profileName.setText(user.getDisplayName());
            profileEmail.setText(user.getEmail());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(profileImage);
            }

        } else {
            profileInfoLayout.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
        }
    }
}
