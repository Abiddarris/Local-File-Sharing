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
import androidx.appcompat.widget.Toolbar;
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
import com.abiddarris.lanfileviewer.file.sharing.NetworkFile;
import com.abiddarris.lanfileviewer.sorter.FileSorter;
import com.abiddarris.lanfileviewer.ui.LocalExplorerDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.List;

public abstract class ExplorerFragment extends Fragment {

    private Explorer explorer;
    private FileSorter sorter;
    private FileSource source;
    private FragmentFileExplorerBinding binding;
    private List<OnExplorerCreatedListener> explorerCreatedListener = new ArrayList<>();
    private OnBackPressed pressed;

    public static final String BACK_PRESSED_EVENT = "backPressedEvent";
    public static final String TAG = Log.getTag(FileExplorerActivity.class);
    public static final String PARENT = "parent";
    public static final String TITLE = "title";

    public ExplorerFragment(FileSource source) {
        super(R.layout.fragment_file_explorer);

        this.source = source;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceBundle) {
        super.onViewCreated(view, savedInstanceBundle);
        
        File root =
                (savedInstanceBundle == null
                        ? getSource().getRoot()
                        : getSource().getFile(savedInstanceBundle.getString(PARENT)));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        binding = FragmentFileExplorerBinding.bind(view);

        setHasOptionsMenu(true);

        // binding.toolbar.setTitle(requireArguments().getString(TITLE,));

        explorer = new Explorer(this, binding, binding.refreshlayout);
        for(OnExplorerCreatedListener listener : explorerCreatedListener) {
            listener.onExplorerCreated(this, explorer);
        }
        
        if(sorter != null) explorer.setSorter(sorter);
        explorer.open(root);

        binding.filesList.setAdapter(explorer.getAdapter());
        binding.filesList.setLayoutManager(layoutManager);
        binding.filesList.addItemDecoration(
                new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        binding.refreshlayout.setOnRefreshListener(
                () -> {
                    explorer.update();
                });

        pressed = new OnBackPressed();

        getActivity().getOnBackPressedDispatcher().addCallback(pressed);

        getParentFragmentManager()
                .setFragmentResultListener(
                        BACK_PRESSED_EVENT,
                        this,
                        (key, bundle) -> {
                            navigateUp();
                        });
    }

    @Override
    @MainThread
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(PARENT, explorer.getParent().getPath());
    }

    @Override
    @MainThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_explorer_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.createFolder :
                new CreateFolderDialog(getExplorer())
                    .show(getChildFragmentManager(), null);
        }
        
        return super.onOptionsItemSelected(item);
    }
    

    public void navigateUp() {
        if (!explorer.onBackPressed()) {
            pressed.setEnabled(false);
            ((AppCompatActivity) getActivity()).getOnBackPressedDispatcher().onBackPressed();
        }
    }
    
    protected Mode getMainMode(Explorer explorer) {
       return new NavigateMode(explorer);
    }

    public SelectMode getSelectMode(Explorer explorer) {
        return new SelectMode(explorer);
    }

    public final FileSource getSource() {
        return source;
    }
    
    public void addOnExplorerCreatedListener(OnExplorerCreatedListener listener) {
    	explorerCreatedListener.add(listener);
        
        if(explorer != null) listener.onExplorerCreated(this, explorer);
    }

    public class OnBackPressed extends OnBackPressedCallback {

        public OnBackPressed() {
            super(true);
        }

        @Override
        public void handleOnBackPressed() {
            navigateUp();
        }
    }

    public Explorer getExplorer() {
        return this.explorer;
    }

    public FileSorter getSorter() {
        return this.sorter;
    }

    public void setSorter(FileSorter sorter) {
        this.sorter = sorter;
        
        if(explorer != null) {
            explorer.setSorter(sorter);
        }
    }
    
    public static interface OnExplorerCreatedListener {
        
        public void onExplorerCreated(ExplorerFragment fragment, Explorer explorer);
        
    }
}
