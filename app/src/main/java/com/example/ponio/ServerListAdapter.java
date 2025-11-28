package com.example.ponio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.ServerViewHolder> {

    private List<ServerDiscovery.DiscoveredServer> servers = new ArrayList<>();
    private OnServerClickListener listener;

    public interface OnServerClickListener {
        void onServerClick(ServerDiscovery.DiscoveredServer server);
    }

    public ServerListAdapter(OnServerClickListener listener) {
        this.listener = listener;
    }

    public void setServers(List<ServerDiscovery.DiscoveredServer> servers) {
        this.servers = servers;
        notifyDataSetChanged();
    }

    public void addServer(ServerDiscovery.DiscoveredServer server) {
        if (!servers.contains(server)) {
            servers.add(server);
            notifyItemInserted(servers.size() - 1);
        }
    }

    public void clear() {
        servers.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_server, parent, false);
        return new ServerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        ServerDiscovery.DiscoveredServer server = servers.get(position);
        holder.bind(server, listener);
    }

    @Override
    public int getItemCount() {
        return servers.size();
    }

    static class ServerViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView tvServerName;
        private TextView tvServerAddress;
        private TextView tvServerProtocol;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvServerName = itemView.findViewById(R.id.tvServerName);
            tvServerAddress = itemView.findViewById(R.id.tvServerAddress);
            tvServerProtocol = itemView.findViewById(R.id.tvServerProtocol);
        }

        public void bind(ServerDiscovery.DiscoveredServer server, OnServerClickListener listener) {
            tvServerName.setText(server.name);
            tvServerAddress.setText(server.ipAddress + ":" + server.port);
            tvServerProtocol.setText(server.protocol.toUpperCase());

            // Set protocol badge color
            int protocolColor;
            switch (server.protocol.toLowerCase()) {
                case "tcp":
                    protocolColor = 0xFF4CAF50; // Green
                    break;
                case "udp":
                    protocolColor = 0xFF2196F3; // Blue
                    break;
                case "bluetooth":
                    protocolColor = 0xFF9C27B0; // Purple
                    break;
                default:
                    protocolColor = 0xFF888888; // Gray
            }
            tvServerProtocol.setTextColor(protocolColor);

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServerClick(server);
                }
            });
        }
    }
}