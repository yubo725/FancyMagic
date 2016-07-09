package com.magic.fancymagic.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * 检测手机摇晃的监听器
 */
public class ShakeListener implements SensorEventListener {
    //速度阈值，当摇晃速度达到这值后产生作用
    private static final int SPEED_SHRESHOLD = 5000;
    //两次检测的时间间隔
    private static final int UPTATE_INTERVAL_TIME = 50;

    //传感器管理器
    private SensorManager sensorManager;
    //传感器
    private Sensor sensor;
    //重力感应监听器
    private OnShakeListener onShakeListener;
    //上下文
    private Context context;
    //手机上一个位置时重力感应坐标
    private float lastX;
    private float lastY;
    private float lastZ;

    //上次检测时间
    private long lastUpdateTime = 0;

    private boolean allowShake = true;

    //构造器
    public ShakeListener(Context c) {
        //获得监听对象
        context = c;
        registerListener();
    }

    //注册摇动监听
    public void registerListener() {
        //获得传感器管理器
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager != null) {
            //获得重力传感器
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //注册
            if(sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    //取消注册摇动监听
    public void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

    //摇晃监听接口
    public interface OnShakeListener {
        void onShake();
    }

    //设置重力感应监听器
    public void setOnShakeListener(OnShakeListener listener) {
        onShakeListener = listener;
    }

    //重力感应器感应获得变化数据
    public void onSensorChanged(SensorEvent event) {
        if(!allowShake)
            return ;
        //现在检测时间
        long currentUpdateTime = System.currentTimeMillis();
        //两次检测的时间间隔
        long timeInterval = currentUpdateTime - lastUpdateTime;
        //判断是否达到了检测时间间隔
        if(timeInterval < UPTATE_INTERVAL_TIME)
            return;
        //现在的时间变成last时间
        lastUpdateTime = currentUpdateTime;

        //获得x,y,z坐标
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        //获得x,y,z的变化值
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        float deltaZ = z - lastZ;

        //将现在的坐标变成last坐标
        lastX = x;
        lastY = y;
        lastZ = z;

        double speed = Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ)/timeInterval * 10000;
        //达到速度阀值，发出提示
        if(speed >= SPEED_SHRESHOLD) {
            onShakeListener.onShake();
            //2s后才允许再次摇动
            new Thread(new Runnable() {
                @Override
                public void run() {
                    allowShake = false;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    allowShake = true;
                }
            }).start();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}

