package com.abiddarris.lanfileviewer.explorer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.FileExplorerActivity;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.FragmentFileExplorerBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.network.NetworkFile;
import com.abiddarris.lanfileviewer.file.network.NetworkFileClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gretta.util.log.Log;

public abstract class BaseExplorerFragment extends Fragment {
    
    private FileExplorer explorer;
    private FragmentFileExplorerBinding binding;
    private OnBackPressed pressed;
    
    public static final String BACK_PRESSED_EVENT = "backPressedEvent";
    public static final String TAG = Log.getTag(FileExplorerActivity.class);
    
    public BaseExplorerFragment() {
        super(R.layout.fragment_file_explorer);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceBundle) {
        super.onViewCreated(view, savedInstanceBundle);
        
        File root = getSource()
            .getRoot();
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        
        binding = FragmentFileExplorerBinding.bind(view);
        
        explorer = new FileExplorer(this, 
            binding, binding.refreshlayout);
        explorer.open(root);
       
        binding.filesList.setAdapter(explorer.getAdapter());
        binding.filesList.setLayoutManager(layoutManager);
        binding.filesList.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        
        binding.refreshlayout.setOnRefreshListener(() -> {
            explorer.update();
        });
        
        pressed = new OnBackPressed();
        
        getActivity().getOnBackPressedDispatcher()
            .addCallback(pressed);
        
        getParentFragmentManager()
            .setFragmentResultListener(BACK_PRESSED_EVENT,this, (key,bundle) -> {
                navigateUp();
            });
        
        setHasOptionsMenu(true);
    }
    
    @Override
    @MainThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        
        inflater.inflate(R.menu.fragment_explorer_menu, menu);
    }
    
    @Override
    @MainThread
    public boolean onOptionsItemSelected(MenuItem arg0) {
        return super.onOptionsItemSelected(arg0);
    }
    
    public void navigateUp() {
    	if(!explorer.onBackPressed()) {
            pressed.setEnabled(false);
            ((AppCompatActivity) getActivity())
                .getOnBackPressedDispatcher()
                .onBackPressed();
        }
    }
    
    public ModifyMode getModifyMode(FileExplorer explorer) {
    	return new ModifyMode(explorer);
    }
    
    public abstract FileSource getSource();
    
    public class OnBackPressed extends OnBackPressedCallback {

        public OnBackPressed() {
            super(true);
        }
        
        @Override
        public void handleOnBackPressed() {
            navigateUp();
        }
    }
}
