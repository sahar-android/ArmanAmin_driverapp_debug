package com.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.kubinga.driver.MyProfileActivity;
import com.kubinga.driver.R;
import com.kubinga.driver.SelectCountryActivity;
import com.kubinga.driver.VerifyInfoActivity;
import com.dialogs.OpenListView;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;

import com.squareup.picasso.Picasso;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    MyProfileActivity myProfileAct;
    View view;

    GeneralFunctions generalFunc;

    JSONObject userProfileJson = null;

    MaterialEditText fNameBox;
    MaterialEditText lNameBox;
    MaterialEditText emailBox;
    MaterialEditText countryBox;
    MaterialEditText mobileBox;
    MaterialEditText langBox;
    MaterialEditText currencyBox;
    MaterialEditText profileDescriptionEditBox;


    String selected_language_code = "";


    ArrayList<HashMap<String, String>> languageDataList = new ArrayList<>();
    ArrayList<HashMap<String, String>> currencyDataList = new ArrayList<>();


    String selected_currency = "";
    String default_selected_currency = "";



    MButton btn_type2;
    int submitBtnId;

    String required_str = "";
    String error_email_str = "";

    String vCountryCode = "";
    String vPhoneCode = "";
    String mobileBoxVal="";
    boolean isCountrySelected = false;

    FrameLayout langSelectArea, currencySelectArea;
    ImageView countryimage;
    String vSImage = "";
    InternetConnection intCheck;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        myProfileAct = (MyProfileActivity) getActivity();
        intCheck = new InternetConnection(getActContext());

        generalFunc = myProfileAct.generalFunc;
        countryimage = (ImageView) view.findViewById(R.id.countryimage);
        fNameBox = (MaterialEditText) view.findViewById(R.id.fNameBox);
        lNameBox = (MaterialEditText) view.findViewById(R.id.lNameBox);
        emailBox = (MaterialEditText) view.findViewById(R.id.emailBox);
        profileDescriptionEditBox = (MaterialEditText) view.findViewById(R.id.profileDescriptionEditBox);
        countryBox = (MaterialEditText) view.findViewById(R.id.countryBox);
        mobileBox = (MaterialEditText) view.findViewById(R.id.mobileBox);
        langBox = (MaterialEditText) view.findViewById(R.id.langBox);
        currencyBox = (MaterialEditText) view.findViewById(R.id.currencyBox);
        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        vSImage = generalFunc.retrieveValue(Utils.DefaultCountryImage);
        Picasso.get().load(vSImage).into(countryimage);
        int paddingValStart = (int) getResources().getDimension(R.dimen._35sdp);
        int paddingValEnd = (int) getResources().getDimension(R.dimen._12sdp);
        if (generalFunc.isRTLmode()){
            countryBox.setPaddings(paddingValEnd, 0, paddingValStart, 0);
        }else {
            countryBox.setPaddings(paddingValStart, 0, paddingValEnd, 0);
        }
        currencySelectArea = (FrameLayout) view.findViewById(R.id.currencySelectArea);
        langSelectArea = (FrameLayout) view.findViewById(R.id.langSelectArea);

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());
        mobileBox.setInputType(InputType.TYPE_CLASS_NUMBER);

        emailBox.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);

        mobileBox.setImeOptions(EditorInfo.IME_ACTION_DONE);


        userProfileJson = myProfileAct.userProfileJsonObj;

        setLabels();

        removeInput();


        setData();
        buildLanguageList();

        myProfileAct.changePageTitle(generalFunc.retrieveLangLBl("", "LBL_EDIT_PROFILE_TXT"));

        if (myProfileAct.isEmail) {
            emailBox.requestFocus();
        }

        if (myProfileAct.isMobile) {
            mobileBox.requestFocus();
        }
        return view;
    }

    public void setLabels() {
        fNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_FIRST_NAME_HEADER_TXT"));
        lNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LAST_NAME_HEADER_TXT"));
        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT"));
        countryBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_COUNTRY_TXT"));
        mobileBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        langBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LANGUAGE_TXT"));
        currencyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CURRENCY_TXT"));
        profileDescriptionEditBox.setBothText(generalFunc.retrieveLangLBl("Service Description", "LBL_SERVICE_DESCRIPTION"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_UPDATE"));

//        fNameBox.getLabelFocusAnimator().start();
//        lNameBox.getLabelFocusAnimator().start();
//        emailBox.getLabelFocusAnimator().start();
//        countryBox.getLabelFocusAnimator().start();
//        mobileBox.getLabelFocusAnimator().start();
//        langBox.getLabelFocusAnimator().start();
//        currencyBox.getLabelFocusAnimator().start();
//        profileDescriptionEditBox.getLabelFocusAnimator().start();
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");
        String APP_TYPE = generalFunc.getJsonValueStr("APP_TYPE", userProfileJson);

        if ((APP_TYPE.equalsIgnoreCase("UberX") || (APP_TYPE.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && myProfileAct.isUfxServicesEnabled)) && !generalFunc.isDeliverOnlyEnabled()) {
            profileDescriptionEditBox.setVisibility(View.VISIBLE);
        }
    }

    public void removeInput() {
        Utils.removeInput(countryBox);
        Utils.removeInput(langBox);
        Utils.removeInput(currencyBox);

        if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {
            view.findViewById(R.id.countrydropimage).setVisibility(View.VISIBLE);
            countryBox.setOnClickListener(new setOnClickList());
            countryBox.setOnTouchListener(new setOnTouchList());
        }

        langBox.setOnTouchListener(new setOnTouchList());
        currencyBox.setOnTouchListener(new setOnTouchList());

        langBox.setOnClickListener(new setOnClickList());
        currencyBox.setOnClickListener(new setOnClickList());
    }

    public void setData() {
        fNameBox.setText(generalFunc.getJsonValueStr("vName", userProfileJson));
        lNameBox.setText(generalFunc.getJsonValueStr("vLastName", userProfileJson));

        if (generalFunc.retrieveValue("ENABLE_EDIT_DRIVER_PROFILE").equalsIgnoreCase("No")) {
            Utils.removeInput(fNameBox) ;
            Utils.removeInput(lNameBox) ;
        }

        emailBox.setText(generalFunc.getJsonValueStr("vEmail", userProfileJson));
        countryBox.setText(generalFunc.convertNumberWithRTL("+" +generalFunc.getJsonValueStr("vCode", userProfileJson)));

        mobileBoxVal=generalFunc.getJsonValueStr("vPhone", userProfileJson);
        mobileBox.setText(generalFunc.convertNumberWithRTL(mobileBoxVal));
        currencyBox.setText(generalFunc.getJsonValueStr("vCurrencyDriver", userProfileJson));
        profileDescriptionEditBox.setText(generalFunc.getJsonValueStr("tProfileDescription", userProfileJson));

        if (!generalFunc.getJsonValue("vCode", userProfileJson).equals("")) {
            isCountrySelected = true;
            vPhoneCode = generalFunc.getJsonValueStr("vCode", userProfileJson);
            vCountryCode = generalFunc.getJsonValueStr("vCountry", userProfileJson);
        }


        if (generalFunc.getJsonValueStr("vSCountryImage", userProfileJson) != null && !generalFunc.getJsonValueStr("vSCountryImage", userProfileJson).equalsIgnoreCase("")) {
            vSImage = generalFunc.getJsonValueStr("vSCountryImage", userProfileJson);
            Picasso.get().load(vSImage).into(countryimage);

        }
        selected_currency = generalFunc.getJsonValueStr("vCurrencyDriver", userProfileJson);
        default_selected_currency = selected_currency;
    }

    public void buildLanguageList() {

        JSONArray languageList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.LANGUAGE_LIST_KEY));
        languageDataList.clear();


        HashMap<String, String> data = new HashMap<>();
        data.put(Utils.LANGUAGE_LIST_KEY, "");
        data.put(Utils.LANGUAGE_CODE_KEY, "");
        data = generalFunc.retrieveValue(data);

        for (int i = 0; i < languageList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(languageList_arr, i);

            HashMap<String, String> mapData = new HashMap<>();
            mapData.put("vTitle", generalFunc.getJsonValueStr("vTitle", obj_temp));
            mapData.put("vCode", generalFunc.getJsonValueStr("vCode", obj_temp));

            if (Utils.getText(langBox).equalsIgnoreCase(generalFunc.getJsonValueStr("vTitle", obj_temp))) {
                selLanguagePosition = i;
            }

            if ((data.get(Utils.LANGUAGE_CODE_KEY)).equalsIgnoreCase(generalFunc.getJsonValueStr("vCode", obj_temp))) {
                selLanguagePosition = i;

                langBox.setText(generalFunc.getJsonValueStr("vTitle", obj_temp));
            }

            languageDataList.add(mapData);

            if ((generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY)).equals(generalFunc.getJsonValue("vCode", obj_temp))) {
                selected_language_code = generalFunc.getJsonValueStr("vCode", obj_temp);

            }
        }


        if (languageDataList.size() < 2) {
            langSelectArea.setVisibility(View.GONE);
        }

        buildCurrencyList();

    }

    public void buildCurrencyList() {


        JSONArray currencyList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.CURRENCY_LIST_KEY));
        currencyDataList.clear();
        for (int i = 0; i < currencyList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(currencyList_arr, i);

            HashMap<String, String> mapData = new HashMap<>();
            mapData.put("vName", generalFunc.getJsonValueStr("vName", obj_temp));
            mapData.put("vSymbol", generalFunc.getJsonValueStr("vSymbol", obj_temp));

            if (Utils.getText(currencyBox).equalsIgnoreCase(generalFunc.getJsonValueStr("vName", obj_temp))) {
                selCurrancyPosition = i;
            }

            currencyDataList.add(mapData);

        }

        if (currencyDataList.size() < 2) {
            currencySelectArea.setVisibility(View.GONE);

            if (languageDataList.size() < 2) {
                langSelectArea.setVisibility(View.GONE);
            }
        }
    }

    public void showCurrencyList() {
        OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_SELECT_CURRENCY"), currencyDataList, OpenListView.OpenDirection.CENTER, true, position -> {


            selCurrancyPosition = position;
            selected_currency = currencyDataList.get(position).get("vName");
            currencyBox.setText(currencyDataList.get(position).get("vName"));
        }).show(selCurrancyPosition, "vName");
    }

    int selCurrancyPosition = -1;
    int selLanguagePosition = -1;

    public void showLanguageList() {

        OpenListView.getInstance(getActContext(), getSelectLangText(), languageDataList, OpenListView.OpenDirection.CENTER, true, position -> {


            selLanguagePosition = position;
            selected_language_code = languageDataList.get(position).get("vCode");
            generalFunc.storeData(Utils.DEFAULT_LANGUAGE_VALUE, languageDataList.get(position).get("vTitle"));
            langBox.setText(languageDataList.get(position).get("vTitle"));
        }).show(selLanguagePosition, "vTitle");
    }

    public String getSelectLangText() {
        return ("" + generalFunc.retrieveLangLBl("Select", "LBL_SELECT_LANGUAGE_HINT_TXT"));
    }

    public void checkValues() {


        boolean fNameEntered = Utils.checkText(fNameBox) ? true : Utils.setErrorFields(fNameBox, required_str);
        boolean lNameEntered = Utils.checkText(lNameBox) ? true : Utils.setErrorFields(lNameBox, required_str);
        boolean emailEntered = Utils.checkText(emailBox) ?
                (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_email_str))
                : Utils.setErrorFields(emailBox, required_str);
        boolean mobileEntered = Utils.checkText(mobileBox) ? true : Utils.setErrorFields(mobileBox, required_str);
        boolean countryEntered = isCountrySelected ? true : false;
        boolean currencyEntered = !selected_currency.equals("") ? true : Utils.setErrorFields(currencyBox, required_str);


        if (mobileEntered) {
            mobileEntered = mobileBox.length() >= 3 ? true : Utils.setErrorFields(mobileBox, generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
        }
        if (fNameEntered == false || lNameEntered == false || emailEntered == false || mobileEntered == false
                || countryEntered == false || currencyEntered == false) {
            return;
        }


        String currentMobileNum = generalFunc.getJsonValueStr("vPhone", userProfileJson);
        String currentPhoneCode = generalFunc.getJsonValueStr("vPhoneCode", userProfileJson);

        if (!currentPhoneCode.equals(vPhoneCode) || !currentMobileNum.equals(mobileBoxVal)) {
            if (generalFunc.retrieveValue(Utils.MOBILE_VERIFICATION_ENABLE_KEY).equals("Yes")) {
                notifyVerifyMobile();
                return;
            }
        }



        updateProfile();

    }

    public void notifyVerifyMobile() {
        Bundle bn = new Bundle();
        bn.putString("MOBILE", vPhoneCode + mobileBoxVal);
        bn.putString("msg", "DO_PHONE_VERIFY");
        generalFunc.verifyMobile(bn, myProfileAct.getEditProfileFrag(), VerifyInfoActivity.class);

    }


    public void updateProfile() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateUserProfileDetail");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("vName", Utils.getText(fNameBox));
        parameters.put("vLastName", Utils.getText(lNameBox));
        parameters.put("vPhone", Utils.getText(mobileBox));
        parameters.put("tProfileDescription", Utils.getText(profileDescriptionEditBox));
        parameters.put("vPhoneCode", vPhoneCode);
        parameters.put("vCountry", vCountryCode);
        parameters.put("vEmail", Utils.getText(emailBox));
        parameters.put("CurrencyCode", selected_currency);
        parameters.put("LanguageCode", selected_language_code);
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {

                    String currentLangCode = generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY);
                    String vCurrencyPassenger = generalFunc.getJsonValueStr("vCurrencyDriver", userProfileJson);

                    String messgeJson = generalFunc.getJsonValue(Utils.message_str, responseString);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, messgeJson);
                    responseString = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);


                    new SetUserData(responseString, generalFunc, getActContext(), false);

                    if (!currentLangCode.equals(selected_language_code) || !selected_currency.equals(vCurrencyPassenger)) {

                        GenerateAlertBox alertBox = generalFunc.notifyRestartApp();
                        alertBox.setCancelable(false);
                        alertBox.setBtnClickList(btn_id -> {

                            if (btn_id == 1) {
                                //  generalFunc.restartApp();
                                generalFunc.storeData(Utils.LANGUAGE_CODE_KEY, selected_language_code);
                                generalFunc.storeData(Utils.DEFAULT_CURRENCY_VALUE, selected_currency);
                                changeLanguagedata(selected_language_code);
                            }
                        });
                    } else {
                        myProfileAct.changeUserProfileJson(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                    }

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void changeLanguagedata(String langcode) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "changelanguagelabel");
        parameters.put("vLang", langcode);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {

                    generalFunc.storeData(Utils.languageLabelsKey, generalFunc.getJsonValue(Utils.message_str, responseString));
                    generalFunc.storeData(Utils.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValue("eType", responseString));
                    generalFunc.storeData(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vGMapLangCode", responseString));
                    GeneralFunctions.clearAndResetLanguageLabelsData(MyApp.getInstance().getApplicationContext());
                    new Handler().postDelayed(() -> generalFunc.restartApp(), 100);

                }
            }
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return myProfileAct.getActContext();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == myProfileAct.RESULT_OK && data != null) {
            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            isCountrySelected = true;
            vSImage = data.getStringExtra("vSImage");
            Picasso.get().load(vSImage).into(countryimage);
            countryBox.setText(generalFunc.convertNumberWithRTL("+" + vPhoneCode));
        } else if (requestCode == Utils.VERIFY_MOBILE_REQ_CODE && resultCode == myProfileAct.RESULT_OK) {


            updateProfile();

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }

    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActivity());
            int i = view.getId();
            if (i == R.id.langBox) {
                showLanguageList();

            } else if (i == R.id.currencyBox) {
                showCurrencyList();

            } else if (i == submitBtnId) {

                checkValues();
            } else if (i == R.id.countryBox) {
                new StartActProcess(getActContext()).startActForResult(myProfileAct.getEditProfileFrag(),
                        SelectCountryActivity.class, Utils.SELECT_COUNTRY_REQ_CODE);
            }
        }
    }
}
