package com.diegomfv.android.realestatemanager.ui.activities;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.diegomfv.android.realestatemanager.R;
import com.diegomfv.android.realestatemanager.RealEstateManagerApp;
import com.diegomfv.android.realestatemanager.constants.Constants;
import com.diegomfv.android.realestatemanager.data.AppDatabase;
import com.diegomfv.android.realestatemanager.data.entities.ImageRealEstate;
import com.diegomfv.android.realestatemanager.data.entities.PlaceRealEstate;
import com.diegomfv.android.realestatemanager.data.entities.RealEstate;
import com.diegomfv.android.realestatemanager.ui.rest.FragmentItemDescription;
import com.diegomfv.android.realestatemanager.ui.rest.FragmentListListings;
import com.diegomfv.android.realestatemanager.utils.ToastHelper;
import com.diegomfv.android.realestatemanager.utils.Utils;
import com.snatik.storage.Storage;

import java.util.List;

/** How crashes were solved:
 * 1. Modified the id of the view from activity_second_activity_text_view_main
 * to activity_main_activity_text_view_quantity
 * 2. Add String.valueOf() to convert int to String
 * */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean accessInternalStorageGranted;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called!");

        this.accessInternalStorageGranted = false;

        getApp().getRepository().deleteCache();

        ////////////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_main);

        this.loadFragmentOrFragments();

        this.checkInternalStoragePermissionGranted();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: called!");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: called!");

        switch (item.getItemId()) {

            case R.id.menu_add_listing_button: {

                if (accessInternalStorageGranted) {
                    Utils.launchActivity(this, CreateNewListingActivity.class);

                } else {
                    ToastHelper.toastSomeAccessNotGranted(this);
                }


            } break;

            case R.id.menu_position_button: {
                Utils.launchActivity(this, PositionActivity.class);

            } break;

            case R.id.menu_change_currency_button: {

                // TODO: 23/08/2018 Code

            } break;

            case R.id.menu_edit_listing_button: {

                if (accessInternalStorageGranted) {
                    Utils.launchActivity(this, EditListingActivity.class);

                } else {
                    ToastHelper.toastSomeAccessNotGranted(this);
                }

            } break;

            case R.id.menu_search_button: {
                Utils.launchActivity(this, SearchEngineActivity.class);

            } break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: called!");

        switch (requestCode) {

            case Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE: {

                if (grantResults.length > 0 && grantResults[0] != -1) {
                    accessInternalStorageGranted = true;
                    createDirectories();
                }
            }
            break;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //SINGLETON GETTERS

    private RealEstateManagerApp getApp () {
        Log.d(TAG, "getApp: called");
        return (RealEstateManagerApp) getApplication();
    }

    private AppDatabase getAppDatabase () {
        Log.d(TAG, "getAppDatabase: called!");
        return getApp().getDatabase();
    }

    private Storage getInternalStorage() {
        Log.d(TAG, "getInternalStorage: called!");
        return getApp().getInternalStorage();
    }

    private RealEstate getRealEstateCache () {
        Log.d(TAG, "getRealEstateCache: called!");
        return getApp().getRepository().getRealEstateCache();
    }

    private List<ImageRealEstate> getListOfImagesRealEstateCache () {
        Log.d(TAG, "getListOfImagesRealEstateCache: called!");
        return getApp().getRepository().getListOfImagesRealEstateCache();
    }

    private List<PlaceRealEstate> getListOfPlacesByNearbyCache () {
        Log.d(TAG, "getListOfImagesRealEstateCache: called!");
        return getApp().getRepository().getListOfPlacesRealEstateCache();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /** Method that loads one or two fragments depending on the device
     * */
    private void loadFragmentOrFragments() {
        Log.d(TAG, "loadFragmentOrFragments: called!");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment1_container_id, FragmentListListings.newInstance())
                .commit();

        /* Only load the fragment if we are in a tablet
        * */
        if (findViewById(R.id.fragment2_container_id) != null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment2_container_id, FragmentItemDescription.newInstance())
                    .commit();

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkInternalStoragePermissionGranted() {
        Log.d(TAG, "checkInternalStoragePermissionGranted: called!");

        if (Utils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            accessInternalStorageGranted = true;
            createDirectories();

        } else {
            Utils.requestPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void createDirectories () {
        Log.d(TAG, "createDirectories: called");

        if (accessInternalStorageGranted) {
            if (!getInternalStorage().isDirectoryExists(getApp().getImagesDir())) {
                getInternalStorage().createDirectory(getApp().getImagesDir());
            }

            if (!getInternalStorage().isDirectoryExists(getApp().getTemporaryDir())) {
                getInternalStorage().createDirectory(getApp().getTemporaryDir());
            }
        }
    }
}
