package com.example.plantwaterer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class DeviceListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
    private OnPairButtonClickListener mListener;

    public DeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_device_list_adapter, null);

            holder = new ViewHolder();

            holder.nameTv = (TextView) convertView.findViewById(R.id.tv_name);
            holder.addressTv = (TextView) convertView.findViewById(R.id.tv_address);
            holder.pairBtn = (Button) convertView.findViewById(R.id.btn_pair);
            holder.connectBtn = (Button) convertView.findViewById(R.id.btn_connect);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mData.get(position);

        String dType = deviceTypeFinder(device);
        int dType2 = deviceTypeFinder2(device);




        holder.nameTv.setText(device.getName());
        holder.nameTv.setCompoundDrawablesWithIntrinsicBounds(dType2,0,0,0);
        holder.addressTv.setText(dType+" "+device.getAddress());


        holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Delete" : "Pair");
        holder.pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPairButtonClick(position, holder.pairBtn);
                }
            }
        });

        holder.connectBtn.setEnabled(false);

        if (holder.pairBtn.getText().toString().equals("Pair")) {
            holder.connectBtn.setEnabled(false);
        } else if (holder.pairBtn.getText().toString().equals("Delete")) {
            holder.connectBtn.setEnabled(true);
        }

        if (holder.connectBtn.isEnabled()) {
            holder.connectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onPairButtonClick(position, holder.connectBtn);
                    }
                }
            });
        }


        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView addressTv;
        TextView pairBtn;
        TextView connectBtn;
    }

    public interface OnPairButtonClickListener {
        public abstract void onPairButtonClick(int position, TextView txt);
    }

    public String deviceTypeFinder(BluetoothDevice device){
        String deviceType="";
        int dMajor = device.getBluetoothClass().getMajorDeviceClass();
        int dMinor = device.getBluetoothClass().getDeviceClass();

        int dIcon = 0;

        deviceType += dMajor +"|"+dMinor;

        return deviceType;
    }
    public int deviceTypeFinder2(BluetoothDevice device){
        String deviceType="";
        int dMajor = device.getBluetoothClass().getMajorDeviceClass();
        int dMinor = device.getBluetoothClass().getDeviceClass();

        int dIcon = 0;

        deviceType += dMajor +"|"+dMinor;

        switch (dMajor){
            case 1024:
                // Audio/Video (headset,speaker,stereo, video display, vcr
                dIcon = R.drawable.ic_headset_black_24dp;
                break;
            case 256:
                // Computer (desktop,notebook, PDA, organizers, .... )
                dIcon = R.drawable.ic_computer_black_24dp;
                break;
            case 2304:
                // Health
                dIcon = R.drawable.ic_favorite_black_24dp;
                break;
            case 1536:
                // Imaging (printing, scanner, camera, display, ...)
                dIcon = R.drawable.ic_image_black_24dp;
                break;
            case 0:
                // Miscellaneous
                dIcon = R.drawable.ic_phonelink_black_24dp;
                break;
            case 768:
                // Network
                dIcon = R.drawable.ic_router_black_24dp;
                break;
            case 1280:
                // Peripheral (mouse, joystick, keyboards, ..... )
                dIcon = R.drawable.ic_mouse_black_24dp;
                break;
            case 512:
                // Phone (cellular, cordless, payphone, modem, ...)
                dIcon = R.drawable.ic_smartphone_black_24dp;
                break;
            case 2048:
                // Toy
                dIcon = R.drawable.ic_toys_black_24dp;
                break;
            case 7936:
                // Uncategorized, specific device code not specified
                dIcon = R.drawable.ic_devices_other_black_24dp;
                break;
            case 1792:
                // Wearable
                dIcon = R.drawable.ic_watch_black_24dp;
                break;
            default:
                dIcon = R.drawable.ic_devices_other_black_24dp;
                break;
        }


        return dIcon;
    }

}