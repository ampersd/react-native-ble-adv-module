package com.reactnativeawesomemodule
import android.annotation.SuppressLint
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.LifecycleEventListener

// bluetooth
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log

private const val TAG = "BLEKeyAndroidActivity"

class AwesomeModuleModule(val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener {
    // init - is a part of primary constructor in Kotlin
    init {
        /*
            Our starting point.
        */
      reactContext.addLifecycleEventListener(this)
    }

    override fun getName(): String {
        return "AwesomeModule"
    }

    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    fun multiply(a: Int, b: Int, promise: Promise) {
          promise.resolve(a * b)
        }

    /* Bluetooth API */
    private var _firstStart: Boolean = true
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter


    @SuppressLint("MissingPermission")
    @ReactMethod
    fun turnBluetoothAdvertise(customName: String, turnOn: Boolean, promise: Promise) {
      if (_firstStart) {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        reactContext.registerReceiver(this.mReceiver, filter)
        bluetoothManager = reactContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        _firstStart = false
      }

      // We can't continue without proper Bluetooth support
      if (!checkBluetoothSupport(bluetoothAdapter)) {
          promise.resolve(false)
      }

      if (turnOn) {
          if (bluetoothAdapter != null) {
              if (!bluetoothAdapter.isEnabled) {
                  bluetoothAdapter.enable()
              }
              // setName - is async, when it's finished - action BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED is send to
              // BroadcastReceiver, so - there will be the place where we will call our operations
              bluetoothAdapter.setName(customName)
              promise.resolve(true)
          } else {
              log("No Support for Bluetooth Low Energy on this device");
          }
      } else {
          if (bluetoothAdapter.isEnabled) {
              stopAdvertising()
              promise.resolve(true)
          }
      }
      promise.resolve(false)
    }

    /**
     * Listens for Bluetooth adapter events to enable/disable
     * advertising and server functionality.
     * Create a BroadcastReceiver for bluetooth actions
     */
    private val mReceiver = object: BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.getAction()
            if (action == BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED) {
                log("current localdevicename: " + bluetoothManager.adapter.name)
                stopAdvertising()
                startAdvertising()
            }
            else if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        stopAdvertising()
                        log("Bluetooth off")
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> log("Turning Bluetooth off...")
                    BluetoothAdapter.STATE_ON -> {
                        startAdvertising()
                        log("Bluetooth on")
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> log("Turning Bluetooth on...")
                }
            }
        }
    }

    /**
     * Callback to receive information about the advertisement process.
     */
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            log("LE Advertise Started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.w(TAG, "LE Advertise Failed: $errorCode")
        }
    }

    /**
     * Begin advertising over Bluetooth that this device is connectable
     * and supports the Current Time Service.
     */
    @SuppressLint("MissingPermission")
    fun startAdvertising() {
        val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
            bluetoothManager.adapter.bluetoothLeAdvertiser
        log("startAdvertising: " + bluetoothManager.adapter.name)

//        val isNameChanged = BluetoothAdapter.getDefaultAdapter().setName("myDeviceName")

        bluetoothLeAdvertiser?.let {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build()

            it.startAdvertising(settings, data, advertiseCallback)
        } ?: log("Failed to create advertiser")
    }

    /**
     * Stop Bluetooth advertisements.
     */
    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
            bluetoothManager.adapter.bluetoothLeAdvertiser
        bluetoothLeAdvertiser?.let {
            it.stopAdvertising(advertiseCallback)
        } ?: log("Failed to create advertiser")
    }

    /**
     * Verify the level of Bluetooth support provided by the hardware.
     * @param bluetoothAdapter System [BluetoothAdapter].
     * @return true if Bluetooth is properly supported, false otherwise.
     */
    private fun checkBluetoothSupport(bluetoothAdapter: BluetoothAdapter?): Boolean {
        if (bluetoothAdapter == null) {
            log("Bluetooth is not supported")
            return false
        }
        if (!reactContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            log("Bluetooth LE is not supported")
            return false
        }
        return true
    }

    fun log(str: String) {
        // Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        Log.i(TAG, str)
    }

  override fun onHostResume() {
    
  }

  override fun onHostPause() {
    
  }

  override fun onHostDestroy() {
        // Don't forget to unregister the ACTION_FOUND receiver.
        reactContext.unregisterReceiver(mReceiver)
    }

    }
