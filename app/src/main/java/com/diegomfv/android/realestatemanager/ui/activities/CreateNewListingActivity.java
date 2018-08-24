package com.diegomfv.android.realestatemanager.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.diegomfv.android.realestatemanager.R;
import com.diegomfv.android.realestatemanager.RealEstateManagerApp;
import com.diegomfv.android.realestatemanager.adapters.RVAdapterMediaHorizontal;
import com.diegomfv.android.realestatemanager.constants.Constants;
import com.diegomfv.android.realestatemanager.data.AppDatabase;
import com.diegomfv.android.realestatemanager.data.datamodels.AddressRealEstate;
import com.diegomfv.android.realestatemanager.data.entities.ImageRealEstate;
import com.diegomfv.android.realestatemanager.data.entities.PlaceRealEstate;
import com.diegomfv.android.realestatemanager.data.entities.RealEstate;
import com.diegomfv.android.realestatemanager.ui.dialogfragments.InsertAddressDialogFragment;
import com.diegomfv.android.realestatemanager.network.models.placebynearby.LatLngForRetrofit;
import com.diegomfv.android.realestatemanager.network.models.placebynearby.PlacesByNearby;
import com.diegomfv.android.realestatemanager.network.models.placedetails.PlaceDetails;
import com.diegomfv.android.realestatemanager.network.models.placefindplacefromtext.PlaceFromText;
import com.diegomfv.android.realestatemanager.network.remote.GoogleServiceStreams;
import com.diegomfv.android.realestatemanager.receivers.InternetConnectionReceiver;
import com.diegomfv.android.realestatemanager.utils.FirebasePushIdGenerator;
import com.diegomfv.android.realestatemanager.utils.ItemClickSupport;
import com.diegomfv.android.realestatemanager.utils.TextInputAutoCompleteTextView;
import com.diegomfv.android.realestatemanager.utils.ToastHelper;
import com.diegomfv.android.realestatemanager.utils.Utils;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Diego Fajardo on 18/08/2018.
 */
// TODO: 24/08/2018 Allow writing the information in EUROS
// TODO: 23/08/2018 If back button clicked when searching for an image, the app crashes
public class CreateNewListingActivity extends AppCompatActivity implements Observer, InsertAddressDialogFragment.InsertAddressDialogListener {

    private static final String TAG = CreateNewListingActivity.class.getSimpleName();

    /////////////////////////////////

    @BindView(R.id.progress_bar_content_id)
    LinearLayout progressBarContent;

    @BindView(R.id.main_layout_id)
    ScrollView mainLayout;

    @BindView(R.id.card_view_type_id)
    CardView cardViewType;

    @BindView(R.id.card_view_price_id)
    CardView cardViewPrice;

    @BindView(R.id.card_view_surface_area_id)
    CardView cardViewSurfaceArea;

    @BindView(R.id.card_view_number_rooms_id)
    CardView cardViewNumberOfRooms;

    @BindView(R.id.card_view_description_id)
    CardView cardViewDescription;

    @BindView(R.id.card_view_address_id)
    CardView cardViewAddress;

    private TextInputAutoCompleteTextView tvTypeOfBuilding;

    private TextInputAutoCompleteTextView tvPrice;

    private TextInputAutoCompleteTextView tvSurfaceArea;

    private TextInputAutoCompleteTextView tvNumberOfRooms;

    private TextInputAutoCompleteTextView tvDescription;

    private TextInputEditText tvAddress;

    @BindView(R.id.button_add_address_id)
    Button buttonAddAddress;

    @BindView(R.id.recyclerView_media_id)
    RecyclerView recyclerView;

    @BindView(R.id.button_add_photo_id)
    Button buttonAddPhoto;

    @BindView(R.id.button_insert_listing_id)
    Button buttonInsertListing;

    /////////////////////////////////

    private ActionBar actionBar;

    //RecyclerView Adapter
    private RVAdapterMediaHorizontal adapter;

    private RequestManager glide;

    private boolean accessInternalStorageGranted;

    private int counter;

    private Unbinder unbinder;

    private List<Bitmap> listOfBitmaps;

    //InternetConnectionReceiver variables
    private InternetConnectionReceiver receiver;
    private IntentFilter intentFilter;
    private Snackbar snackbar;
    private boolean isInternetAvailable;

