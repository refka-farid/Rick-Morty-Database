package com.shevaalex.android.rickmortydatabase.ui.character;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

class FilterLiveData extends MediatorLiveData<Pair<String, Integer>> {
    FilterLiveData(LiveData<String> searchQuery, LiveData<Integer> filterResultKey) {
        addSource(searchQuery, first -> setValue(Pair.create(first, filterResultKey.getValue())));
        addSource(filterResultKey, second -> setValue(Pair.create(searchQuery.getValue(), second)));
    }
}
