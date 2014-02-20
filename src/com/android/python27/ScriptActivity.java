/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Copyright (C) 2012, Anthony Prieur & Daniel Oppenheim. All rights reserved.
 *
 * Original from SL4A modified to allow to embed Interpreter and scripts into an APK
 */

package com.android.python27;

import com.android.python27.config.GlobalConstants;
import com.android.python27.support.Utils;
import com.googlecode.android_scripting.FileUtils;

import java.io.File;
import java.io.InputStream;

import se.leap.bitmaskclient.R;
import android.util.Log;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class ScriptActivity extends Activity {
	
	ProgressDialog myProgressDialog; 
	private static final String ScriptActivity_Tag = "COM.ANDROID.PYTHON27.SCRIPTACTIVITY";  
	
	public static final int INSTALL_PYTHON = 1;
	public static final int RUN_SERVICE = 2;
	public static final int INSTALLATION_NEEDED = 3;
	
	public static final String INSTALL_PYTHON_s= "INSTALL_P";
	public static final String RUN_SERVICE_s = "RUN_S";
	public static final String INSTALLATION_NEEDED_s = "INSTALLATION_N";
	
	private static String intentExtra = ""; 
	private static boolean returnValue = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// mounted sdcard ?
		//if (!Environment.getExternalStorageState().equals("mounted")) {
		//  Log.e(GlobalConstants.LOG_TAG, "External storage is not mounted");
		//  
		//  Toast toast = Toast.makeText( getApplicationContext(), "External storage not mounted", Toast.LENGTH_LONG);
		//  toast.show();
		//  return;
		//}
	  
		// install needed ?
//    	boolean installNeeded = isInstallNeeded();
//		
//    	if(installNeeded) {
//    	  setContentView(R.layout.install);	
//  		  new InstallAsyncTask().execute();
//    	}
//    	else {
//    	    runScriptService();
//    	    finish();
//    	}
	//	installPythonIfNeeded();
		//onStart();
		Intent intent = getIntent();
		
		Log.d(ScriptActivity_Tag, "intent: " + intent.getExtras());
		
		if (intent.hasExtra(INSTALLATION_NEEDED_s)){
			Log.d(ScriptActivity_Tag, "INSTALLATION NEEDED");
			intentExtra = INSTALLATION_NEEDED_s;
			returnValue = isInstallNeeded();
			finish();
		}
		else if (intent.hasExtra(INSTALL_PYTHON_s)){
			Log.d(ScriptActivity_Tag, "INSTALLATION PYTHON");
			intentExtra = INSTALL_PYTHON_s;
			installPythonIfNeeded();
			
		}
		else if (intent.hasExtra(RUN_SERVICE_s)){
			Log.d(ScriptActivity_Tag, "RUN SERVICE");
			intentExtra = RUN_SERVICE_s;
			runScriptService();
			finish();
		}
  }

	@Override
	public void finish(){
		 Intent data = new Intent();
		 data.putExtra(intentExtra, returnValue);
		  // Activity finished ok, return the data
		  setResult(RESULT_OK, data);
		  super.finish();
	}
	
	public void installPythonIfNeeded(){
		boolean installNeeded = isInstallNeeded();
		
    	if(installNeeded) {
    	  setContentView(R.layout.install);	
  		  new InstallAsyncTask().execute();
    	}
    /*	else {
    	    runScriptService();
    	    finish();
    	}*/

	}

	
