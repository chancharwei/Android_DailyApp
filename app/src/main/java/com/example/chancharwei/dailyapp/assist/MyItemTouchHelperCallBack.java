package com.example.chancharwei.dailyapp.assist;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.example.chancharwei.dailyapp.MonitorERListActivity;

public class MyItemTouchHelperCallBack extends ItemTouchHelper.Callback{
    private static final String TAG = MyItemTouchHelperCallBack.class.getName();
    private static MonitorERListActivity mActivity;

    public MyItemTouchHelperCallBack(Activity activity){
        mActivity = (MonitorERListActivity)activity;
    }
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG,"getMovementFlags");
        int dragFlags = 0;//ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int swipeFlags = ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Log.d(TAG,"onMove");
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d(TAG,"onSwiped direction = "+direction);
        Log.d(TAG,"onSwiped "+viewHolder.getAdapterPosition());
        mActivity.deleteMonitorDataInList(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        Log.d(TAG,"isItemViewSwipeEnabled");
        //return super.isItemViewSwipeEnabled();
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        Log.d(TAG,"isLongPressDragEnabled");
        //return super.isLongPressDragEnabled();
        return true;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        Log.d(TAG,"onSelectedChanged");
        /*if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder.itemView.setScaleX(1.05f); viewHolder.itemView.setScaleY(1.05f);
        }*/
        //super.onSelectedChanged(viewHolder, actionState);
    }

}
