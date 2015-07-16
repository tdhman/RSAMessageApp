package com.example.rsamessageapp.request;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
 
public class AlertFragment extends DialogFragment {
	private String title;
	private String message;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		title = "Alert Dialog";
		message = getTag();
		return new AlertDialog.Builder(getActivity())
				// Set Dialog Icon
				//.setIcon(R.drawable.androidhappy)
				// Set Dialog Title
				.setTitle(title)
				// Set Dialog Message
				.setMessage(message)
 
				// Positive button
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Do something else
					}
				})
				// Negative Button
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,	int which) {
						// Do something else
					}
				}).create();
	}
}