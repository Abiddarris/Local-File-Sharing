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

    private Context context;
    private LayoutInflater inflater;
    private List<SharingDevice> servers;
    private OnServerSelectedListener onServerSelectedListener;
    
    public ServerListAdapter(Context context, List<SharingDevice> servers) {
        this.context = context;
        this.servers = servers;
        
        inflater = LayoutInflater.from(context);
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

        String address = context.getString(R.string.server_address);
        
        String name = device.getName();
        holder.name.setText(name);
        holder.cardView.setOnClickListener(v -> {
            onServerSelectedListener.onSelect(device);
        });
        holder.address.setText(
            String.format(address, device.getHost().getHostAddress(), device.getPort()));
    }
    
    public static class ServerViewHolder extends ViewHolder {
        
        private MaterialCardView cardView;
        private TextView name;
        private TextView address;
        
        public ServerViewHolder(View view) {
            super(view);
            
            name = view.findViewById(R.id.name);
            cardView = view.findViewById(R.id.card_view);
            address = view.findViewById(R.id.address);
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