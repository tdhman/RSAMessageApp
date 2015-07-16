package com.example.rsamessageapp;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.rsamessageapp.utils.Preferences;

public class MainActivity extends ActionBarActivity {
	
	private ViewPager pager;

    Fragment[] fragments = new Fragment[]{
            new GenerateRSAFragment(),
            new EncryptAndDecryptFragment(),
            new VerifyAndSignFragment()
    };

    private Button prev;

    private Button next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Preferences.init(this);
        
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
                setTitle("RSA Sample: " + f.getSubtitle());
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
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
