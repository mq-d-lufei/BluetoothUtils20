package com.crazy.bluetoothutils.weight;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.crazy.bluetoothutils.R;
import com.crazy.bluetoothutils.data.BltDevice;

import java.util.ArrayList;

/**
 * Created by feaoes on 2018/6/12.
 */

public class BltDialogFragment extends DialogFragment {

    private ArrayList<BltDevice> mDeviceList = new ArrayList<>();

    private BltDialogAdapter mBltDialogAdapter;

    public interface OnItemClickListener {
        void onBltDialogSelected(BltDevice bltDevice);
    }

    private OnItemClickListener mListener;

    public static BltDialogFragment newInstance() {

        return new BltDialogFragment();
    }

    public void setItemClickListener(OnItemClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnItemClickListener) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDeviceList.clear();
        mBltDialogAdapter = new BltDialogAdapter(mDeviceList);
        return new AlertDialog.Builder(getActivity())
                .setAdapter(mBltDialogAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        mListener.onBltDialogSelected(mDeviceList.get(position));
                    }
                })
                .create();

    }

    public void addBltDevice(BltDevice bltDevice) {
        mDeviceList.add(bltDevice);
        mBltDialogAdapter.notifyDataSetChanged();
    }


    private class BltDialogAdapter extends BaseAdapter {

        private ArrayList<BltDevice> deviceList;

        public BltDialogAdapter(ArrayList<BltDevice> deviceList) {
            this.deviceList = deviceList;
        }

        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public BltDevice getItem(int position) {
            return deviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (null == view) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blue_device, parent, false);
                holder = new ViewHolder();
                holder.nameTv = (TextView) view.findViewById(R.id.tv_blt_name);
                holder.macTv = (TextView) view.findViewById(R.id.tv_blt_mac);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            BltDevice bltDevice = getItem(position);
            if (TextUtils.isEmpty(bltDevice.getName())) {
                holder.nameTv.setText("无");
            } else {
                holder.nameTv.setText(bltDevice.getName());
            }
            holder.macTv.setText(bltDevice.getMac());
            return view;
        }


        private class ViewHolder {
            TextView nameTv;
            TextView macTv;
        }
    }

}
