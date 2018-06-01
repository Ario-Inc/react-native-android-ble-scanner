package com.arioliving.ble.scanner;

import com.facebook.react.uimanager.*;
import com.facebook.react.bridge.*;
import com.facebook.systrace.Systrace;
import com.facebook.systrace.SystraceMessage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

public class AndroidBLEScannerModule extends ReactContextBaseJavaModule {

    private ReactApplicationContext mReactContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private int mCompanyId = 0x00;
    private boolean mScannerStarted = false;

    //Constructor
    public AndroidBLEScannerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
        
        BluetoothManager bluetoothManager = (BluetoothManager) reactContext.getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        
    }  

    @Override
    public String getName() {
        return "AndroidBLEScannerModule";
    }


    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            
            byte[] rawAdv = result.getScanRecord().getManufacturerSpecificData(mCompanyId);
            
            if (rawAdv != null) {
                WritableNativeArray output = new WritableNativeArray();
                for (int i = 0; i < rawAdv.length; i++) {
                    output.pushInt(rawAdv[i] & 0xFF);
                }
                mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("scanResult",
                        output);
            }
            
        }
    };

    @ReactMethod
    public void setCompanyId(int companyId) {
        mCompanyId = companyId;
    }

    @ReactMethod
    public void startScanner() {
        if (!mScannerStarted) {
            mBluetoothScanner.startScan(leScanCallback);
            mScannerStarted = true;
        }
    }

    @ReactMethod
    public void stopScanner() {
        if (mScannerStarted) {
            mBluetoothScanner.stopScan(leScanCallback);
            mScannerStarted = false;
        }
    }
}
