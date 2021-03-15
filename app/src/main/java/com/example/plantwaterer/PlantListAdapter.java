package com.example.plantwaterer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PlantListAdapter extends BaseAdapter {

    private Context context;
    private int layout;
    private ArrayList<Plant> recordList;

    public PlantListAdapter(Context context, int layout, ArrayList<Plant> recordList) {
        this.context = context;
        this.layout = layout;
        this.recordList = recordList;
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int position) {
        return recordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView txtName, txtTime, txtAmount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ViewHolder holder = new ViewHolder();

        if(row==null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(layout,null);
            holder.txtName = row.findViewById(R.id.txtName);
            holder.txtTime = row.findViewById(R.id.txtTime);
            holder.txtAmount = row.findViewById(R.id.txtAmount);
            holder.imageView = row.findViewById(R.id.pimgIcon);
            row.setTag(holder);
        }
        else{
            holder = (ViewHolder) row.getTag();
        }

        Plant plant = recordList.get(position);
        holder.txtName.setText(plant.getName());
        holder.txtTime.setText(plant.getTime());
        holder.txtAmount.setText(plant.getAmount());

        byte[] recordImage = plant.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(recordImage,0,recordImage.length);
        holder.imageView.setImageBitmap(bitmap);

        return row;
    }
}

