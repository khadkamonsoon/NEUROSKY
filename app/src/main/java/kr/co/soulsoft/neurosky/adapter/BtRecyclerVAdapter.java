package kr.co.soulsoft.neurosky.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kr.co.soulsoft.neurosky.R;

public class BtRecyclerVAdapter extends RecyclerView.Adapter<BtRecyclerVAdapter.MyViewHolder> {
    private final ArrayList<BluetoothDevice> btDevices;
    private OnItemClickListener onItemClickListener;

    public void OnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public BtRecyclerVAdapter(ArrayList<BluetoothDevice> btDevices){
        this.btDevices = btDevices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.bt_list_item,parent,false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txtBtName.setText(btDevices.get(position).getName());
        holder.txtBtName.setOnClickListener(view -> {
            onItemClickListener.onDeviceSelection(btDevices.get(position));
        });
    }


    @Override
    public int getItemCount() {
        return btDevices.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtBtName;
        public MyViewHolder(View itemView)
        {
            super(itemView);
            this.txtBtName = itemView.findViewById(R.id.txtBTName);
        }
    }

    public interface OnItemClickListener {
        void onDeviceSelection(BluetoothDevice btDevice);
    }
}
