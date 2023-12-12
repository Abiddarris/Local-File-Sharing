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

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), LinearLayoutManager.HORIZONTAL);
        itemDecoration.setDrawable(getActivity().getDrawable(R.drawable.ic_chevron_right));
        
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.addItemDecoration(itemDecoration);
        
        return binding.getRoot();
    }
    
    public void setExplorer(Explorer explorer) {
    	adapter.setExplorer(explorer);
    }
    
}