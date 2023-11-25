package com.yauntyour.showmap;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private void updataLocationINFO() {
        TextView statinfo = (TextView) findViewById(R.id.statinfo);
        TextView geoinfo = (TextView) findViewById(R.id.geoinfo);
        //获取最近一次位置
        Location l = getLastLocation();
        double Latitude = l.getLatitude();
        double Longitude = l.getLongitude();
        //Geocoder通过经纬度获取具体信息
        Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> locationList = gc.getFromLocation(Latitude, Longitude, 1);
            Address address = locationList.get(0);

            String geobuf = "最近一次位置信息: \n"
                    + address.getCountryName()//国家
                    + "(" + address.getCountryCode() + ") \n"//国家编码
                    + address.getAdminArea() + "," + address.getLocality() + ",\n"//省市
                    + address.getSubLocality() + "," + address.getFeatureName() + "\n"//区街道
                    + "地理信息：\n"
                    + "纬度: " + Latitude + "°\n"
                    + "经度: " + Longitude + "°\n"
                    + "精确度： " + l.getAccuracy() + "m\n"
                    + "海拔： " + l.getAltitude() + "m\n";
            geoinfo.setText(geobuf);
            statinfo.setText("服务状态: "+l.getProvider());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upodataProviderStatus(String provider, int status) {
        TextView statinfo = (TextView) findViewById(R.id.statinfo);
        statinfo.setText("服务状态: " + provider + ": " + status);
    }

    private void upodataProviderStatus(String provider) {
        TextView statinfo = (TextView) findViewById(R.id.statinfo);
        statinfo.setText("服务状态: " + provider);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isLocationProviderEnabled() == true) {
            getLocationPermissions();
        } else {
            openLocationServer();
            getLocationPermissions();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        Switch swt = (Switch) findViewById(R.id.apswt);


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updataLocationINFO();
            }
        });

        swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LocationManager locationManager = (LocationManager) getApplicationContext()
                        .getSystemService(LOCATION_SERVICE);
                List<String> providers = locationManager.getProviders(true);
                if (isChecked) {
                    for (String provider : providers) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        if (locationManager != null) {
                            locationManager.requestLocationUpdates(provider, 60000, 10, locationListener);
                        }
                    }
                } else {
                    if (locationManager != null) {
                        locationManager.removeUpdates(locationListener);
                    }
                }
            }
        });
    }


    /**
     * 判断是否开启了GPS或网络定位开关
     */
    public boolean isLocationProviderEnabled() {
        boolean result = false;

        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            result = true;
        }
        return result;
    }

    /**
     * 跳转到设置界面，引导用户开启定位服务
     */
    private void openLocationServer() {
        Intent i = new Intent();
        i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    private void getLocationPermissions() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {
            upodataProviderStatus(provider, status);
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
            upodataProviderStatus(provider);
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
            upodataProviderStatus(provider);
        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location loc) {
            updataLocationINFO();
        }
    };

    private Location getLastLocation() {
        Location location = null;

        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            return null;
        }

        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                getLocationPermissions();
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                // Found best last known location: %s", l);
                location = l;
            }
        }
        return location;
    }
}