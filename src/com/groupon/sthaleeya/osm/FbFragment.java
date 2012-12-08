package com.groupon.sthaleeya.osm;

import com.facebook.widget.LoginButton;

import com.groupon.sthaleeya.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FbFragment extends Fragment {
	private LoginButton authButton;

	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.activity_osmloader, container, false);

	    authButton = (LoginButton) view.findViewById(R.id.authButton);
	    authButton.setApplicationId(getString(R.string.app_id));

	    return view;
	}
}