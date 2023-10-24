package com.abiddarris.lanfileviewer;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import com.abiddarris.lanfileviewer.databinding.ServerListLayoutBinding;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.List;

public class ServerListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<NsdServiceInfo> servers = new ArrayList<>();

    public ServerListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void addServer(NsdServiceInfo info) {
        servers.add(info);
        notifyDataSetChanged();
    }
    
    public NsdServiceInfo getServer(String name) {
        Log.debug.log("", servers.toString());
        for(NsdServiceInfo info : servers) {
            if(info.getServiceName().equals(name)) {
                return info;
            }
        }
        return null;
    }

    public void removeServer(NsdServiceInfo info) {
        for (NsdServiceInfo server : servers) {
            if (server.getServiceName().equals(info.getServiceName())) {
                servers.remove(server);
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
        NsdServiceInfo info = servers.get(position);
        
        String name = info.getServiceName();
        name = name.substring(0, name.length() - "_FILEV".length());
        binder.name.setText(name);

        return binder.getRoot();
    }
}
