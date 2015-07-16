package com.example.rsamessageapp;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rsamessageapp.request.AlertFragment;
import com.example.rsamessageapp.request.RequestTask;
import com.example.rsamessageapp.utils.Preferences;

/**
 * Created by javiermanzanomorilla on 12/05/15.
 */
public class EncryptAndDecryptFragment extends Fragment implements PagerSlide, OnItemClickListener, OnItemSelectedListener {

    public static TextView textToBeEncrypted;
    
    public static AutoCompleteTextView textPhoneNo;

    public static TextView encryptedText;

    public static TextView decryptedText;

    private Button encrypt;
    
    private Button send;
    
    private ArrayAdapter<String> adapter;
    
    public static ProgressDialog progress;
    
    // Store contacts values in these arraylist
    public static ArrayList<String> phoneValueArr = new ArrayList<String>();
    public static ArrayList<String> nameValueArr = new ArrayList<String>();    
    public static boolean isEncrypted = false;
    public String toNumberValue="";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encrypt_or_decrypt, container, false);
        textToBeEncrypted = (TextView) view.findViewById(R.id.text_to_be_encrypted);
        encryptedText = (TextView) view.findViewById(R.id.encrypted_text);
        decryptedText = (TextView) view.findViewById(R.id.decrypted_text);
        encrypt = (Button) view.findViewById(R.id.encrypt);
        encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Preferences.getString(Preferences.RSA_PRIVATE_KEY) == null) {
                    Toast.makeText(getActivity(), "There's now RSA KeyPair generated", Toast.LENGTH_LONG).show();
                    return;
                }
                if (textToBeEncrypted.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter a correct message to encrypt", Toast.LENGTH_LONG).show();
                    return;
                }
                progress = new ProgressDialog(getActivity());
    			progress.setTitle("Alert Dialog");
    			progress.setMessage("Getting public key from server...");
    			progress.setCancelable(false);
    			progress.show();
                getPublicKey();
                //String message = textToBeEncrypted.getText().toString();
                //String encryptedMessage = RSA.encryptWithStoredKey(message);
                //String decryptedMessage = RSA.decryptWithStoredKey(encryptedMessage);
                //encryptedText.setText("Encrypted Text: " + encryptedMessage);
                //decryptedText.setText("Decrypted Text: " + decryptedMessage);

                //Preferences.putString(Preferences.ENCRYPTED_MESSAGE, encryptedMessage);
            }
        });
        send = (Button) view.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (isEncrypted){
					isEncrypted = false;
					Intent sendIntent = new Intent(Intent.ACTION_VIEW);
					sendIntent.putExtra("address", toNumberValue);
					sendIntent.putExtra("sms_body", encryptedText.getText().toString());
					sendIntent.setData(Uri.parse("smsto:" + toNumberValue));
					sendIntent.setType("vnd.android-dir/mms-sms");
					startActivity(sendIntent);
				} else {
					AlertFragment alert = new AlertFragment();
					alert.show(getChildFragmentManager(), "Cannot send SMS before encryption!");
				}
					
			}
		});
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        textPhoneNo = (AutoCompleteTextView) view.findViewById(R.id.autoCompletePhoneNo);
        textPhoneNo.setThreshold(0);
        textPhoneNo.setAdapter(adapter);
        textPhoneNo.setOnItemSelectedListener(this);
        textPhoneNo.setOnItemClickListener(this);
        readContactData();
        return view;
    }

    @Override
    public List<Integer> getVisibleButtons() {
        List<Integer> res = new ArrayList<>();
        res.add(R.id.prev);
        res.add(R.id.next);
        return res;
    }

    @Override
    public String getSubtitle() {
        return "Encrypt / Decrypt";
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// Get Array index value for selected name
        int i = nameValueArr.indexOf(""+parent.getItemAtPosition(position));
       
       // If name exist in name ArrayList
       if (i >= 0) {            
           // Get Phone Number
           textPhoneNo.setText(phoneValueArr.get(i));
            
           InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
           imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);            
       } 

	}
	
	public void getPublicKey(){
		String url;
		try {
			toNumberValue = textPhoneNo.getText().toString();
			String id = toNumberValue.replaceAll("\\D+", "");
			if (id == null || id.isEmpty())
				throw new Exception("Unknown Phone Number");
			url = Preferences.BASE_URL + "get?id=" + URLEncoder.encode(id, "UTF-8");
			new RequestTask(getFragmentManager()).execute(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Read phone contact name and phone numbers 
    private void readContactData() {
        try {
            ContentResolver cr = getActivity().getBaseContext().getContentResolver();
             
            //Query to get contact name
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            					  new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER,
            									 ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            									 ContactsContract.CommonDataKinds.Phone._ID}, 
            									 null, 
            									 null, 
            									 ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false)
            {
                String contactNumber= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));  
                String contactName =  cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                //int phoneContactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));


                phoneValueArr.add(contactNumber);
        		nameValueArr.add(contactName);
        		adapter.add(contactName);
                cursor.moveToNext();
            }       
            cursor.close();
            cursor = null;
            Log.d("END","Got all Contacts");
        } catch (Exception e) {
             e.printStackTrace();
        }                      
   }

}
