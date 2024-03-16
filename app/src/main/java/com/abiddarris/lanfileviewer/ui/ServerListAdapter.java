package com.abiddarris.lanfileviewer.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.ScanResult;
import com.abiddarris.lanfileviewer.ScanResult.Event;
import com.abiddarris.lanfileviewer.ScanResult.OnResultUpdatedListener;
import com.abiddarris.lanfileviewer.databinding.LayoutServerListBinding;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;
import com.abiddarris.lanfileviewer.ui.ServerListAdapter.ServerViewHolder;

import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ServerListAdapter extends Adapter<ServerViewHolder> implements OnResultUpdatedListener {

    private LayoutInflater inflater;
    private List<SharingDevice> servers;
    private OnServerSelectedListener onServerSelectedListener;
    
    public ServerListAdapter(Context context, List<SharingDevice> servers) {
        inflater = LayoutInflater.from(context);
        this.servers = servers;
    }
    
    @Override
    public void onResultUpdated(ScanResult result, Event event, SharingDevice device) {
        switch(event) {
            case FOUND :
                servers.add(device);
                notifyItemInserted(servers.size() - 1);
                break;
            case LOST :
                for(int i = 0; i < servers.size(); ++i) {
                	if(servers.get(i).equals(device)) {
                		servers.remove(device);
                        notifyItemRemoved(i);
                        break;
                	}
                }
                
                break;
        }
    }

    @Override
    public ServerViewHolder onCreateViewHolder(ViewGroup group, int type) {
        return new ServerViewHolder(
            inflater.inflate(R.layout.layout_server_list, null));
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    @Override
    public void onBindViewHolder(ServerViewHolder holder, int index) {
        SharingDevice device = servers.get(index);

        String name = device.getName();
        holder.name.setText(name);
        holder.cardView.setOnClickListener(v -> {
            onServerSelectedListener.onSelect(device);
        });
    }
    
    public static class ServerViewHolder extends ViewHolder {
        
        private MaterialCardView cardView;
        private TextView name;
        
        public ServerViewHolder(View view) {
            super(view);
            
            name = view.findViewById(R.id.name);
            cardView = view.findViewById(R.id.card_view);
        }
        
    }
    
    public static interface OnServerSelectedListener {
        void onSelect(SharingDevice device);
    }

    public OnServerSelectedListener getOnServerSelectedListener() {
        return this.onServerSelectedListener;
    }
    
    public void setOnServerSelectedListener(OnServerSelectedListener onServerSelectedListener) {
        this.onServerSelectedListener = onServerSelectedListener;
    }
}