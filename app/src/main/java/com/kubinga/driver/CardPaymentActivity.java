package com.kubinga.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.fragments.AddCardFragment;
import com.fragments.ViewCardFragment;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONObject;

import java.util.HashMap;


public class CardPaymentActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    public JSONObject userProfileJsonObj;
    MTextView titleTxt;
    ImageView backImgView;
    ViewCardFragment viewCardFrag;
    AddCardFragment addCardFrag;
    public static final int REQ_ADD_CARD_CODE = 101;

    String APP_PAYMENT_METHOD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_payment);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        getUserProfileJson(generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        setLabels();

        backImgView.setOnClickListener(new setOnClickList());

        openViewCardFrag();


    }

    private void getUserProfileJson(JSONObject object) {
        userProfileJsonObj = object;

        APP_PAYMENT_METHOD = generalFunc.getJsonValueStr("APP_PAYMENT_METHOD", userProfileJsonObj);
    }


    public void setLabels() {
        changePageTitle(generalFunc.retrieveLangLBl("", "LBL_CARD_PAYMENT_DETAILS"));
    }

    public void changePageTitle(String title) {
        titleTxt.setText(title);
    }

    public void changeUserProfileJson(String userProfileJson) {
        getUserProfileJson(generalFunc.getJsonObject(userProfileJson));

        Bundle bn = new Bundle();
        new StartActProcess(getActContext()).setOkResult(bn);

        openViewCardFrag();

        generalFunc.showMessage(getCurrView(), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
    }

    public View getCurrView() {
        return generalFunc.getCurrentView(CardPaymentActivity.this);
    }

    public void openViewCardFrag() {

        if (viewCardFrag != null) {
            viewCardFrag = null;
            addCardFrag = null;
            Utils.runGC();
        }
        viewCardFrag = new ViewCardFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, viewCardFrag).commit();
    }

    public void openAddCardFrag(String mode) {

        if (addCardFrag != null) {
            addCardFrag = null;
            viewCardFrag = null;
            Utils.runGC();
        }
        if (APP_PAYMENT_METHOD.equalsIgnoreCase("Stripe")) {
            Bundle bundle = new Bundle();
            bundle.putString("PAGE_MODE", mode);
            bundle.putString("carno", generalFunc.getJsonValueStr("vCreditCard", userProfileJsonObj));
            addCardFrag = new AddCardFragment();
            addCardFrag.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, addCardFrag).commit();
        }
    }

    public Context getActContext() {
        return CardPaymentActivity.this;
    }

    @Override
    public void onBackPressed() {
        backImgView.performClick();
        return;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_ADD_CARD_CODE) {
                CreateCustomer();

            } else if (requestCode == Utils.REQ_VERIFY_CARD_PIN_CODE) {

                if (addCardFrag != null) {
                    addCardFrag.setdata(requestCode, resultCode, data);
                }
            }
        }

    }

    public void CreateCustomer() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateCustomer");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);
                String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);

                if (isDataAvail == true) {
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, msg_str);
                    changeUserProfileJson(msg_str);

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", msg_str));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    private void UpdateCustomerToken() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateCustomerToken");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vPaymayaToken", "");
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail == true) {
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
                    changeUserProfileJson(generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
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

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(CardPaymentActivity.this);
            if (i == R.id.backImgView) {

                if (addCardFrag != null && addCardFrag.isInProcessMode) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("You cannot go back while your transaction is being processed. Please wait for transaction being completed.", "LBL_TRANSACTION_IN_PROCESS_TXT"));
                } else if (addCardFrag == null) {
                    CardPaymentActivity.super.onBackPressed();
                } else {
                    openViewCardFrag();
                }
            }
        }
    }

}
