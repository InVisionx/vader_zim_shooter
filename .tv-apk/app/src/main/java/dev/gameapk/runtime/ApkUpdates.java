package dev.gameapk.runtime;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.FileProvider;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;
final class ApkUpdates {
 static void check(Activity activity) {
  if(!BuildConfig.AUTO_UPDATES)return;
  new Thread(()->{try {
   JSONObject info=new JSONObject(new String(fetch(BuildConfig.RELEASE_ROOT+"update.json",16384),"UTF-8"));
   if(info.getInt("versionCode")<=BuildConfig.VERSION_CODE)return;
   File folder=new File(activity.getFilesDir(),"updates");folder.mkdirs();
   File apk=new File(folder,"update.apk");
   String expected=info.getString("sha256");
   byte[] data=null;
   if(apk.isFile())try(FileInputStream in=new FileInputStream(apk)){data=read(in,150*1024*1024);}
   if(data==null || !sha(data).equals(expected))data=fetch(BuildConfig.RELEASE_ROOT+"game.apk",150*1024*1024);
   if(!sha(data).equals(expected))return;
   File pending=new File(folder,"pending.apk");try(FileOutputStream out=new FileOutputStream(pending)){out.write(data);}if(apk.exists())apk.delete();if(!pending.renameTo(apk))return;
   PackageManager pm=activity.getPackageManager();
   PackageInfo installed=pm.getPackageInfo(activity.getPackageName(),PackageManager.GET_SIGNATURES);
   PackageInfo candidate=pm.getPackageArchiveInfo(apk.getAbsolutePath(),PackageManager.GET_SIGNATURES);
   if(candidate==null || !candidate.packageName.equals(installed.packageName) || candidate.versionCode<=installed.versionCode || !Arrays.equals(candidate.signatures,installed.signatures)){apk.delete();return;}
   activity.runOnUiThread(()->{if(!activity.isFinishing())new AlertDialog.Builder(activity).setTitle("Game update ready").setMessage("An update has downloaded. Fire TV will ask you to approve installation.").setPositiveButton("Install",(d,w)->install(activity,apk)).setNegativeButton("Later",null).show();});
  }catch(Exception ignored){}},"GameApkUpdate").start();
 }
 static void install(Activity activity,File apk) {
  try {
   if(Build.VERSION.SDK_INT>=26&&!activity.getPackageManager().canRequestPackageInstalls()) {
    new AlertDialog.Builder(activity).setTitle("Allow app updates").setMessage("Allow this app under Install unknown apps, then reopen the game to install the downloaded update.").setPositiveButton("Settings",(d,w)->activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+activity.getPackageName())))).setNegativeButton("Cancel",null).show();return;
   }
   Uri uri=FileProvider.getUriForFile(activity,activity.getPackageName()+".updates",apk);
   Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);activity.startActivity(intent);
  }catch(Exception e){new AlertDialog.Builder(activity).setTitle("Install manually").setMessage("Open the game's GitHub release in Downloader to install this update.").setPositiveButton("OK",null).show();}
 }
 private static byte[] read(InputStream in,int max)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new IOException("Too large");out.write(b,0,n);}return out.toByteArray();}
 private static String sha(byte[] bytes)throws Exception{StringBuilder out=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))out.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return out.toString();}
 private static byte[] fetch(String address,int max)throws Exception {
  for(int hops=0;hops<6;hops++) {
   URL url=new URL(address);String host=url.getHost();if(!url.getProtocol().equals("https")||!(host.equals("github.com")||host.endsWith(".githubusercontent.com")))throw new IOException("Unexpected host");
   HttpsURLConnection c=(HttpsURLConnection)url.openConnection();c.setConnectTimeout(5000);c.setReadTimeout(15000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","GameApkBuilder/1");
   try{int status=c.getResponseCode();if(status>=300&&status<400){address=new URL(url,c.getHeaderField("Location")).toString();continue;}if(status!=200)throw new IOException("Unavailable");try(InputStream in=c.getInputStream()){return read(in,max);}}finally{c.disconnect();}
  }throw new IOException("Too many redirects");
 }
}
