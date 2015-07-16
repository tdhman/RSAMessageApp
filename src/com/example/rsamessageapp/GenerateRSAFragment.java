package com.example.rsamessageapp;

import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rsamessageapp.crypto.Crypto;
import com.example.rsamessageapp.crypto.RSA;
import com.example.rsamessageapp.request.RequestTask;
import com.example.rsamessageapp.utils.Preferences;

/**
 * Created by javiermanzanomorilla on 12/05/15.
 */
public class GenerateRSAFragment extends Fragment implements PagerSlide {

    private TextView generated;

    private ProgressBar progress;

    private TextView privateKey;

    private View completedContainer;

    private TextView publicKey;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generate_rsa, container, false);
        generated = (TextView) view.findViewById(R.id.generated);
        progress = (ProgressBar) view.findViewById(R.id.progress);
        privateKey = (TextView) view.findViewById(R.id.private_key);
        publicKey = (TextView) view.findViewById(R.id.public_key);
        completedContainer = view.findViewById(R.id.completed);
        if (Preferences.getString(Preferences.RSA_PRIVATE_KEY) != null) {
            showKeyPair();
        }
        view.findViewById(R.id.generate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Preferences.clear();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                completedContainer.setVisibility(View.GONE);
                                generated.setVisibility(View.GONE);
                                publicKey.setVisibility(View.GONE);
                                privateKey.setVisibility(View.GONE);
                                progress.setVisibility(View.VISIBLE);
                            }
                        });
                        final long timeStarted = System.currentTimeMillis();
                        final KeyPair keyPair = RSA.generate();
                        Crypto.writePrivateKeyToPreferences(keyPair);
                        Crypto.writePublicKeyToPreferences(keyPair);
                        final long timeFinished = System.currentTimeMillis();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                generated.setText(getString(R.string.rsa_key_generated) + " in " + (timeFinished - timeStarted) + " ms");
                                showKeyPair();
                                sendKeyToServer(Crypto.stripPublicKeyHeaders(Preferences.getString(Preferences.RSA_PUBLIC_KEY)));
                            }
                        });
                    }
                }).start();
            }
        });
        return view;
    }
    
    public void sendKeyToServer(String key){
    	 // call AsynTask to perform network operation on separate thread
        String url;
		try {
			String id = Preferences.getPhoneNumber().replaceAll("\\D+", "");
			if (id == null || id.isEmpty())
				throw new Exception("Unknown Phone Number");
			url = Preferences.BASE_URL + "register?id=" + URLEncoder.encode(id, "UTF-8") + "&key=" + URLEncoder.encode(key, "UTF-8");
			new RequestTask(getFragmentManager()).execute(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void showKeyPair() {
        progress.setVisibility(View.GONE);
        generated.setVisibility(View.VISIBLE);
        completedContainer.setVisibility(View.VISIBLE);
        publicKey.setVisibility(View.VISIBLE);
        privateKey.setVisibility(View.VISIBLE);
        privateKey.setText(Crypto.stripPrivateKeyHeaders(Preferences.getString(Preferences.RSA_PRIVATE_KEY)));
        publicKey.setText(Crypto.stripPublicKeyHeaders(Preferences.getString(Preferences.RSA_PUBLIC_KEY)));
    }

    @Override
    public List<Integer> getVisibleButtons() {
        List<Integer> res = new ArrayList<>();
        res.add(R.id.next );
        return res;
    }

    @Override
    public String getSubtitle() {
        return "Generate RSA";
    }

}
