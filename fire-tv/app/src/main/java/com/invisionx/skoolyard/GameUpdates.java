package com.invisionx.skoolyard;

import android.app.Activity;
import android.content.Context;
import android.util.AtomicFile;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.net.ssl.HttpsURLConnection;

/** Downloads game content only. APK installation and native code are unchanged. */
final class GameUpdates {
    private static final String ROOT="https://github.com/InVisionx/vader_zim_shooter/releases/download/fire-tv-latest/";
    private static final String PREFS="game-content";
    private static final int MAX_HTML=3*1024*1024;
    private static File file(Context c){return new File(c.getFilesDir(),"game-update.html");}
    static String startupHTML(Context c) throws Exception {
        long cachedVersion=c.getSharedPreferences(PREFS,0).getLong("version",0);
        if(cachedVersion>BuildConfig.GAME_CONTENT_VERSION) {
            try(InputStream stream=new AtomicFile(file(c)).openRead()) {
                byte[] data=read(stream,MAX_HTML);
                String expected=c.getSharedPreferences(PREFS,0).getString("sha256","");
                if(hash(data).equals(expected))return new String(data,StandardCharsets.UTF_8);
            }catch(Exception ignored) { /* Interrupted or invalid cache: use bundled game. */ }
            new AtomicFile(file(c)).delete();
            c.getSharedPreferences(PREFS,0).edit().remove("version").remove("sha256").commit();
        }
        try(InputStream stream=c.getAssets().open("index.html")){return new String(read(stream,MAX_HTML),StandardCharsets.UTF_8);}
    }
    static void check(Activity activity) {
        new Thread(()->{
            try {
                JSONObject manifest=new JSONObject(new String(download(ROOT+"game-update.json",16384),StandardCharsets.UTF_8));
                long version=manifest.getLong("version");
                long current=Math.max(BuildConfig.GAME_CONTENT_VERSION,activity.getSharedPreferences(PREFS,0).getLong("version",0));
                if(version<=current)return;
                if(manifest.getInt("minAppVersion")>BuildConfig.VERSION_CODE) {
                    activity.runOnUiThread(()->{if(!activity.isFinishing())Toast.makeText(activity,"A new app version is available on the game install page.",Toast.LENGTH_LONG).show();});
                    return;
                }
                // Fixed repository asset URL: the manifest cannot redirect us to other content.
                byte[] data=download(ROOT+"game.html",MAX_HTML);
                String expected=manifest.getString("sha256");
                if(!hash(data).equals(expected))return;
                String html=new String(data,StandardCharsets.UTF_8);
                if(!html.contains("window.__fireTVPad") || !html.contains("pollController"))return;
                AtomicFile target=new AtomicFile(file(activity));FileOutputStream out=null;
                try {out=target.startWrite();out.write(data);target.finishWrite(out);} catch(Exception e){if(out!=null)target.failWrite(out);throw e;}
                activity.getSharedPreferences(PREFS,0).edit().putLong("version",version).putString("sha256",expected).commit();
                activity.runOnUiThread(()->{if(!activity.isFinishing())Toast.makeText(activity,"Game update downloaded. It will apply next time you open the app.",Toast.LENGTH_LONG).show();});
            }catch(Exception ignored){ /* Offline, unavailable, or incomplete release: keep current game. */ }
        },"GameContentUpdate").start();
    }
    private static byte[] read(InputStream stream,int maximum)throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
        while((n=stream.read(buffer))!=-1){if(out.size()+n>maximum)throw new java.io.IOException("Update too large");out.write(buffer,0,n);}return out.toByteArray();
    }
    private static String hash(byte[] data)throws Exception {
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(data);StringBuilder text=new StringBuilder();for(byte b:digest)text.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return text.toString();
    }
    private static byte[] download(String address,int maximum)throws Exception {
        for(int redirects=0;redirects<6;redirects++) {
            URL url=new URL(address);String host=url.getHost();
            if(!"https".equals(url.getProtocol()) || !(host.equals("github.com") || host.endsWith(".githubusercontent.com")))throw new java.io.IOException("Unexpected update host");
            HttpsURLConnection connection=(HttpsURLConnection)url.openConnection();
            connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(5000);connection.setReadTimeout(8000);connection.setRequestProperty("User-Agent","SkoolyardTV-Updates/0.2");
            try {
                int status=connection.getResponseCode();
                if(status>=300&&status<400){address=new URL(url,connection.getHeaderField("Location")).toString();continue;}
                if(status!=200)throw new java.io.IOException("Update unavailable");
                try(InputStream stream=connection.getInputStream()){return read(stream,maximum);}
            }finally{connection.disconnect();}
        }
        throw new java.io.IOException("Too many redirects");
    }
}
