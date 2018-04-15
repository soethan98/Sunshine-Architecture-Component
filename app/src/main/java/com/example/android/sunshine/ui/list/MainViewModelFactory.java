package com.example.android.sunshine.ui.list;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;

import com.example.android.sunshine.data.SunshineRepository;
import com.example.android.sunshine.ui.detail.DetailActivityViewModel;

import java.util.Date;

/**
 * Created by soe_than on 4/15/18.
 */

public class MainViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final SunshineRepository mRepository;


    public MainViewModelFactory(SunshineRepository repository) {
        this.mRepository = repository;

    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MainActivityViewModel(mRepository);
    }
}
