package com.diegomfv.android.realestatemanager.ui.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diegomfv.android.realestatemanager.R;
import com.diegomfv.android.realestatemanager.RealEstateManagerApp;
import com.diegomfv.android.realestatemanager.data.entities.PlaceRealEstate;
import com.diegomfv.android.realestatemanager.data.entities.RealEstate;
import com.diegomfv.android.realestatemanager.utils.ToastHelper;
import com.diegomfv.android.realestatemanager.utils.Utils;
import com.diegomfv.android.realestatemanager.viewmodel.SearchEngineViewModel;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SearchEngineActivity extends AppCompatActivity {

    private static final String TAG = SearchEngineActivity.class.getSimpleName();

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @BindView(R.id.card_view_price_id)
    CardView cardViewPrice;

    @BindView(R.id.card_view_surface_area_id)
    CardView cardViewSurfaceArea;

    @BindView(R.id.card_view_number_rooms_id)
    CardView cardViewNumberOfRooms;

    @BindView(R.id.card_view_locality_id)
    CardView cardViewLocality;

    @BindView(R.id.card_view_city_id)
    CardView cardViewCity;

    @BindView(R.id.card_view_amount_photos_id)
    CardView cardViewAmountPhotos;

    @BindView(R.id.checkbox_on_sale_id)
    CheckBox checkBoxOnSale;

    @BindView(R.id.checkbox_sold_id)
    CheckBox checkBoxSold;

    @BindView(R.id.button_search_id)
    Button buttonSearch;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @BindView(R.id.linear_layout_checkboxes_type_id)
    LinearLayout typeCheckBoxesLinearLayout;

    @BindView(R.id.linear_layout_checkboxes_points_of_interest_id)
    LinearLayout pointsOfInterestCheckBoxesLinearLayout;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private List<CheckBox> listOfBuildingTypeCheckboxes;
    private List<CheckBox> listOfPointOfInterestCheckboxes;

    private TextView tvPrice;
    private RangeSeekBar seekBarPrice;

    private TextView tvSurfaceArea;
    private RangeSeekBar seekBarSurfaceArea;

    private TextView tvNumberOfRooms;
    private RangeSeekBar seekBarNumberOfRooms;

    private AutoCompleteTextView actvLocality;
    private AutoCompleteTextView actvCity;

    private TextView tvAmountOfPhotos;
    private RangeSeekBar seekBarAmountOfPhotos;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private List<RealEstate> listOfListings;

    private List<PlaceRealEstate> listOfPlaceRealEstate;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private int currency;

    private ActionBar actionBar;

    private SearchEngineViewModel searchEngineViewModel;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called!");

        this.currency = 0;

        ////////////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_search_engine);
        setTitle("Search");
        unbinder = ButterKnife.bind(this);

        /* We refresh the information in the database
         * We need the sets for displaying information in the UI
         * */
        this.getApp().getRepository().refreshSets();

        this.configureActionBar();

        this.configureLayout();

        this.createModel();

        this.subscribeToModel(searchEngineViewModel);

        // TODO: 24/08/2018 Delete!
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called!");
        unbinder.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: called!");
        getMenuInflater().inflate(R.menu.position_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: called!");

        switch (item.getItemId()) {

            case R.id.menu_change_currency_button: {

                changeCurrencyIcon(item);
                changeCurrency();

                updatePriceTextView();

            }
            break;

            case android.R.id.home: {
                Utils.launchActivity(this, MainActivity.class);

            }
            break;

        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_search_id)
    public void buttonClicked(View view) {
        Log.d(TAG, "buttonClicked: " + ((Button) view).getText().toString() + " clicked!");

        switch (view.getId()) {

            case R.id.button_search_id: {

                initSearch();

            }
            break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //SINGLETON GETTERS

    private RealEstateManagerApp getApp() {
        Log.d(TAG, "getApp: called");
        return (RealEstateManagerApp) getApplication();
    }

    private Set<String> getSetOfBuildingTypes() {
        Log.d(TAG, "getSetOfBuildingTypes: called!");
        return getApp().getRepository().getSetOfBuildingTypes();
    }

    private Set<String> getSetOfLocalities() {
        Log.d(TAG, "getSetOfLocalities: called!");
        return getApp().getRepository().getSetOfLocalities();
    }

    private Set<String> getSetOfCities() {
        Log.d(TAG, "getSetOfCities: called!");
        return getApp().getRepository().getSetOfCities();
    }

    private Set<String> getSetOfTypesOfPointsOfInterest() {
        Log.d(TAG, "getSetOfTypesOfPointsOfInterest: called!");
        return getApp().getRepository().getSetOfTypesOfPointsOfInterest();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void addCheckboxesToLayout() {
        Log.d(TAG, "addCheckboxesToLayout: called!");

        for (String buildingType : getSetOfBuildingTypes()) {
            fillWithCheckboxes(
                    typeCheckBoxesLinearLayout,
                    buildingType,
                    getListOfBuildingTypeCheckboxes());
        }

        for (String typeOfPointOfInterest : getSetOfTypesOfPointsOfInterest()) {
            fillWithCheckboxes(
                    pointsOfInterestCheckBoxesLinearLayout,
                    typeOfPointOfInterest,
                    getListOfPointOfInterestCheckboxes());
        }
    }

    private void fillWithCheckboxes(LinearLayout linearLayout, String type, List<CheckBox> listOfCheckboxes) {
        Log.d(TAG, "addPointsOfInterestCheckboxesToLayout: called!");

        CheckBox checkBox = new CheckBox(this);
        linearLayout.addView(checkBox);

        // TODO: 26/08/2018 LayoutParams does not work!

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMarginStart(8);
        layoutParams.setMargins(8, 8, 0, 0);

        checkBox.setLayoutParams(layoutParams);
        checkBox.setText(Utils.capitalize(Utils.replaceUnderscore(type)));
        checkBox.setTag(type);

        listOfCheckboxes.add(checkBox);
    }

    public List<CheckBox> getListOfBuildingTypeCheckboxes() {
        Log.d(TAG, "getListOfBuildingTypeCheckboxes: called!");
        if (listOfBuildingTypeCheckboxes == null) {
            return listOfBuildingTypeCheckboxes = new ArrayList<>();
        }
        return listOfBuildingTypeCheckboxes;
    }

    public List<CheckBox> getListOfPointOfInterestCheckboxes() {
        Log.d(TAG, "getListOfPointOfInterestCheckboxes: called!");
        if (listOfPointOfInterestCheckboxes == null) {
            return listOfPointOfInterestCheckboxes = new ArrayList<>();
        }
        return listOfPointOfInterestCheckboxes;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private List<RealEstate> getListOfListings() {
        Log.d(TAG, "getListOfListings: called!");

        if (listOfListings == null) {
            return listOfListings = new ArrayList<>();
        }
        return listOfListings;
    }

    private List<PlaceRealEstate> getListOfPlaceRealEstate() {
        Log.d(TAG, "getListOfPlaceRealEstate: called!");

        if (listOfPlaceRealEstate == null) {
            return listOfPlaceRealEstate = new ArrayList<>();
        }
        return listOfPlaceRealEstate;
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

        this.getTextViews();
        this.getSeekBars();

        this.setAllHints();
        this.setAllTexts();

    }

    private void getTextViews() {
        Log.d(TAG, "getTextViews: called!");

        this.actvLocality = cardViewLocality.findViewById(R.id.text_input_layout_id).findViewById(R.id.text_input_autocomplete_text_view_id);
        this.actvCity = cardViewCity.findViewById(R.id.text_input_layout_id).findViewById(R.id.text_input_autocomplete_text_view_id);

        this.tvPrice = cardViewPrice.findViewById(R.id.textView_title_id);
        this.tvSurfaceArea = cardViewSurfaceArea.findViewById(R.id.textView_title_id);
        this.tvNumberOfRooms = cardViewNumberOfRooms.findViewById(R.id.textView_title_id);
        this.tvAmountOfPhotos = cardViewAmountPhotos.findViewById(R.id.textView_title_id);
    }

    private void getSeekBars() {
        Log.d(TAG, "getSeekBars: called!");

        this.seekBarPrice = cardViewPrice.findViewById(R.id.range_seek_bar_id);
        this.seekBarSurfaceArea = cardViewSurfaceArea.findViewById(R.id.range_seek_bar_id);
        this.seekBarNumberOfRooms = cardViewNumberOfRooms.findViewById(R.id.range_seek_bar_id);
        this.seekBarAmountOfPhotos = cardViewAmountPhotos.findViewById(R.id.range_seek_bar_id);

    }

    private void setAllHints() {
        Log.d(TAG, "setAllHints: called!");

        // TODO: 23/08/2018 Use Resources instead of hardcoded text

        setHint(cardViewLocality, "Locality");
        setHint(cardViewCity, "City");

    }

    private void setHint(CardView cardView, String hint) {
        Log.d(TAG, "setHint: called!");

        TextInputLayout textInputLayout = cardView.findViewById(R.id.text_input_layout_id);
        textInputLayout.setHint(hint);
    }

    private void setAllTexts() {
        Log.d(TAG, "setAllTexts: called!");

        tvPrice.setText("Price (thousands, $)");
        tvSurfaceArea.setText("Surface area (sqm)");
        tvNumberOfRooms.setText("Number of Rooms");
        tvAmountOfPhotos.setText("Amount of Photos");
    }

    private void setMinMaxValuesRangeSeekBars(List<RealEstate> listOfRealEstates) {
        Log.d(TAG, "setMinMaxValuesRangeSeekBars: called!");

        int maxPrice = 0;
        int maxSurfaceArea = 0;
        int maxNumberOfRooms = 0;
        int maxAmountOfPhotos = 0;

        for (int i = 0; i < listOfRealEstates.size(); i++) {
            if (maxPrice < listOfRealEstates.get(i).getPrice()) {
                maxPrice = listOfRealEstates.get(i).getPrice();
            }
            if (maxSurfaceArea < listOfRealEstates.get(i).getSurfaceArea()) {
                maxSurfaceArea = listOfRealEstates.get(i).getSurfaceArea();
            }
            if (maxNumberOfRooms < listOfRealEstates.get(i).getNumberOfRooms()) {
                maxNumberOfRooms = listOfRealEstates.get(i).getNumberOfRooms();
            }
            if (maxAmountOfPhotos < listOfRealEstates.get(i).getListOfImagesIds().size()) {
                maxAmountOfPhotos = listOfRealEstates.get(i).getListOfImagesIds().size();
            }
        }

        Log.d(TAG, "setMinMaxValuesRangeSeekBars: maxAmountOfPhotos = " + maxAmountOfPhotos);

        seekBarPrice.setRangeValues(0, maxPrice);
        seekBarSurfaceArea.setRangeValues(50, maxSurfaceArea);
        seekBarNumberOfRooms.setRangeValues(1, maxNumberOfRooms);
        seekBarAmountOfPhotos.setRangeValues(1, maxAmountOfPhotos);

        if (maxNumberOfRooms == 1) {
            seekBarNumberOfRooms.setEnabled(false);
            tvNumberOfRooms.setText("Number of Rooms (Disabled)");
        }

        if (maxAmountOfPhotos == 1) {
            seekBarAmountOfPhotos.setEnabled(false);
            tvAmountOfPhotos.setText("Amount of Photos (Disabled)");
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void createModel() {
        Log.d(TAG, "createModel: called!");

        SearchEngineViewModel.Factory factory = new SearchEngineViewModel.Factory(getApp());
        this.searchEngineViewModel = ViewModelProviders
                .of(this, factory)
                .get(SearchEngineViewModel.class);

    }

    private void subscribeToModel(SearchEngineViewModel searchEngineViewModel) {
        Log.d(TAG, "subscribeToModel: called!");

        if (searchEngineViewModel != null) {

            this.searchEngineViewModel.getObservableListOfListings().observe(this, new Observer<List<RealEstate>>() {
                @Override
                public void onChanged(@Nullable List<RealEstate> realEstates) {
                    Log.d(TAG, "onChanged: called!");
                    listOfListings = realEstates;
                    configureAllAutocompleteTextViews();
                    setMinMaxValuesRangeSeekBars(listOfListings);
                }
            });

            this.searchEngineViewModel.getObservableAllPlacesRealEstate().observe(this, new Observer<List<PlaceRealEstate>>() {
                @Override
                public void onChanged(@Nullable List<PlaceRealEstate> placeRealEstates) {
                    Log.d(TAG, "onChanged: called!");
                    listOfPlaceRealEstate = placeRealEstates;
                    addCheckboxesToLayout();
                }
            });
        }
    }

    private void changeCurrencyIcon(MenuItem item) {
        Log.d(TAG, "changeCurrencyIcon: called!");

        if (this.currency == 0) {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_euro_symbol_white_24dp));

        } else {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_dollar_symbol_white_24dp));
        }
    }

    private void changeCurrency() {
        Log.d(TAG, "changeCurrency: called!");

        if (this.currency == 0) {
            this.currency = 1;
        } else {
            this.currency = 0;
        }
    }

    private void updatePriceTextView() {
        Log.d(TAG, "updatePriceTextView: called!");
        tvPrice.setText("Price (thousands, " + Utils.getCurrencySymbol(currency) + ")");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureAllAutocompleteTextViews() {
        Log.d(TAG, "configureAutocompleteTextViews: called!");

        this.configureAutocompleteTextView(actvLocality, getSetOfLocalities().toArray(new String[getSetOfLocalities().size()]));
        this.configureAutocompleteTextView(actvCity, getSetOfCities().toArray(new String[getSetOfCities().size()]));

    }

    @SuppressLint("CheckResult")
    private void configureAutocompleteTextView(AutoCompleteTextView autoCompleteTextView,
                                               String[] arrayOfStrings) {
        Log.d(TAG, "configureAutcompleteTextView: called!");

        ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1, //This layout has to be a textview
                arrayOfStrings
        );

        autoCompleteTextView.setAdapter(autocompleteAdapter);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String getTextFromView(TextView textView) {
        Log.d(TAG, "getTextFromView: called!");
        return Utils.capitalize(Utils.getTextViewString(textView));
    }

    private int getMaxValueFromSeekBar(RangeSeekBar rangeSeekBar) {
        Log.d(TAG, "getMaxValueFromSeekBar: called!");
        return (int) rangeSeekBar.getSelectedMaxValue();
    }

    private int getMinValueFromSeekBar(RangeSeekBar rangeSeekBar) {
        Log.d(TAG, "getMinValueFromSeekBar: called!");
        return (int) rangeSeekBar.getSelectedMinValue();
    }

    private boolean seekBarNotUsed(RangeSeekBar rangeSeekBar) {
        Log.d(TAG, "seekBarNotUsed: called!");

        if ((int) rangeSeekBar.getSelectedMinValue() == (int) rangeSeekBar.getAbsoluteMinValue()
                && (int) rangeSeekBar.getSelectedMaxValue() == (int) rangeSeekBar.getAbsoluteMaxValue()) {
            return true;
        }
        return false;
    }

    private boolean textViewNotUsed(TextView textView) {
        Log.d(TAG, "textViewNotUsed: called!");
        return getTextFromView(textView).equals("");
    }

    private boolean maxMinValuesFilterPassed(RangeSeekBar rangeSeekBar, int value) {
        Log.d(TAG, "maxMinValuesFilterPassed: called!");

        if (value > (int) rangeSeekBar.getSelectedMinValue()
                && value < (int) rangeSeekBar.getSelectedMaxValue()) {
            return true;
        }
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void initSearch() {
        Log.d(TAG, "initSearch: called!");

        List<RealEstate> listOfFilteredRealEstate = new ArrayList<>();

        for (int i = 0; i < listOfListings.size(); i++) {

            if (!allFiltersPassed(listOfListings.get(i))) {
                continue;
            }

            listOfFilteredRealEstate.add(listOfListings.get(i));
        }

        if (listOfFilteredRealEstate.size() > 0) {
            ToastHelper.toastLong(this, "Filter passed");
        }

        // TODO: 26/08/2018 Use an intent to send the information
        // TODO: 26/08/2018 Show the user the system is running the search

        // TODO: 24/08/2018 DELETE ALL OF THIS

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    //FILTERS

    private boolean allFiltersPassed(RealEstate realEstate) {
        Log.d(TAG, "allFiltersPassed: called!");

        /* Could be simplified but we leave it like this for readability
         * */

        if (!typeFilterPassed(realEstate)) {
            return false;
        }

        if (!priceFilterPassed(realEstate)) {
            return false;
        }

        if (!surfaceAreaFilterPassed(realEstate)) {
            return false;
        }

        if (!numberOfRoomsFilterPassed(realEstate)) {
            return false;
        }

        if (!cityFilterPassed(realEstate)) {
            return false;
        }

        if (!localityFilterPassed(realEstate)) {
            return false;
        }

        if (!amountOfPhotosFilterPassed(realEstate)) {
            return false;
        }

        if (!onSaleFilterPassed(realEstate)){
            return false;
        }

        if (!pointsOfInterestFilterPassed(realEstate)) {
            return false;
        }

        return true;
    }

    ///////////////

    private boolean typeFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "typeFilterPassed: called!");

        List<String> listOfCheckedTypes = new ArrayList<>();

        for (int i = 0; i < getListOfBuildingTypeCheckboxes().size(); i++) {
            if (getListOfBuildingTypeCheckboxes().get(i).isChecked()) {
                listOfCheckedTypes.add(Utils.getTextViewString(getListOfBuildingTypeCheckboxes().get(i)));
            }
        }

        /* Means the user did not check any checkbox
         * */
        if (listOfCheckedTypes.size() == 0) {
            return true;
        }

        for (int i = 0; i < getListOfBuildingTypeCheckboxes().size(); i++) {
            if (realEstate.getType().equalsIgnoreCase(listOfCheckedTypes.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean priceFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "priceFilterPassed: called!");

        /* We firstly check that the seekBar was used
         * */
        if (seekBarNotUsed(seekBarPrice)) {
            return true;
        }

        /* We take care that the price is in dollars. Besides we multiply it by 1000
         * because the range bar show prices divided by thousands
         * */
        int maxPrice = getMaxValueFromSeekBar(seekBarPrice) * 1000;
        int minPrice = getMinValueFromSeekBar(seekBarPrice) * 1000;

        if (currency == 1) {
            maxPrice = (int) Utils.convertEuroToDollar(maxPrice);
            minPrice = (int) Utils.convertEuroToDollar(minPrice);
        }

        if (realEstate.getPrice() > minPrice && realEstate.getPrice() < maxPrice) {
            return true;
        }
        return false;
    }

    private boolean surfaceAreaFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "surfaceAreaFilterPassed: called!");

        /* We firstly check that values were entered
         * */
        if (seekBarNotUsed(seekBarSurfaceArea)) {
            return true;
        }

        if (maxMinValuesFilterPassed(seekBarSurfaceArea, realEstate.getSurfaceArea())) {
            return true;
        }
        return false;
    }

    private boolean numberOfRoomsFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "numberOfRoomsFilterPassed: called!");

        /* We firstly check that values were entered
         * */
        if (seekBarNotUsed(seekBarNumberOfRooms)) {
            return true;
        }

        if (maxMinValuesFilterPassed(seekBarNumberOfRooms, realEstate.getNumberOfRooms())) {
            return true;
        }
        return false;
    }

    private boolean localityFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "cityFilterPassed: called!");

        if (textViewNotUsed(actvLocality)) {
            return true;
        }

        if (realEstate.getAddress().getLocality().equalsIgnoreCase(getTextFromView(actvLocality))) {
            return true;
        }
        return false;
    }


    private boolean cityFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "cityFilterPassed: called!");

        if (textViewNotUsed(actvCity)) {
            return true;
        }

        if (realEstate.getAddress().getCity().equalsIgnoreCase(getTextFromView(actvCity))) {
            return true;
        }
        return false;
    }

    private boolean amountOfPhotosFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "amountOfPhotosFilterPassed: called!");

        /* We firstly check that values were entered
         * */
        if (seekBarNotUsed(seekBarAmountOfPhotos)) {
            return true;
        }

        if (maxMinValuesFilterPassed(seekBarAmountOfPhotos, realEstate.getNumberOfRooms())) {
            return true;
        }
        return false;
    }

    private boolean onSaleFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "onSaleFilterPassed: called!");

        if ((!checkBoxOnSale.isChecked() && !checkBoxSold.isChecked())
                || (checkBoxOnSale.isChecked() && checkBoxSold.isChecked())) {
            return true;
        }

        if (checkBoxOnSale.isChecked() && realEstate.getDateSale() == null) {
            return true;
        }

        if (checkBoxSold.isChecked() && realEstate.getDateSale() != null) {
            return true;
        }
        return false;
    }

    private boolean pointsOfInterestFilterPassed(RealEstate realEstate) {
        Log.d(TAG, "pointsOfInterestFilterPassed: called!");

        List<String> listOfCheckedPointsOfInterest = new ArrayList<>();

        for (int i = 0; i < getListOfPointOfInterestCheckboxes().size(); i++) {
            if (getListOfPointOfInterestCheckboxes().get(i).isChecked()) {
                listOfCheckedPointsOfInterest.add(Utils.getTextViewString(getListOfPointOfInterestCheckboxes().get(i)));
            }
        }

        /* Means the user did not check any checkbox
         * */
        if (listOfCheckedPointsOfInterest.size() == 0) {
            return true;
        }

        /* We get all the nearby point of interests' types of a real estate
         * */
        List<PlaceRealEstate> listOfPointsOfInterestRelatedToTheRealEstate = new ArrayList<>();

        for (int i = 0; i < realEstate.getListOfNearbyPointsOfInterestIds().size(); i++) {
            for (int j = 0; j < getListOfPlaceRealEstate().size(); j++) {
                if (realEstate.getListOfNearbyPointsOfInterestIds().get(i).equals(getListOfPlaceRealEstate().get(j).getId())) {
                    listOfPointsOfInterestRelatedToTheRealEstate.add(getListOfPlaceRealEstate().get(j));
                }
            }
        }

        /* Now we extract all the points of interest related to the real estate
         * */
        Set<String> setOfPointsOfInterest = new HashSet<>();
        for (int i = 0; i < listOfPointsOfInterestRelatedToTheRealEstate.size(); i++) {
            for (int j = 0; j < listOfPointsOfInterestRelatedToTheRealEstate.get(i).getTypesList().size(); j++) {
                setOfPointsOfInterest.add(Utils.capitalize(Utils.replaceUnderscore(listOfPointsOfInterestRelatedToTheRealEstate.get(i).getTypesList().get(j))));
            }
        }

        /* Finally, we do the checks. The real estate will pass the filter if at least one type
         * of point of interest can be found in the list of points of interest related to the
         * real estate
         * */
        for (int i = 0; i < listOfPointsOfInterestRelatedToTheRealEstate.size(); i++) {
            for (int j = 0; j < listOfCheckedPointsOfInterest.size(); j++) {
                if (setOfPointsOfInterest.contains(listOfCheckedPointsOfInterest.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }
}