package com.rsquared.blescanner;

import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ListActivity {
	private LeDeviceListAdapter bLeDeviceListAdapter;
	BluetoothAdapter bluetoothAdapter; 
	private boolean bScanning;
	private Handler bHandler;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,"Device doesnt support BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
      
       /*
        * ccode to get bluetooth adapter instance - bluetoothAdapter.
        */
        bLeDeviceListAdapter=new LeDeviceListAdapter();
        bHandler=new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        /*
         * If the device doesn't have bluetooth stop the app.
         */
        if (bluetoothAdapter == null) {
            Toast.makeText(this,"bluetooth harware not found :(", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        /*
         * code to enable bluetooth if it is not.
         */
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
       
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	  if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
              Toast.makeText(this,"Device doesnt support BLE", Toast.LENGTH_SHORT).show();
              finish();
          }
        getMenuInflater().inflate(R.menu.main, menu);
        
        if (!bScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_progressbar);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	 switch (item.getItemId()) {
         case R.id.menu_scan:
        	 if(bLeDeviceListAdapter.getCount()>0)
        		 bLeDeviceListAdapter.clear();
             scanLeDevice(true);
             break;
         case R.id.menu_stop:
             scanLeDevice(false);
             break;
     }
        return super.onOptionsItemSelected(item);
    }
    
    
    /*
     * The BLE Scanning callback
     */
    
    private BluetoothAdapter.LeScanCallback bLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bLeDeviceListAdapter.addDevice(device);
                    Toast.makeText(getApplicationContext(), device.getName(),Toast.LENGTH_SHORT).show();
                    bLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }

        };
           
    
    /*
     * function for stopping or starting scanning
     */
    
    
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            bHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bScanning = false;
                    bluetoothAdapter.stopLeScan(bLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            bScanning = true;
            bluetoothAdapter.startLeScan(bLeScanCallback);
        } else {
            bScanning = false;
            bluetoothAdapter.stopLeScan(bLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    
   
        
        
        
        
        
   /*
    * adapter for the list view.     
    */
        
             
        
        private class LeDeviceListAdapter extends BaseAdapter {
            private ArrayList<BluetoothDevice> bLeDevices;
            private LayoutInflater bInflator;

            public LeDeviceListAdapter() {
                super();
                bLeDevices = new ArrayList<BluetoothDevice>();
                bInflator = MainActivity.this.getLayoutInflater();
            }

            public void addDevice(BluetoothDevice device) {
            	//If the list doesn't have the device add.
                if(!bLeDevices.contains(device)) {
                    bLeDevices.add(device);
                }
            }

           
            public void clear() {
                bLeDevices.clear();
            }

            @Override
            public int getCount() {
                return bLeDevices.size();
            }

            @Override
            public Object getItem(int i) {
                return bLeDevices.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                Row row;
                // General ListView optimization code.
                if (view == null) {
                    view = bInflator.inflate(R.layout.list_item, null);
                    row = new Row();
                    row.bleAddress = (TextView) view.findViewById(R.id.ble_address);
                    row.bleName = (TextView) view.findViewById(R.id.ble_name);
                    view.setTag(row);
                } else {
                    row = (Row) view.getTag();
                }

                BluetoothDevice device = bLeDevices.get(i);
                final String deviceName = device.getName();
                if (deviceName != null && deviceName.length() > 0)
                    row.bleName.setText(deviceName);
                else
                    row.bleName.setText("no name");
                row.bleAddress.setText(device.getAddress());

                return view;
            }
        }
        static class Row {
            TextView bleName;
            TextView bleAddress;
        }
}
