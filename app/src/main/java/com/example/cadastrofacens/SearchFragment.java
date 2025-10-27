package com.example.cadastrofacens;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment implements SongAdapter.OnSongInteractionListener {

    private RecyclerView searchResultsList;
    private RecyclerView categoryGrid;
    private SearchView searchView;
    private SongAdapter songAdapter;
    private CategoryAdapter categoryAdapter;
    private MainActivity mainActivity;

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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchResultsList = view.findViewById(R.id.search_results_list);
        categoryGrid = view.findViewById(R.id.category_grid);
        searchView = view.findViewById(R.id.searchView);
        
        setupAdapters();
        setupSearchView();

        return view;
    }

    private void setupAdapters() {
        // Configura a lista de resultados da busca
        searchResultsList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mainActivity != null) {
            // CORREÇÃO: Usando o novo método getAllSongs()
            songAdapter = new SongAdapter(mainActivity.getAllSongs(), this);
            searchResultsList.setAdapter(songAdapter);
        }

        // Configura a grade de categorias
        categoryGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<String> categories = Arrays.asList("Funk", "Pop", "Rock", "Sertanejo", "Eletrônica", "Hip Hop", "Pagode", "Clássica");
        categoryAdapter = new CategoryAdapter(categories);
        categoryGrid.setAdapter(categoryAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    categoryGrid.setVisibility(View.VISIBLE);
                    searchResultsList.setVisibility(View.GONE);
                } else {
                    categoryGrid.setVisibility(View.GONE);
                    searchResultsList.setVisibility(View.VISIBLE);
                    if (songAdapter != null) songAdapter.filter(newText);
                }
                return true;
            }
        });
    }

    @Override
    public void onSongClick(Song song) {
        if (mainActivity != null) {
            mainActivity.playSong(song);
        }
    }

    @Override
    public void onOptionsMenuClick(Song song) {
        // Não implementado para a busca, mas necessário pela interface
    }

    @Override
    public void onResume() {
        super.onResume();
        // Garante que a lista (e os ícones de like) esteja atualizada
        if (songAdapter != null) {
            songAdapter.notifyDataSetChanged();
        }
    }
}
