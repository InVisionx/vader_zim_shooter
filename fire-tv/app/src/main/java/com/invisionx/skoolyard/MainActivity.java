package com.invisionx.skoolyard;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;

public class MainActivity extends Activity implements InputManager.InputDeviceListener {
    private WebView web;
    private InputManager inputs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final float[] axes = new float[4];
    private final float[] buttons = new float[17];
    private final boolean[] keyButtons = new boolean[17];
    private int deviceId = -1;
    private boolean ready, dirty = true, active = false;
    private final Runnable pump = new Runnable() {
        @Override public void run() {
            if (!active) return;
            if (ready && dirty) { dirty = false; sendPad(); }
            handler.postDelayed(this, 16);
        }
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        immersive();
        web = new WebView(this);
        web.setBackgroundColor(0xff101622);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.setFocusableInTouchMode(true);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) { return true; }
            @Override public void onPageFinished(WebView view, String url) { ready = true; dirty = true; }
        });
        setContentView(web);
        web.requestFocus();
        inputs = (InputManager) getSystemService(INPUT_SERVICE);
        inputs.registerInputDeviceListener(this, handler);
        findController();
        try (java.io.InputStream stream = getAssets().open("index.html")) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192]; int count;
            while ((count = stream.read(buffer)) != -1) out.write(buffer, 0, count);
            web.loadDataWithBaseURL("https://invisionx.github.io/vader_zim_shooter/", out.toString("UTF-8"), "text/html", "UTF-8", null);
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("Game could not load").setMessage(e.getMessage()).setPositiveButton("Close", (d,w)->finish()).show();
        }
    }
    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    private boolean isController(InputDevice d) {
        return d != null && (d.supportsSource(InputDevice.SOURCE_GAMEPAD) || d.supportsSource(InputDevice.SOURCE_JOYSTICK));
    }
    private void findController() {
        if (isController(InputDevice.getDevice(deviceId))) return;
        deviceId = -1;
        for (int id : InputDevice.getDeviceIds()) if (isController(InputDevice.getDevice(id))) { deviceId=id; break; }
        dirty=true;
    }
    private void clearInput() { Arrays.fill(axes,0); Arrays.fill(buttons,0); Arrays.fill(keyButtons,false); dirty=true; }
    private void sendPad() {
        if (deviceId == -1) { web.evaluateJavascript("window.__fireTVPad=null;",null); return; }
        try {
            InputDevice device=InputDevice.getDevice(deviceId);
            if(device==null) { deviceId=-1; dirty=true; return; }
            JSONObject pad=new JSONObject();
            pad.put("id", device.getName()+" (native Fire TV input)"); pad.put("index",999); pad.put("mapping","standard"); pad.put("connected",true);
            JSONArray a=new JSONArray(); for(float v:axes)a.put((double)v); pad.put("axes",a);
            JSONArray b=new JSONArray(); for(int i=0;i<buttons.length;i++) { float value=Math.max(buttons[i], keyButtons[i]?1:0); JSONObject item=new JSONObject();item.put("pressed",value>.45f);item.put("value",(double)value);b.put(item); } pad.put("buttons",b);
            web.evaluateJavascript("window.__fireTVPad="+pad.toString()+";",null);
        } catch(Exception ignored) { }
    }
    private float axis(MotionEvent e, int preferred, int alternate) {
        InputDevice d=e.getDevice();
        InputDevice.MotionRange range=d==null?null:d.getMotionRange(preferred,e.getSource());
        int selected=range!=null?preferred:alternate;
        return e.getAxisValue(selected);
    }
    @Override public boolean dispatchGenericMotionEvent(MotionEvent e) {
        if(e.isFromSource(InputDevice.SOURCE_JOYSTICK) && e.getAction()==MotionEvent.ACTION_MOVE) {
            if(deviceId!=e.getDeviceId())clearInput();deviceId=e.getDeviceId();
            axes[0]=e.getAxisValue(MotionEvent.AXIS_X);axes[1]=e.getAxisValue(MotionEvent.AXIS_Y);
            axes[2]=axis(e,MotionEvent.AXIS_Z,MotionEvent.AXIS_RX);axes[3]=axis(e,MotionEvent.AXIS_RZ,MotionEvent.AXIS_RY);
            buttons[6]=Math.max(0,axis(e,MotionEvent.AXIS_LTRIGGER,MotionEvent.AXIS_BRAKE));
            buttons[7]=Math.max(0,axis(e,MotionEvent.AXIS_RTRIGGER,MotionEvent.AXIS_GAS));
            float hx=e.getAxisValue(MotionEvent.AXIS_HAT_X),hy=e.getAxisValue(MotionEvent.AXIS_HAT_Y);
            buttons[12]=hy<-.5f?1:0;buttons[13]=hy>.5f?1:0;buttons[14]=hx<-.5f?1:0;buttons[15]=hx>.5f?1:0;
            dirty=true;return true;
        }
        return super.dispatchGenericMotionEvent(e);
    }
    private int buttonIndex(int code) {
        switch(code) {
            case KeyEvent.KEYCODE_BUTTON_A: case KeyEvent.KEYCODE_DPAD_CENTER: case KeyEvent.KEYCODE_ENTER:return 0;
            case KeyEvent.KEYCODE_BUTTON_B:return 1;
            case KeyEvent.KEYCODE_BUTTON_X:return 2;
            case KeyEvent.KEYCODE_BUTTON_Y:return 3;
            case KeyEvent.KEYCODE_BUTTON_L1:return 4;
            case KeyEvent.KEYCODE_BUTTON_R1:return 5;
            case KeyEvent.KEYCODE_BUTTON_L2:return 6;
            case KeyEvent.KEYCODE_BUTTON_R2:return 7;
            case KeyEvent.KEYCODE_BUTTON_SELECT:return 8;
            case KeyEvent.KEYCODE_BUTTON_START: case KeyEvent.KEYCODE_MENU: case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:return 9;
            case KeyEvent.KEYCODE_BUTTON_THUMBL:return 10;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:return 11;
            case KeyEvent.KEYCODE_DPAD_UP:return 12;
            case KeyEvent.KEYCODE_DPAD_DOWN:return 13;
            case KeyEvent.KEYCODE_DPAD_LEFT:return 14;
            case KeyEvent.KEYCODE_DPAD_RIGHT:return 15;
            default:return -1;
        }
    }
    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        int index=buttonIndex(e.getKeyCode());
        if(index>=0) {
            if(isController(e.getDevice())) { if(deviceId!=e.getDeviceId())clearInput();deviceId=e.getDeviceId(); }
            // Remote buttons can navigate the same game menu when no controller is paired.
            if(deviceId==-1)deviceId=e.getDeviceId();
            keyButtons[index]=e.getAction()==KeyEvent.ACTION_DOWN;
            dirty=true;return true;
        }
        return super.dispatchKeyEvent(e);
    }
    @Override public void onBackPressed() {
        web.evaluateJavascript("if(typeof pauseMatch==='function')pauseMatch();",null);
        new AlertDialog.Builder(this).setTitle("Call of Doom").setItems(new String[]{"Return to game menu","Exit to Fire TV"},(d,w)->{if(w==1)finish();else immersive();}).show();
    }
    @Override protected void onPause() {
        active=false;handler.removeCallbacks(pump);clearInput();
        if(web!=null){web.evaluateJavascript("window.__fireTVPad=null;if(typeof pauseMatch==='function')pauseMatch();",null);web.onPause();web.pauseTimers();}
        super.onPause();
    }
    @Override protected void onResume() {
        super.onResume();active=true;dirty=true;
        if(web!=null){web.onResume();web.resumeTimers();}handler.removeCallbacks(pump);handler.post(pump);immersive();
    }
    @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)immersive();}
    @Override protected void onDestroy(){active=false;handler.removeCallbacks(pump);if(inputs!=null)inputs.unregisterInputDeviceListener(this);if(web!=null)web.destroy();super.onDestroy();}
    @Override public void onInputDeviceAdded(int id){findController();}
    @Override public void onInputDeviceChanged(int id){if(id==deviceId){clearInput();}findController();}
    @Override public void onInputDeviceRemoved(int id){if(id==deviceId){clearInput();deviceId=-1;if(web!=null)web.evaluateJavascript("window.__fireTVPad=null;if(typeof pauseMatch==='function')pauseMatch();",null);findController();}}
}
