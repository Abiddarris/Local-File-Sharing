package com.abiddarris.lanfileviewer.explorer;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.FragmentExplorerPathBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import java.util.ArrayList;
import java.util.List;

public class ExplorerPathFragment extends Fragment {
    
    private FragmentExplorerPathBinding binding;
    private PathAdapter adapter = new PathAdapter();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        binding = FragmentExplorerPathBinding.inflate(inflater);
        
        adapter.setContext(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        ChevronItemDecoration itemDecoration = new ChevronItemDecoration(getContext());
        
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.addItemDecoration(itemDecoration);
        
        binding.icon.setOnClickListener((v) -> {
            Explorer explorer = adapter.getExplorer();
            if(explorer == null) return;
                
            File currentFile = explorer.getParent();
            if(currentFile == null) return;
           
            File file = currentFile.getSource()
                .getRoot();
            explorer.open(file);
        });
        
        return binding.getRoot();
    }
    
    public void setExplorer(Explorer explorer) {
    	adapter.setExplorer(explorer);
    }
    
}