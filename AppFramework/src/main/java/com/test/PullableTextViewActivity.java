package com.test;

import com.demo.demo.R;
import com.library.pulltorefresh.classical.ClassicalPullToRefreshLayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PullableTextViewActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_activity_textview);
		((ClassicalPullToRefreshLayout) findViewById(R.id.refresh_view)).setOnRefreshListener(new MyListener2());
	}
}
