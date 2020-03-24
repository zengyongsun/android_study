package com.zy.android_study.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;


/**
 * @author : Zeyo
 * e-mail : zengyongsun@163.com
 * date   : 2020/3/20 14:10
 * desc   :
 * version: 1.0
 */
public class GpsManager {

    private static final String TAG = "GpsManager";

    private LocationManager mLocationManager = null;

    /**
     * 开启定位成功
     */
    public static final int START_LOCATION_OK = 0;

    /**
     * GPS 没有打开
     */
    public static final int GPS_NO_OPEN = 1;

    public GpsManager(Context context) {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    /**
     * 在activity里面需要销毁，不然会后台一直运行
     */
    public void stopLocation() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(gpsLocationListener);
            mLocationManager.removeUpdates(netWorkLocationListener);
        }
    }

    @SuppressLint("MissingPermission")
    public int startLocation() {
        Criteria criteria = new Criteria();
        //高精度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //无海拔要求   criteria.setBearingRequired(false);//无方位要求
        criteria.setAltitudeRequired(false);
        //允许产生资费   criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗
        criteria.setCostAllowed(true);
        boolean gpsOpen = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsOpen) {
            /*
             * 绑定监听
             * 参数1，设备：有GPS_PROVIDER 和 NETWORK_PROVIDER 两种，前者是GPS,后者是GPRS以及WIFI定位
             * 参数2，位置信息更新周期.单位是毫秒
             * 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
             * 参数4，监听
             * 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
             */
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000,
                    0, netWorkLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                    0, gpsLocationListener);
             Log.e(TAG, "location:requestLocationUpdates");
        } else {
            return GPS_NO_OPEN;
        }
        return START_LOCATION_OK;
    }

    private Location mLocation;

    private LocationListener netWorkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
             Log.e(TAG, "location:netWorkLocationListener" + location.toString());
            if (isBetterLocation(location, mLocation)) {
                mLocation = location;
                if (myLocationListener != null) {
                    myLocationListener.onLocationChanged(mLocation);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private LocationListener gpsLocationListener = new LocationListener() {

        /**
         * 位置更改时调用
         * @param location 位置信息
         */
        @Override
        public void onLocationChanged(Location location) {
            //位置信息变化时触发
            Log.e(TAG, "location:gpsLocationListener" + location.toString());
            if (isBetterLocation(location, mLocation)) {
                mLocationManager.removeUpdates(netWorkLocationListener);
                mLocation = location;
            }
            if (myLocationListener != null) {
                myLocationListener.onLocationChanged(mLocation);
            }
        }

        /**
         * GPS状态变化时触发
         * @param provider
         * @param status LocationProvider.AVAILABLE 可用； LocationProvider.OUT_OF_SERVICE 服务区外状态；
         *               LocationProvider.TEMPORARILY_UNAVAILABLE 暂停服务状态
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.e(TAG, "onStatusChanged" + "当前GPS状态为可见状态");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.e(TAG, "onStatusChanged" + "当前GPS状态为服务区外状态");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.e(TAG, "onStatusChanged" + "当前GPS状态为暂停服务状态");
                    break;
                default:
                    break;
            }
            if (myLocationListener != null) {
                myLocationListener.onStatusChanged(status);
            }
        }

        /**
         * GPS开启时触发
         */
        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled");
        }

        /**
         * GPS禁用时触发
         */
        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled");
        }
    };


    private MyLocationListener myLocationListener;

    public void setLocationListener(MyLocationListener myLocationListener) {
        this.myLocationListener = myLocationListener;
    }

    public interface MyLocationListener {
        /**
         * 位置信息
         *
         * @param location
         */
        void onLocationChanged(Location location);

        /**
         * GPS状态
         *
         * @param status
         */
        void onStatusChanged(int status);
    }


    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * 确定一个位置读数是否比当前位置修正更好
     *
     * @param location            您要评估的新位置
     * @param currentBestLocation 您要与新位置信息进行比较的当前位置信息
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // 新位置总比没有位置好
            return true;
        }

        // 检查新的位置修复是新的还是旧的
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // 如果距当前位置已超过两分钟，请使用新位置
        // 因为用户可能已经移动
        if (isSignificantlyNewer) {
            return true;
            //如果新位置的时间早于两分钟，那肯定会更糟
        } else if (isSignificantlyOlder) {
            return false;
        }

        // 检查新的位置修复是否或多或少准确
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // 检查旧位置和新位置是否来自同一提供商
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // 结合及时性和准确性来确定位置质量
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * 检查两个提供者是否相同
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


}