/*	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK){
	    	if (requestCode == INSTALL_PYTHON) {
	    		setContentView(R.layout.install);
	    		new InstallAsyncTask();
	    	}
	    	else if (requestCode == RUN_SERVICE){
	    		runScriptService();
	    		finish();
	    	}
	    	else if (requestCode == INSTALLATION_NEEDED){
	    		data.putExtra("installation_needed", isInstallNeeded());
	    		setResult(resultCode, data);
	    	}
	    }
	      
	    }
	*/
	private void sendmsg(String key, String value) {
	      Message message = installerHandler.obtainMessage();
	      Bundle bundle = new Bundle();
	      bundle.putString(key, value);
	      message.setData(bundle);
	      installerHandler.sendMessage(message);
	   }
	    
	   final Handler installerHandler = new Handler() {
	   @Override
	   public void handleMessage(Message message) {
		        Bundle bundle = message.getData();
		        
		        if (bundle.containsKey("showProgressDialog")) {
		 	       myProgressDialog = ProgressDialog.show(ScriptActivity.this, "Installing", "Loading", true); 
		        }
		        else if (bundle.containsKey("setMessageProgressDialog")) {
		        	if (myProgressDialog.isShowing()) {
			        	myProgressDialog.setMessage(bundle.getString("setMessageProgressDialog"));
		        	}
		        }
		        else if (bundle.containsKey("dismissProgressDialog")) {
		        	if (myProgressDialog.isShowing()) {
			        	myProgressDialog.dismiss();
		        	}
		        }
		        else if (bundle.containsKey("installSucceed")) {
		  		  Toast toast = Toast.makeText( getApplicationContext(), "Install Succeed", Toast.LENGTH_LONG);
				  toast.show();
		        }
		        else if (bundle.containsKey("installFailed")) {
			  		  Toast toast = Toast.makeText( getApplicationContext(), "Install Failed. Please check logs.", Toast.LENGTH_LONG);
					  toast.show();
			    }
	       }
	   };
	   
	  public class InstallAsyncTask extends AsyncTask<Void, Integer, Boolean> {
		   @Override
		   protected void onPreExecute() {
		   }
	
		   @Override
		   protected Boolean doInBackground(Void... params) {	    
	    	Log.i(GlobalConstants.LOG_TAG, "Installing...");

	    	// show progress dialog
	    	sendmsg("showProgressDialog", "");

	    	sendmsg("setMessageProgressDialog", "Installing Python modules for Bitmask. Please wait...");
	    	createOurExternalStorageRootDir();
	
			// Copy all resources
			copyResourcesToLocal();
	
			// TODO
		    return true;
		   }
	
		   @Override
		   protected void onProgressUpdate(Integer... values) {
		   }
	
		   @Override
		   protected void onPostExecute(Boolean installStatus) {
	    	sendmsg("dismissProgressDialog", "");
	    	
	    	if(installStatus) {
		    	sendmsg("installSucceed", "");
		    	returnValue=true;
	    	}
	    	else {
		    	sendmsg("installFailed", "");
		    	returnValue=false;
	    	}
	    	
		//    runScriptService();
		    finish();
		   }
	   
	  }
	
  private void runScriptService() {
	  if(GlobalConstants.IS_FOREGROUND_SERVICE) {
		  startService(new Intent(this, ScriptService.class));
	  }
	  else {
		  startService(new Intent(this, BackgroundScriptService.class)); 
	  }
  }
  
	private void createOurExternalStorageRootDir() {
		Utils.createDirectoryOnExternalStorage( this.getPackageName() );
	}
	
	// quick and dirty: only test a file
	public boolean isInstallNeeded() {
		File testedFile = new File(this.getFilesDir().getAbsolutePath()+ "/" + GlobalConstants.PYTHON_MAIN_SCRIPT_NAME);
			if(!testedFile.exists()) {
				return true;
			}
		return false;
	}
	
	
	 private void copyResourcesToLocal() {
			String name, sFileName;
			InputStream content;
			
			R.raw a = new R.raw();
			java.lang.reflect.Field[] t = R.raw.class.getFields();
			Resources resources = getResources();
			
			boolean succeed = true;
			
			for (int i = 0; i < t.length; i++) {
				try {
					name = resources.getText(t[i].getInt(a)).toString();
					sFileName = name.substring(name.lastIndexOf('/') + 1, name.length());
					content = getResources().openRawResource(t[i].getInt(a));
					content.reset();

					// python project
					if(sFileName.endsWith(GlobalConstants.PYTHON_PROJECT_ZIP_NAME)) {
						succeed &= Utils.unzip(content, this.getFilesDir().getAbsolutePath()+ "/", true);
					}
					// python -> /data/data/com.android.python27/files/python
					else if (sFileName.endsWith(GlobalConstants.PYTHON_ZIP_NAME)) {
						succeed &= Utils.unzip(content, this.getFilesDir().getAbsolutePath()+ "/", true);
						FileUtils.chmod(new File(this.getFilesDir().getAbsolutePath()+ "/python/bin/python" ), 0755);
					}
					// python extras -> /sdcard/com.android.python27/extras/python
					else if (sFileName.endsWith(GlobalConstants.PYTHON_EXTRAS_ZIP_NAME)) {
						Utils.createDirectoryOnExternalStorage( this.getPackageName() + "/" + "extras");
						Utils.createDirectoryOnExternalStorage( this.getPackageName() + "/" + "extras" + "/" + "tmp");
						succeed &= Utils.unzip(content, Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName() + "/extras/", true);
					}
					
				} catch (Exception e) {
					Log.e(GlobalConstants.LOG_TAG, "Failed to copyResourcesToLocal", e);
					succeed = false;
				}
			} // end for all files in res/raw
			
	 }

  @Override
  protected void onStart() {
	  super.onStart();
	
	  String s = "System infos:";
	  s += " OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
	  s += " | OS API Level: " + android.os.Build.VERSION.SDK;
	  s += " | Device: " + android.os.Build.DEVICE;
	  s += " | Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
	  
	  Log.i(GlobalConstants.LOG_TAG, s);

	  //finish();
  }
  
}
