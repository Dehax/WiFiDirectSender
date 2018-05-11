package org.donntu.itt.dehax.wifidirectsender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiDirectChannel;
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;

    private ArrayList<WifiP2pDevice> mDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });

        final Button discoverButton = findViewById(R.id.discoverButton);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiP2pManager.discoverPeers(mWifiDirectChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Discovered peers!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        String errorMessage = "WiFi Direct Failed: ";

                        switch (reason) {
                            case WifiP2pManager.BUSY:
                                errorMessage += "Framework busy.";
                                break;
                            case WifiP2pManager.ERROR:
                                errorMessage += "Internal error.";
                                break;
                            case WifiP2pManager.P2P_UNSUPPORTED:
                                errorMessage += "Unsupported.";
                                break;
                            default:
                                errorMessage += "Unknown error.";
                                break;
                        }
                        Log.e("DEHAX", errorMessage);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        final ListView devicesListView = findViewById(R.id.devicesListView);
        final ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDeviceList);
        devicesListView.setAdapter(arrayAdapter);

        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice device = mDeviceList.get((int) id);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mWifiP2pManager.connect(mWifiDirectChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        String errorMessage = "WiFi Direct Failed: ";

                        switch (reason) {
                            case WifiP2pManager.BUSY:
                                errorMessage += "Framework busy.";
                                break;
                            case WifiP2pManager.ERROR:
                                errorMessage += "Internal error.";
                                break;
                            case WifiP2pManager.P2P_UNSUPPORTED:
                                errorMessage += "Unsupported.";
                                break;
                            default:
                                errorMessage += "Unknown error.";
                                break;
                        }
                        Log.e("DEHAX", errorMessage);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        initializeWifiDirect();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                assert action != null;
                switch (action) {
                    case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, WifiP2pManager.WIFI_P2P_STATE_DISABLED);

                        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                            discoverButton.setEnabled(true);
                        } else {
                            discoverButton.setEnabled(false);
                        }
                        break;
                    case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                        mWifiP2pManager.requestPeers(mWifiDirectChannel, new WifiP2pManager.PeerListListener() {
                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList peers) {
                                Toast.makeText(MainActivity.this, "Peers available", Toast.LENGTH_SHORT).show();
                                mDeviceList.clear();
                                mDeviceList.addAll(peers.getDeviceList());
                                arrayAdapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                        boolean connected = networkInfo.isConnected();

                        if (connected) {
                            Toast.makeText(MainActivity.this, "Wi-Fi Direct Connected", Toast.LENGTH_LONG).show();
                            mWifiP2pManager.requestConnectionInfo(mWifiDirectChannel, new WifiP2pManager.ConnectionInfoListener() {
                                @Override
                                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                    if (info.groupFormed) {
                                        if (info.isGroupOwner) {
                                            Toast.makeText(MainActivity.this, "You are group owner (SERVER)", Toast.LENGTH_LONG).show();
                                            new ServerTask(MainActivity.this).execute();
                                        } else {
                                            Toast.makeText(MainActivity.this, "You are client", Toast.LENGTH_LONG).show();
                                            new ClientTask(MainActivity.this).execute(info.groupOwnerAddress);
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "NO GROUP FORMED", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Log.d("DEHAX", "Wi-Fi Direct Disconnected");
                            Toast.makeText(MainActivity.this, "Wi-Fi Direct Disconnected", Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void initializeWifiDirect() {
        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiDirectChannel = mWifiP2pManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Toast.makeText(MainActivity.this, "Channel Disconnected!", Toast.LENGTH_LONG).show();
//                initializeWifiDirect();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
