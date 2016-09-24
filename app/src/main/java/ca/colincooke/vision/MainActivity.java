package ca.colincooke.vision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity {

    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static DJIBaseProduct mProduct;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(getMainLooper());

        //initializing the dji sdk manager
        DJISDKManager.DJISDKManagerCallback callback = getDJICallback();
        DJISDKManager.getInstance().initSDKManager(this, callback);
    }

    private DJISDKManager.DJISDKManagerCallback getDJICallback() {

        return new DJISDKManager.DJISDKManagerCallback() {
            @Override
            public void onGetRegisteredResult(DJIError djiError) {
                Log.d("NO TAG", djiError == null ? "success" : djiError.getDescription());
                if (djiError == DJISDKError.REGISTRATION_SUCCESS){
                    DJISDKManager.getInstance().startConnectionToProduct();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "App Registered Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //failed
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "App Failed to Register", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

                Log.e("NO TAG", djiError.toString());
            }

            @Override
            public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {
                mProduct = newProduct;
                if (mProduct != null){
                    mProduct.setDJIBaseProductListener(productListener);
                }
            }
        };
    }

    private DJIBaseProduct.DJIBaseProductListener productListener = new DJIBaseProduct.DJIBaseProductListener() {
        @Override
        public void onComponentChange(DJIBaseProduct.DJIComponentKey djiComponentKey, DJIBaseComponent oldComponent, DJIBaseComponent newComponent) {
            if (newComponent != null){
                newComponent.setDJIComponentListener(componentListener);
            }
            notifyStatusChange();
        }

        @Override
        public void onProductConnectivityChanged(boolean b) {
            notifyStatusChange();
        }

        private DJIBaseComponent.DJIComponentListener componentListener = new DJIBaseComponent.DJIComponentListener() {
            @Override
            public void onComponentConnectivityChanged(boolean b) {
                notifyStatusChange();
            }
        };

        private void notifyStatusChange() {
            handler.removeCallbacks(updateRunnable);
            handler.postDelayed(updateRunnable, 500);
        }

        private Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
                sendBroadcast(intent);
            }
        };
    };




}
