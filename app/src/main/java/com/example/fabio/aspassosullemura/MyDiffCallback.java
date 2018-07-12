package com.example.fabio.aspassosullemura;

import android.support.v7.util.DiffUtil;

import org.jetbrains.annotations.Nullable;

import java.util.List;

//preso da : https://medium.com/@iammert/using-diffutil-in-android-recyclerview-bdca8e4fbb00

public class MyDiffCallback extends DiffUtil.Callback {
    private List<InterPlaces> oldInterPlaces;
    private List<InterPlaces> newInterPlaces;

    public MyDiffCallback(List<InterPlaces> newIP, List<InterPlaces> oldIP) {
        this.newInterPlaces = newIP;
        this.oldInterPlaces = oldIP;
    }

    @Override
    public int getOldListSize() {
        return oldInterPlaces.size();
    }

    @Override
    public int getNewListSize() {
        return newInterPlaces.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldInterPlaces.get(oldItemPosition).getName() == newInterPlaces.get(newItemPosition).getName();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldInterPlaces.get(oldItemPosition).getName().equals(newInterPlaces.get(newItemPosition).getName());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}

