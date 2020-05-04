package com.shevaalex.android.rickmortydatabase.ui.character;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.PagedList;

import com.shevaalex.android.rickmortydatabase.source.MainRepository;
import com.shevaalex.android.rickmortydatabase.source.database.Character;
import com.shevaalex.android.rickmortydatabase.source.database.Episode;
import com.shevaalex.android.rickmortydatabase.source.database.Location;

import java.util.List;


public class CharacterViewModel extends AndroidViewModel {
    private final MainRepository rmRepository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<Integer> filterResultKey = new MutableLiveData<>();
    private LiveData<PagedList<Character>> mCharacterList;



    public CharacterViewModel(@NonNull Application application) {
        super(application);
        rmRepository = MainRepository.getInstance(application);
    }

    void setNameQuery(String name) {
        this.searchQuery.setValue(name);
    }

    void setFilter(Integer key) {
        this.filterResultKey.setValue(key);
    }

    LiveData<PagedList<Character>> getCharacterList() {
        if (mCharacterList == null) {
            FilterLiveData trigger = new FilterLiveData(searchQuery, filterResultKey);
            mCharacterList = Transformations.switchMap(trigger,
                    value -> rmRepository.getCharacterListFiltered(value.first, value.second));
        }
        return mCharacterList;
    }

    boolean dbIsNotSynced() {
        return !rmRepository.dbIsUpToDate();
    }

    void syncDb() {
        rmRepository.syncDatabase();
    }

    LiveData<Integer> getFilterResultKey() {
        return filterResultKey;
    }

    LiveData<String> getSearchQuery() { return searchQuery;   }

    Location getLocationById (int id) { return rmRepository.getLocationById(id); }

    LiveData<List<Episode>> getEpisodeList(int characterId) {
        return rmRepository.getEpisodesFromCharacter(characterId);
    }

}
