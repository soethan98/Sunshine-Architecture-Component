///*
// * Copyright (C) 2017 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
package com.example.android.sunshine.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.android.sunshine.AppExecutors;
import com.example.android.sunshine.data.database.ListWeatherEntry;
import com.example.android.sunshine.data.database.WeatherDao;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.data.network.WeatherNetworkDataSource;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import java.util.Date;
import java.util.List;

///**
// * Handles data operations in Sunshine. Acts as a mediator between {@link WeatherNetworkDataSource}
// * and {@link WeatherDao}
// */
public class SunshineRepository {
    private static final String LOG_TAG = SunshineRepository.class.getSimpleName();
    //
//    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static SunshineRepository sInstance;
    private final WeatherDao mWeatherDao;
    private final WeatherNetworkDataSource mWeatherNetworkDataSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;


    private SunshineRepository(WeatherDao weatherDao,
                               WeatherNetworkDataSource weatherNetworkDataSource,
                               AppExecutors executors) {
        mWeatherDao = weatherDao;
        mWeatherNetworkDataSource = weatherNetworkDataSource;
        mExecutors = executors;

        LiveData<WeatherEntry[]> networkData = mWeatherNetworkDataSource.getCurrentWeatherforecast();
        networkData.observeForever(weatherEntries -> {
            mExecutors.diskIO().execute(() -> {
                deleteOldData();
                Log.d(LOG_TAG, "Old weather deleted");
                mWeatherDao.bulkInsert(weatherEntries);
            });
        });


    }

    public synchronized static SunshineRepository getInstance(
            WeatherDao weatherDao, WeatherNetworkDataSource weatherNetworkDataSource,
            AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new SunshineRepository(weatherDao, weatherNetworkDataSource,
                        executors);
                Log.d(LOG_TAG, "Made new repository");
            }
        }
        return sInstance;
    }


    //
//    /**
//     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
//     * immediate sync is required, this method will take care of making sure that sync occurs.
//     */
    private synchronized void initializeData() {

        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return;
        mInitialized = true;


        mWeatherNetworkDataSource.scheduleRecurringFetchWeatherSync();

        mExecutors.diskIO().execute(() -> {
            if (isFetchNeeded()) {

                startFetchWeatherService();
            }
        });


    }

    //
//    /**
//     * Database related operations
//     **/
//
//    /**
//     * Deletes old weather data because we don't need to keep multiple days' data
//     */
    private void deleteOldData() {
        Date date = SunshineDateUtils.getNormalizedUtcDateForToday();
        mWeatherDao.deleteOldWeather(date);
    }

    //
//    /**
//     * Checks if there are enough days of future weather for the app to display all the needed data.
//     *
//     * @return Whether a fetch is needed
//     */
    private boolean isFetchNeeded() {
        Date date = SunshineDateUtils.getNormalizedUtcDateForToday();
        int count = mWeatherDao.countAllFutureDate(date);
        Log.i(SunshineRepository.LOG_TAG, count + "");
        return (count < WeatherNetworkDataSource.NUM_DAYS);
    }

    //
//    /**
//     * Network related operation
//     */
//
    private void startFetchWeatherService() {

        mWeatherNetworkDataSource.startFetchWeatherService();
        // TODO Finish this method when instructed
    }

    public LiveData<WeatherEntry> getWeatherByDate(Date date) {

        initializeData();
        return mWeatherDao.getWeatherByDate(date);
    }

    public LiveData<List<ListWeatherEntry>> getCurrentWeatherforecast() {
        initializeData();
        Date date = SunshineDateUtils.getNormalizedUtcDateForToday();
        return mWeatherDao.getCurrentWeatherForecast(date);
    }
//
}