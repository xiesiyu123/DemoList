package com.whf.demolist.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whf.demolist.R;

import java.util.List;

/**
 * Created by @author WangHaoFei on 2018/8/13.
 */

public class WifiAdapter extends RecyclerView.Adapter<WifiHolder> {

    private static final String TAG = "WIFI_TEST_" + WifiAdapter.class.getSimpleName();

    private LayoutInflater layoutInflater;
    private List<ScanResult> wifiList;
    private OnItemClickListener onItemClickListener;
    private String connectedSSID;

    public WifiAdapter(Context context, List<ScanResult> wifiList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.wifiList = wifiList;
    }

    @Override
    public WifiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_wifi, parent, false);
        return new WifiHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WifiHolder holder, int position) {
        ScanResult scanResult = wifiList.get(position);

        holder.tvSsid.setText(scanResult.SSID);
        holder.tvRssi.setText(String.valueOf(scanResult.level));
        String securityType = getSecurity(scanResult);
        holder.tvSecurity.setText(securityType);

        if (("\"" + scanResult.SSID + "\"").equals(connectedSSID)) {
            holder.tvState.setText("已连接");
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(holder.tvState, scanResult.SSID, securityType);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    private String getSecurity(ScanResult scanResult) {
        String capabilities = scanResult.capabilities;

        if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
            return "wpa";
        } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
            return "wep";
        } else {
            return "non";
        }
    }

    public void setWifiList(List<ScanResult> wifiList) {
        this.wifiList = wifiList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setConnectedWifi(String ssid) {
        this.connectedSSID = ssid;
    }

    interface OnItemClickListener {
        void onItemClickListener(TextView tvState, String ssid, String type);

        void onRemoveClickListener();
    }
}
