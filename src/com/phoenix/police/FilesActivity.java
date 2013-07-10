package com.phoenix.police;

import java.io.FileFilter;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.os.Bundle;

public class FilesActivity extends Activity implements ActionBar.TabListener{

	private static String STATE_SELECTED_NAVIGATION_ITEM = "state_selected_navigation_item";
	FragmentManager manager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.files_main);
		manager = getFragmentManager();
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		actionBar.addTab(actionBar.newTab().setText(R.string.camera)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.video)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.record)
				.setTabListener(this));
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)){
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
		
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		FragmentTransaction transaction = manager.beginTransaction();
		switch(tab.getPosition()){
		case 0:
			
			CameraFragment cFragment = new CameraFragment();
			transaction.replace(R.id.forfragment, cFragment);
			transaction.commit();
			break;
		case 1:
			VideoFragment vFragment = new VideoFragment();
			transaction.replace(R.id.forfragment, vFragment);
			transaction.commit();
			break;
		case 2:
			AudioFragment aFragment = new AudioFragment();
			transaction.replace(R.id.forfragment, aFragment);
			transaction.commit();
			break;
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