    /////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called!");

        this.listOfBitmaps = new ArrayList<>();

        this.accessInternalStorageGranted = false;
        this.isInternetAvailable = false;

        this.counter = 0;

        this.glide = Glide.with(CreateNewListingActivity.this);

        ////////////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.insert_information_layout);
        setTitle("Create a New Listing");
        this.unbinder = ButterKnife.bind(this);

        this.configureActionBar();

        this.configureLayout();

        Utils.showMainContent(progressBarContent, mainLayout);

        this.updateViews();

        this.checkInternalStoragePermissionGranted();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        this.connectBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.disconnectBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");
        this.disconnectBroadcastReceiver();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called!");
        // TODO: 19/08/2018 Might need to change this

        // TODO: 19/08/2018 Add a fragment saying, would you like to delete the media?
        // TODO: 19/08/2018 If yes, clean the list and the HashMap 
        //do nothing
    }

    @Override
    public void update(Observable o, Object internetAvailable) {
        Log.d(TAG, "update: called!");
        isInternetAvailable = Utils.setInternetAvailability(internetAvailable);
        snackbarConfiguration();
    }

    @OnClick ({R.id.button_add_address_id, R.id.button_add_photo_id, R.id.button_insert_listing_id})
    public void buttonClicked (View view) {
        Log.d(TAG, "buttonClicked: " + ((Button)view).getText().toString() + " clicked!");

        switch (view.getId()) {

            case R.id.button_add_address_id: {
                launchInsertAddressDialog();

            } break;

            case R.id.button_add_photo_id: {
                launchAddPhotoActivity();

            } break;

            case R.id.button_insert_listing_id: {

                // TODO: 24/08/2018 Check that we have all the necessary information!
                // TODO: 24/08/2018 If there was no internet, we might not have all!
                // TODO: 24/08/2018 Use the broadcastreceiver to check the repository caches
                // TODO: 24/08/2018 If they are empty, cache information with request
                // TODO: 24/08/2018 Check also that types are correct
                // TODO: 24/08/2018 NOTIFY the user in ALLCHECKSARECORRECT()
                if (allChecksCorrect()) {
                    insertListing();
                }


            } break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: called!");

        switch (requestCode) {

            case Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE: {

                if (grantResults.length > 0 && grantResults[0] != -1) {
                    accessInternalStorageGranted = true;
                    getBitmapImagesFromImagesFiles();
                }
            }
            break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: called!");

        switch (item.getItemId()) {

            case android.R.id.home: {
                Utils.launchActivity(this, MainActivity.class);

            } break;

        }
        return super.onOptionsItemSelected(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureActionBar() {
        Log.d(TAG, "configureActionBar: called!");

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeActionContentDescription(getResources().getString(R.string.go_back));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureLayout() {
        Log.d(TAG, "configureLayout: called!");

        this.getAutocompleteTextViews();
        this.setAllHints();
    }

    private void getAutocompleteTextViews () {
        Log.d(TAG, "getAutocompleteTextViews: called!");

        this.tvTypeOfBuilding = cardViewType.findViewById(R.id.text_input_layout_id).findViewById(R.id.text_input_autocomplete_text_view_id);
        this.tvPrice = cardViewPrice.findViewById(R.id.text_input_layout_id).findViewById(R.id.text_input_autocomplete_text_view_id);
        this.tvSurfaceArea = cardViewSurfaceArea.findViewById(R.id.text_input_layout_id).findViewById(R.id.text_input_autocomplete_text_view_id);
        this.tvNumberOfRooms = cardViewNumberOfRooms.findViewById(R.id.text_input_layout_id).findViewById(R.id.text_input_autocomplete_text_view_id);
        this.tvDescription = cardViewDescription.findViewById(R.id.text_input_layout_id).findViewById(R.id.text_input_autocomplete_text_view_id);
        this.tvAddress = cardViewAddress.findViewById(R.id.text_input_layout_id).findViewById(R.id.text_input_edit_text_id);
    }

    private void setAllHints() {
        Log.d(TAG, "setAllHints: called!");

        // TODO: 23/08/2018 Use Resources instead of hardcoded

        setHint(cardViewType, "Type");
        setHint(cardViewPrice, "Price ($)");
        setHint(cardViewSurfaceArea, "Surface Area (sqm)");
        setHint(cardViewNumberOfRooms, "Number of Rooms");
        setHint(cardViewDescription, "Description");
        setHint(cardViewAddress, "AddressRealEstate");
    }

    private void setHint (CardView cardView, String hint) {
        Log.d(TAG, "setHint: called!");

        TextInputLayout textInputLayout = cardView.findViewById(R.id.text_input_layout_id);
        textInputLayout.setHint(hint);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //DIALOG FRAGMENT

    private void launchInsertAddressDialog() {
        Log.d(TAG, "launchInsertAddressDialog: called!");

        DialogFragment dialog = new InsertAddressDialogFragment();
        dialog.show(getSupportFragmentManager(), "InsertAddressDialogFragment");

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment, AddressRealEstate addressRealEstate) {
        Log.d(TAG, "onDialogPositiveClick: called!");

        checkAddressIsValid(addressRealEstate);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialogFragment) {
        Log.d(TAG, "onDialogNegativeClick: called!");

        ToastHelper.toastShort(this, "The address was not added");
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

    private List<PlaceRealEstate> getListOfPlacesRealEstateCache() {
        Log.d(TAG, "getListOfImagesRealEstateCache: called!");
        return getApp().getRepository().getListOfPlacesRealEstateCache();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean allChecksCorrect () {
        Log.d(TAG, "allChecksCorrect: called!");

        // TODO: 24/08/2018 DO THIS!

        return true;
    }

    private void insertListing() {
        Log.d(TAG, "insertListing: called!");

        Utils.hideMainContent(progressBarContent, mainLayout);

        /* Start insertion process
        * */
        insertRealEstateObject();
    }

    @SuppressLint("CheckResult")
    private void insertRealEstateObject() {
        Log.d(TAG, "insertRealEstateObject: called!");

        updateRealEstateCacheId();
        updateRealEstateCache();
        updateImagesIdRealEstateCache();
        updateDatePutRealEstateCacheCache();

        Single.just(getAppDatabase().realStateDao().insertRealEstate(getRealEstateCache()))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<Long>() {
                    @Override
                    public void onSuccess(Long aLong) {
                        Log.d(TAG, "onSuccess: called!");
                        insertListImageRealEstate();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void insertListImageRealEstate() {
        Log.d(TAG, "insertListImageRealEstate: called!");

        Single.just(getAppDatabase().imageRealEstateDao().insertListOfImagesRealEstate(getListOfImagesRealEstateCache()))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<long[]>() {
                    @Override
                    public void onSuccess(long[] longs) {
                        Log.d(TAG, "onSuccess: called!");

                        insertListPlacesRealEstate();
                        copyAllBitmapsFromTemporaryDirectoryToImageDirectory();

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());

                    }
                });
    }

    @SuppressLint("CheckResult")
    public void insertListPlacesRealEstate () {
        Log.d(TAG, "insertListPlacesRealEstate: called");

        // TODO: 23/08/2018 Could be done just with App Executors
        Single.just(getAppDatabase().placeRealEstateDao().insertListOfPlaceRealEstate(getListOfPlacesRealEstateCache()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<long[]>() {
                    @Override
                    public void onSuccess(long[] longs) {
                        Log.d(TAG, "onSuccess: called!");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //CACHE

    private void updateViews() {
        Log.d(TAG, "updateViews: called!");

        this.tvTypeOfBuilding.setText(getRealEstateCache().getType());
        this.tvPrice.setText(String.valueOf(getRealEstateCache().getPrice()));
        this.tvSurfaceArea.setText(String.valueOf(getRealEstateCache().getSurfaceArea()));
        this.tvNumberOfRooms.setText(String.valueOf(getRealEstateCache().getNumberOfRooms()));
        this.tvDescription.setText(getRealEstateCache().getDescription());
        this.tvAddress.setText(Utils.setTextOfTextViewUsingAddressFromRealEstate(getRealEstateCache()));
    }

    private void updateRealEstateCache() {
        Log.d(TAG, "updateRealEstateCache: called!");

        this.updateStringValues();
        this.updateIntegerValues();
    }

    private void updateIntegerValues() {
        Log.d(TAG, "updateIntegerValues: called!");

        this.getRealEstateCache().setPrice(Utils.getTextViewInteger(tvPrice));
        this.getRealEstateCache().setSurfaceArea(Utils.getTextViewInteger(tvSurfaceArea));
        this.getRealEstateCache().setNumberOfRooms(Utils.getTextViewInteger(tvNumberOfRooms));
    }

    private void updateStringValues() {
        Log.d(TAG, "updateStringValues: called!");

        this.getRealEstateCache().setType(tvTypeOfBuilding.getText().toString().trim());
        this.getRealEstateCache().setDescription(tvDescription.getText().toString().trim());
    }

    private void updateRealEstateCacheId() {
        Log.d(TAG, "updateRealEstateCacheId: called!");
        getRealEstateCache().setId(FirebasePushIdGenerator.generate());
    }

    private void updateImagesIdRealEstateCache() {
        Log.d(TAG, "updateImagesIdRealEstateCache: called!");

        List<String> listOfImagesIds = new ArrayList<>();

        for (int i = 0; i < getListOfImagesRealEstateCache().size(); i++) {
            listOfImagesIds.add(getListOfImagesRealEstateCache().get(i).getId());
        }
        getRealEstateCache().setListOfImagesIds(listOfImagesIds);
    }

    private void updateDatePutRealEstateCacheCache() {
        Log.d(TAG, "updateDatePutRealEstateCacheCache: called!");
        getRealEstateCache().setDatePut(Utils.getTodayDate());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //NETWORK

    private void checkAddressIsValid(AddressRealEstate addressRealEstate) {
        Log.d(TAG, "checkIfAddressIsValid: called!");

        if (isInternetAvailable) {
            getPlaceFromText(addressRealEstate);

        } else {
            ToastHelper.toastShort(this, "Internet is not available, AddressRealEstate cannot be saved");

        }
    }

    private void updateRealEstateCacheWithAddress(AddressRealEstate addressRealEstate) {
        Log.d(TAG, "updateRealEstateCacheWithAddress: called!");
        getRealEstateCache().getAddress().setStreet(addressRealEstate.getStreet());
        getRealEstateCache().getAddress().setLocality(addressRealEstate.getLocality());
        getRealEstateCache().getAddress().setCity(addressRealEstate.getCity());
        getRealEstateCache().getAddress().setPostcode(addressRealEstate.getPostcode());
    }

    @SuppressLint("CheckResult")
    private void getPlaceFromText(final AddressRealEstate addressRealEstate) {
        Log.d(TAG, "getPlaceFromText: called!");

        GoogleServiceStreams.streamFetchPlaceFromText(
                addressRealEstate.getStreet() + ","
                        + addressRealEstate.getLocality() + ","
                        + addressRealEstate.getCity()+ ","
                        + addressRealEstate.getPostcode(),
                "textquery",
                getResources().getString(R.string.a_k_p))
                .subscribeWith(new DisposableObserver<PlaceFromText>() {
                    @Override
                    public void onNext(PlaceFromText placeFromText) {
                        Log.d(TAG, "onNext: called!");

                        if (Utils.checksPlaceFromText(placeFromText)) {

                            ToastHelper.toastShort(CreateNewListingActivity.this,
                                    "The address is valid");

                            /* Fill the address of the cache
                            * */
                            updateRealEstateCacheWithAddress(addressRealEstate);

                            /* Set the address in the textView
                            * */
                            tvAddress.setText(Utils.setTextOfTextViewUsingAddressFromRealEstate(getRealEstateCache()));

                            /* Get place details
                            * */
                            getPlaceDetails(placeFromText.getCandidates().get(0).getPlaceId());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: called!");
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void getPlaceDetails(String placeId) {
        Log.d(TAG, "getPlaceDetails: called!");

        GoogleServiceStreams.streamFetchPlaceDetails(
                placeId,
                getResources().getString(R.string.a_k_p))
                .subscribeWith(new DisposableObserver<PlaceDetails>() {
                    @Override
                    public void onNext(PlaceDetails placeDetails) {
                        Log.d(TAG, "onNext: called!");

                        if (Utils.checksPlaceDetails(placeDetails)) {
                            getRealEstateCache().setLatitude(placeDetails.getResult().getGeometry().getLocation().getLat());
                            getRealEstateCache().setLongitude(placeDetails.getResult().getGeometry().getLocation().getLng());

                            /* We use the latitude and longitude to fetch nearby places
                            * */
                            getNearbyPlaces(
                                    placeDetails.getResult().getGeometry().getLocation().getLat(),
                                    placeDetails.getResult().getGeometry().getLocation().getLng());



                        } else {
                            ToastHelper.toastShort(CreateNewListingActivity.this,
                                    "There is a problem with the latitude and longitude");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());

                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: called!");

                    }
                });
    }

    @SuppressLint("CheckResult")
    private void getNearbyPlaces (double latitude, double longitude) {
        Log.d(TAG, "getNearbyPlaces: called!");

        /* Clear the cache
        * */
        getListOfPlacesRealEstateCache().clear();

        // TODO: 22/08/2018 Constraint the search with types!

        GoogleServiceStreams.streamFetchPlacesNearby(
                new LatLngForRetrofit(latitude,longitude),
                Constants.FETCH_NEARBY_RANKBY,
                getResources().getString(R.string.a_k_p))
                .subscribeWith(new DisposableObserver<PlacesByNearby>() {
                    @Override
                    public void onNext(PlacesByNearby placesByNearby) {
                        Log.d(TAG, "onNext: called!");

                        if (Utils.checkPlacesByNearbyResults(placesByNearby)) {

                            PlaceRealEstate placeRealEstate;
                            List<String> listPlaceRealEstateIds = new ArrayList<>();

                            for (int i = 0; i < placesByNearby.getResults().size(); i++) {

                                if (Utils.checkResultPlacesByNearby(placesByNearby.getResults().get(i))) {

                                    placeRealEstate = new PlaceRealEstate(
                                            FirebasePushIdGenerator.generate(),
                                            placesByNearby.getResults().get(i).getPlaceId(),
                                            placesByNearby.getResults().get(i).getName(),
                                            placesByNearby.getResults().get(i).getVicinity(),
                                            placesByNearby.getResults().get(i).getTypes(),
                                            placesByNearby.getResults().get(i).getGeometry().getLocation().getLat(),
                                            placesByNearby.getResults().get(i).getGeometry().getLocation().getLng());

                                    /* If the result passes all checks, add the place to the cache
                                    * */
                                    getListOfPlacesRealEstateCache().add(placeRealEstate);
                                    listPlaceRealEstateIds.add(placeRealEstate.getId());

                                }
                            }
                            getRealEstateCache().setListOfNearbyPointsOfInterestIds(listPlaceRealEstateIds);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());

                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: called!");

                    }
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //INTERNET CONNECTION RECEIVER

    /** Method that connects a broadcastReceiver to the activity.
     * It allows to notify the user about the internet state
     * */
    private void connectBroadcastReceiver () {
        Log.d(TAG, "connectBroadcastReceiver: called!");

        receiver = new InternetConnectionReceiver();
        intentFilter = new IntentFilter(Constants.CONNECTIVITY_CHANGE_STATUS);
        Utils.connectReceiver(this, receiver, intentFilter, this);
    }

    /** Method that disconnects the broadcastReceiver from the activity.
     * */
    private void disconnectBroadcastReceiver () {
        Log.d(TAG, "disconnectBroadcastReceiver: called!");

        if (receiver != null) {
            Utils.disconnectReceiver(
                    this,
                    receiver,
                    this);
        }
        receiver = null;
        intentFilter = null;
        snackbar = null;
    }

    private void snackbarConfiguration () {
        Log.d(TAG, "snackbarConfiguration: called!");

        if (isInternetAvailable) {
            if (snackbar != null) {
                snackbar.dismiss();
            }

        } else {
            if (snackbar == null) {
                snackbar = Utils.createSnackbar(
                        this,
                        mainLayout,
                        getResources().getString(R.string.noInternet));

            } else {
                snackbar.show();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //INTERNAL STORAGE

    private void checkInternalStoragePermissionGranted() {
        Log.d(TAG, "checkInternalStoragePermissionGranted: called!");

        if (Utils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            accessInternalStorageGranted = true;
            getBitmapImagesFromImagesFiles();

        } else {
            Utils.requestPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
    }

    @SuppressLint("CheckResult")
    private void getBitmapImagesFromImagesFiles() {
        Log.d(TAG, "getBitmapImagesFromImagesFiles: called!");

        if (accessInternalStorageGranted) {

            String mainPath = getInternalStorage().getInternalFilesDirectory() + File.separator;
            String temporaryDir = mainPath + File.separator + Constants.TEMPORARY_DIRECTORY + File.separator;

            counter = getListOfImagesRealEstateCache().size();

            for (int i = 0; i < getListOfImagesRealEstateCache().size(); i++) {

                Single.just(getInternalStorage().readFile(temporaryDir + getListOfImagesRealEstateCache().get(i).getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<byte[]>() {
                            @Override
                            public void onSuccess(byte[] data) {
                                Log.i(TAG, "onSuccess: called!");

                                listOfBitmaps.add(BitmapFactory.decodeByteArray(data, 0 , data.length));

                                counter--;
                                if (counter == 0) {
                                    configureRecyclerView();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: " + e.getMessage());

                            }
                        });
            }
        }
    }

    @SuppressLint("CheckResult")
    public void copyAllBitmapsFromTemporaryDirectoryToImageDirectory() {
        Log.d(TAG, "copyAllBitmapsFromTemporaryDirectoryToImageDirectory: called!");

        if (accessInternalStorageGranted) {

            List<File> listOfTempFiles = getInternalStorage().getFiles(getApp().getTemporaryDir());

            byte[] temporaryFileByteArray;
            String pushKey;

            for (int i = 0; i < listOfTempFiles.size(); i++) {

                pushKey = listOfTempFiles.get(i).getName();
                temporaryFileByteArray = getInternalStorage().readFile(getApp().getTemporaryDir() + pushKey);

                if (getInternalStorage().isDirectoryExists(getApp().getImagesDir())) {
                    createFileInImagesDir(getApp().getImagesDir() + pushKey, temporaryFileByteArray);

                } else {
                    getInternalStorage().createDirectory(getApp().getImagesDir());
                    createFileInImagesDir(getApp().getImagesDir() + pushKey, temporaryFileByteArray);
                }
            }

            Utils.launchActivity(this, AuthLoginActivity.class);
            createNotification();

        } else {
                // TODO: 18/08/2018 Create a dialog asking for permissions! It should load configure internal storage again!
                ToastHelper.toastInternalStorageAccessNotGranted(this);

        }

    }

    @SuppressLint("CheckResult")
    private void createFileInImagesDir (String filePath, byte[] file) {
        Log.d(TAG, "createFileInImagesDir: called!");

        Single.just(getInternalStorage().createFile(filePath, file))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean fileIsCreated) {
                        Log.i(TAG, "onSuccess: called!");

                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError: called!");
                        ToastHelper.toastThereWasAnError(CreateNewListingActivity.this);
                    }
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureRecyclerView() {
        Log.d(TAG, "configureRecyclerView: called!");

        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        this.adapter = new RVAdapterMediaHorizontal(
                this,
                listOfBitmaps,
                glide);
        this.recyclerView.setAdapter(this.adapter);

        this.configureOnClickRecyclerView();

    }

    private void configureOnClickRecyclerView () {
        Log.d(TAG, "configureOnClickRecyclerView: called!");

        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Log.d(TAG, "onItemClicked: item(" + position + ") clicked!");
                        ToastHelper.toastShort(CreateNewListingActivity.this, getListOfImagesRealEstateCache().get(position).getDescription());
                    }
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void launchAddPhotoActivity() {
        Log.d(TAG, "launchAddPhotoActivity: called!");

        if (!accessInternalStorageGranted) {
            ToastHelper.toastInternalStorageAccessNotGranted(this);

        } else {
            updateRealEstateCache();
            Utils.launchActivity(this, AddPhotoActivity.class);
        }
    }

    private void createNotification () {
        Log.d(TAG, "createNotification: called!");

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notif_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        //The request code must be the same as the same we pass to .notify later
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.real_estate_logo)
                        .setContentTitle(getResources().getString(R.string.notification_title))
                        .setContentText(getResources().getString(R.string.notification_text, getRealEstateCache().getAddress()))
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                        .setAutoCancel(true);
        //SetAutoCancel(true) makes the notification dismissible when the user swipes it away

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        if (notificationManager != null) {
            notificationManager.notify(100, notificationBuilder.build());
        }
    }
}
