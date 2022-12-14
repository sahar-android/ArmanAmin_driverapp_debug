package com.kubinga.driver;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONObject;

import java.util.HashMap;

public class PrefranceActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    MTextView titleTxt;
    ImageView backImgView;

    public GeneralFunctions generalFunc;
    CheckBox checkboxFemale, checkboxHandicap;

    String ishandicap = "No";
    String isfemale = "No";
    public MButton update_btn;
    String UserprofileJson = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefrance);


        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        UserprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());
        checkboxHandicap = (CheckBox) findViewById(R.id.checkboxHandicap);
        checkboxFemale = (CheckBox) findViewById(R.id.checkboxFemale);
        update_btn = ((MaterialRippleLayout) findViewById(R.id.update_btn)).getChildView();
        update_btn.setId(Utils.generateViewId());


        update_btn.setOnClickListener(new setOnClickList());
        checkboxFemale.setOnCheckedChangeListener(this);
        checkboxHandicap.setOnCheckedChangeListener(this);
        setLabel();
        if (generalFunc.getJsonValue("eFemaleOnlyReqAccept", UserprofileJson).equalsIgnoreCase("Yes")) {
            checkboxFemale.setChecked(true);
        }


    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(PrefranceActivity.this);
            if (i == update_btn.getId()) {
                updateDriverPrefrance();

            } else if (i == backImgView.getId()) {
                PrefranceActivity.super.onBackPressed();
            }


        }
    }

    public void updateDriverPrefrance() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateuserPref");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eFemaleOnly", isfemale);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail == true) {

                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
                    try {
                        GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());

                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_PREF_SUCCESS_UPDATE"));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
                        generateAlert.setBtnClickList(btn_id -> {
                            if (btn_id == 1) {
                                finish();
                            }
                        });
                        generateAlert.showAlertBox();
                    } catch (Exception e) {

                    }

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void setLabel() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Prefrance", "LBL_PREFRANCE_TXT"));
        checkboxHandicap.setText(generalFunc.retrieveLangLBl("Filter handicap accessibility drivers only", "LBL_MUST_HAVE_HANDICAP_ASS_CAR"));
        checkboxFemale.setText(generalFunc.retrieveLangLBl("Accept Female Only trip request", "LBL_ACCEPT_FEMALE_REQ_ONLY"));
        update_btn.setText(generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"));
    }

    public Context getActContext() {
        return PrefranceActivity.this;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Utils.hideKeyboard(PrefranceActivity.this);
        if (buttonView == checkboxFemale) {
            if (checkboxFemale.isChecked()) {
                isfemale = "Yes";
            } else {
                isfemale = "No";
            }
            generalFunc.storeData(Utils.PREF_FEMALE, isfemale);

        } else if (buttonView == checkboxHandicap) {
            if (checkboxHandicap.isChecked()) {
                ishandicap = "Yes";
            } else {
                ishandicap = "No";
            }

        }

    }
}
