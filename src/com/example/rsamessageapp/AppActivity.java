package com.example.rsamessageapp;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.example.rsamessageapp.request.AlertFragment;
import com.example.rsamessageapp.utils.Preferences;

public class AppActivity extends ActionBarActivity implements OnTabChangeListener {

    private TabHost mTabHost;
    private static final String TAB_MESSAGE = "Send Message";
	private static final String TAB_LIST = "Contact List";
	
	private ListView listView;
	private TextView textView;
	private SimpleCursorAdapter mAdapter;
    private MatrixCursor mMatrixCursor;
    
    private ViewPager pager;
    Fragment[] fragments = new Fragment[]{
            new GenerateRSAFragment(),
            new EncryptAndDecryptFragment(),
            new VerifyAndSignFragment()
    };
    private Button prev;
    private Button next;
    
    private String phoneNo;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app);
		setTitle("RSA Message App");		
		Preferences.init(this);
		
        mTabHost = (TabHost)findViewById(R.id.tabhost);
        mTabHost.setup();
        mTabHost.addTab(mTabHost.newTabSpec(TAB_MESSAGE).setIndicator(TAB_MESSAGE).setContent(R.id.tab1));
        mTabHost.addTab(mTabHost.newTabSpec(TAB_LIST).setIndicator(TAB_LIST).setContent(R.id.tab2));
        mTabHost.setOnTabChangedListener(this);
        
        // The contacts from the contacts content provider is stored in this cursor
        mMatrixCursor = new MatrixCursor(new String[] { "_id","name","photo","details"} );
        
        // Adapter to set data in the listview
        mAdapter = new SimpleCursorAdapter(getBaseContext(),
            R.layout.contacts_list_item,
            null,
            new String[] { "name","photo","details"},
            new int[] { R.id.textName,R.id.imagePhoto,R.id.textDetails}, 0);

        textView = (TextView) findViewById(R.id.info_text);
        listView = (ListView) findViewById(R.id.listVew);
        listView.setAdapter(mAdapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		});
        
        pager = (ViewPager) findViewById(R.id.pager);
        prev = (Button) findViewById(R.id.prev);
        next = (Button) findViewById(R.id.next);
        pager.setAdapter(new PagerAdapter());
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                PagerSlide f = (PagerSlide) fragments[position];
                List<Integer> visibleButtons = f.getVisibleButtons();
                prev.setVisibility(visibleButtons.contains(R.id.prev) ? View.VISIBLE : View.GONE);
                next.setVisibility(visibleButtons.contains(R.id.next) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
            }
        });
        
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(pager.getCurrentItem() - 1);
            }
        });
        
        if (Preferences.getBoolean(Preferences.FIRST_RUN, true)) {
            // Do first run stuff here then set 'firstrun' as false
			setTitle("RSA Sample: " + ((PagerSlide)fragments[0]).getSubtitle());
            Preferences.putBoolean(Preferences.FIRST_RUN, false);
        } else {
        	setTitle("RSA Sample: " + ((PagerSlide)fragments[1]).getSubtitle());
        	pager.setCurrentItem(1);
        }
        
        new ListViewContactsLoader().execute();
	}
	
	@SuppressWarnings("unused")
	private void hideKeyboard() {   
	    // Check if no view has focus:
	    View view = this.getCurrentFocus();
	    if (view != null) {
	        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	      super.onCreateContextMenu(menu, v, menuInfo);
	      if (v.getId()==R.id.listVew) {
	          MenuInflater inflater = getMenuInflater();
	          inflater.inflate(R.menu.menu_item, menu);
	          
	          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	          int itemID = info.position;
	          Cursor cursor = (Cursor) mAdapter.getItem(itemID);
	          menu.setHeaderTitle("" + cursor.getString(1));
	      }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	      int itemID = info.position;
          Cursor cursor = (Cursor) mAdapter.getItem(itemID);
          phoneNo = cursor.getString(3);
          if (phoneNo.isEmpty()) {
    		  new AlertFragment().show(getSupportFragmentManager(), "No phone number found!");
    		  return super.onContextItemSelected(item);
          }
          else
		      switch(item.getItemId()) {
			      case R.id.sendSMS:
			    	  Intent sendIntent = new Intent(Intent.ACTION_VIEW);
					  sendIntent.putExtra("address", phoneNo);
					  sendIntent.setData(Uri.parse("smsto:" + phoneNo));
					  sendIntent.setType("vnd.android-dir/mms-sms");
					  startActivity(sendIntent);
			    	  return true;
			      case R.id.sendRSASMS:
			    	  EncryptAndDecryptFragment.textPhoneNo.setText(phoneNo);
			    	  pager.setCurrentItem(1);
			    	  mTabHost.setCurrentTab(0);
			    	  return true;
		          default:
		                return super.onContextItemSelected(item);
		      }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			pager.setCurrentItem(0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabChanged(String tabId) {
		
		if (AppActivity.TAB_MESSAGE.equals(tabId)) {			
			mTabHost.setCurrentTab(0);
			return;
		}
		if (AppActivity.TAB_LIST.equals(tabId)) {						
			mTabHost.setCurrentTab(1);
			return;
		}		
	}
	
	/** An AsyncTask class to retrieve and load listview with contacts */
    private class ListViewContactsLoader extends AsyncTask<Void, Void, Cursor>{
 
        @Override
        protected Cursor doInBackground(Void... params) {
            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
 
            // Querying the table ContactsContract.Contacts to retrieve all the contacts
            Cursor contactsCursor = getContentResolver().query(contactsUri, null, null, null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
 
            if(contactsCursor.moveToFirst()){
                do{
                    long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));
 
                    Uri dataUri = ContactsContract.Data.CONTENT_URI;
 
                    // Querying the table ContactsContract.Data to retrieve individual items like
                    // home phone, mobile phone, work email etc corresponding to each contact
                    Cursor dataCursor = getContentResolver().query(dataUri, null,
                                        ContactsContract.Data.CONTACT_ID + "=" + contactId,
                                        null, null);
 
                    String displayName="";
                    String mobilePhone="";
                    String homePhone="";
                    String workPhone="";
 
                    if(dataCursor.moveToFirst()){
                        // Getting Display Name
                        displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME ));
                        do{
                            // Getting Phone numbers
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)){
                                switch(dataCursor.getInt(dataCursor.getColumnIndex("data2"))){
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME :
                                        homePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE :
                                        mobilePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK :
                                        workPhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                }
                            }
                        } while(dataCursor.moveToNext());
                        
                        String details = "";
 
                        // Concatenating various information to single string
                        if(homePhone != null && !homePhone.equals("") )
                            details = "HomePhone : " + homePhone + "\n";
                        if(mobilePhone != null && !mobilePhone.equals("") )
                            details += "MobilePhone : " + mobilePhone + "\n";
                        if(workPhone != null && !workPhone.equals("") )
                            details += "WorkPhone : " + workPhone + "\n";
 
                        // Adding id, display name, path to photo and other details to cursor
                        if (!mobilePhone.isEmpty())
                        	mMatrixCursor.addRow(new Object[]{ Long.toString(contactId),displayName, R.drawable.girl_avatar, mobilePhone});
                    }
                    dataCursor.close();
                }while(contactsCursor.moveToNext());
                contactsCursor.close();
            }
            return mMatrixCursor;
        }
 
        @Override
        protected void onPostExecute(Cursor result) {
        	textView.setVisibility(View.GONE);
        	textView.setText(result.getCount() + " contact(s) in total.");
            // Setting the cursor containing contacts to listview
            mAdapter.swapCursor(result);
        }
    }
    
    class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

    }
}

