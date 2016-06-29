package com.buffett.pulltorefresh.test;

import com.buffett.pulltorefresh.ui.PullToRefreshActivity;
import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


public class pullrefresh_test1 extends ActivityInstrumentationTestCase2<PullToRefreshActivity> {
  	private Solo solo;
  	
  	public pullrefresh_test1() {
		super(PullToRefreshActivity.class);
  	}

  	public void setUp() throws Exception {
        super.setUp();
		solo = new Solo(getInstrumentation());
		getActivity();
  	}
  
   	@Override
   	public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
  	}
  
	public void testRun() {
        //Wait for activity: 'com.buffett.pulltorefresh.ui.PullToRefreshActivity'
		solo.waitForActivity(com.buffett.pulltorefresh.ui.PullToRefreshActivity.class, 2000);
        //Set default small timeout to 15539 milliseconds
		Timeout.setSmallTimeout(15539);
        //Scroll View to the right side
		solo.scrollViewToSide(solo.getView(com.buffett.pulltorefresh.R.id.pager), Solo.RIGHT);
	}
}
