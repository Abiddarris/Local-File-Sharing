package com.abiddarris.lanfileviewer;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import com.abiddarris.lanfileviewer.databinding.ServerListLayoutBinding;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.List;

public class ServerListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<SharingDevice> servers = new ArrayList<>();

    public ServerListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void addServer(SharingDevice info) {
        servers.add(info);
        notifyDataSetChanged();
    }
    
    public SharingDevice getServer(String name) {
        Log.debug.log("", servers.toString());
        for(SharingDevice device : servers) {
            if(device.getName().equals(name)) {
                return device;
            }
        }
        return null;
    }

    public void removeServer(SharingDevice info) {
        for (SharingDevice device : servers) {
            if (device.getName().equals(info.getName())) {
                servers.remove(device);
                notifyDataSetChanged();
                break;
            }
        }
    }
    
    public void clear() {
    	servers.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return servers.size();
    }

    @Override
    public Object getItem(int position) {
        return servers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    @Override
    public View getView(int position, View view, ViewGroup group) {
        ServerListLayoutBinding binder = ServerListLayoutBinding.inflate(inflater);
        SharingDevice device = servers.get(position);
        
        String name = device.getName();
        binder.name.setText(name);

        return binder.getRoot();
    }
}
