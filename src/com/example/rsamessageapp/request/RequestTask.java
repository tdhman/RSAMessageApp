package com.example.rsamessageapp.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;

import com.example.rsamessageapp.EncryptAndDecryptFragment;
import com.example.rsamessageapp.crypto.RSA;
import com.example.rsamessageapp.utils.Preferences;

public class RequestTask extends AsyncTask<String, String, String> {
	
	private FragmentManager fm;
	
	public RequestTask(FragmentManager fm){
		this.fm = fm;
	}

	/** 
	 * Send a HTTP request to server
	 * Get the response in json format 
	 * @param URL to RESTful service
	 */
	@Override
	protected String doInBackground(String... params) {
		URL url;
		HttpURLConnection urlConnection = null;
		BufferedReader in = null;
		String result = "";
		try {
			url = new URL(params[0]);
			urlConnection = (HttpURLConnection) url.openConnection();
			if (urlConnection.getResponseCode()==201 || urlConnection.getResponseCode() == 200){
				 in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) 
				    result += inputLine;
				in.close();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		    
		return result;
	}
	
	/**
	 * Depend on the HTTP request, show corresponding dialog.
	 */
	@Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
			JSONObject json = new JSONObject(result);
			boolean status = json.getBoolean("status");
			String tag = json.getString("tag");
			AlertFragment alertFragment = new AlertFragment();
			if (tag.equalsIgnoreCase("register")){
				if (status) {
					alertFragment.show(fm, "Registered public key to server with id : " + json.getString("message"));
				} else
					alertFragment.show(fm, "Failed to sent key with error : " + json.getString("message"));
			} else if (tag.equalsIgnoreCase("get")){
				if (!status)
					alertFragment.show(fm, json.getString("message")); // should be error message
				else {
					// Find public key to encrypt message
					EncryptAndDecryptFragment.progress.setMessage("Encrypting message...");
					String key = json.getString("message");
					String message = EncryptAndDecryptFragment.textToBeEncrypted.getText().toString();
					try {
		                String encryptedMessage = RSA.encryptWithKey(key, message);
		                String decryptedMessage = RSA.decryptWithStoredKey(encryptedMessage);
		                EncryptAndDecryptFragment.encryptedText.setText("Encrypted Text: " + encryptedMessage);
		                EncryptAndDecryptFragment.decryptedText.setText("Decrypted Text: " + decryptedMessage);
		                EncryptAndDecryptFragment.isEncrypted = true;
		                Preferences.putString(Preferences.ENCRYPTED_MESSAGE, encryptedMessage);
					} catch (Exception e){
					alertFragment.show(fm, "Cannot encrypt message with that key!");
					}
				}
				EncryptAndDecryptFragment.progress.dismiss();
			} else if (tag.equalsIgnoreCase("fetch")){
				if (status){
					
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
}
