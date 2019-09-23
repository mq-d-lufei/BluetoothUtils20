package com.crazy.bluetoothutils.weight;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crazy.bluetoothutils.R;
import com.crazy.bluetoothutils.data.BltDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Created by Crazy on 2018/8/13.
 */
public class BltDialogFragment1 extends DialogFragment {

    private static final String BLT_LIST = "blt_list";

    private RecyclerView mRecyclerView;

    private BltDialogAdpter mBltDialogAdpter;

    private List<BltDevice> mDeviceList = new ArrayList<>();

    public interface OnItemClickListener {
        void onBltDialogSelected(BltDevice bltDevice);
    }

    private OnItemClickListener mListener;

    public static BltDialogFragment1 newInstance() {
        return new BltDialogFragment1();
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
        initRecyclerView();
        return new AlertDialog.Builder(getActivity())
                .setView(mRecyclerView)
               /* .setAdapter(new BltDialogAdpter(typeList), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        mListener.onDekaDialogSelected(typeList.get(position));
                    }
                })*/
                .create();

    }

    private void initRecyclerView() {
        mRecyclerView = new RecyclerView(getActivity());
        mRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mBltDialogAdpter = new BltDialogAdpter(mDeviceList);
        mRecyclerView.setAdapter(mBltDialogAdpter);
    }

    public void addBltDevice(BltDevice bltDevice) {
        mDeviceList.add(bltDevice);
        // mBltDialogAdpter.notifyItemInserted(mDeviceList.size() - 1);
        mBltDialogAdpter.notifyDataSetChanged();
    }


    public class BltDialogAdpter extends RecyclerView.Adapter<BltDialogAdpter.ViewHolder> {

        List<BltDevice> bltDeviceList;

        public BltDialogAdpter(List<BltDevice> bltDeviceList) {
            this.bltDeviceList = bltDeviceList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blue_device, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BltDevice bltDevice = bltDeviceList.get(position);
            holder.nameTv.setText(bltDevice.getName());
            holder.macTv.setText(bltDevice.getMac());
        }

        @Override
        public int getItemCount() {
            if (null != bltDeviceList) {
                bltDeviceList.size();
            }
            return 0;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTv;
            TextView macTv;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTv = itemView.findViewById(R.id.tv_blt_name);
                macTv = itemView.findViewById(R.id.tv_blt_mac);
            }
        }
    }


}
