﻿package com.tyt.bbs;

import com.tyt.bbs.adapter.ArticelAdapter;
import com.tyt.bbs.adapter.CollectionAdapter;
import com.tyt.bbs.adapter.SimpleCollectionAdapter;
import com.tyt.bbs.parser.ArticleParser;
import com.tyt.bbs.provider.DataColums.PostData;
import com.tyt.bbs.utils.Property;
import com.tyt.bbs.view.LoadingDrawable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class CollectionActivity extends BaseActivity implements OnClickListener {

	private ListView collectionList;
	private SimpleCursorAdapter mAdapter;
	private ProgressBar mProgressBar;
	private ArticleParser mParser;
	private ArticelAdapter mArticelAdapter;
	private int _id=0;
	private int mode=0;  //0 为标题模式   1为阅读帖子模式

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_collection);
		initialView();
	}

	/**
	 * 初始化View
	 */
	private void initialView() {
		// TODO Auto-generated method stub
		collectionList= ((ListView)findViewById(R.id.listview_collection));
		Cursor  cursor =managedQuery(PostData.CONTENT_URI,null, null, null, null);
		//		mAdapter =new CollectionAdapter(this,R.layout.item_collection,cursor);

		mAdapter = new SimpleCollectionAdapter(this, R.layout.item_collection, cursor,
				new String[] { PostData.TIME, PostData.TITLE, PostData.AUTHOR, PostData.BOARD}, new int[] {R.id.tv_time,R.id.tv_title,R.id.tv_author,R.id.tv_board });
		collectionList.setAdapter(mAdapter);

		findViewById(R.id.btn_back).setOnClickListener(this);
		mProgressBar=(ProgressBar)findViewById(R.id.progress);
		mProgressBar.setIndeterminateDrawable(new LoadingDrawable(0,
				Color.parseColor("#4F337fd3"), Color.parseColor("#0d337fd3"), Color.TRANSPARENT, 200));

		collectionList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(mode==0){
					Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
					int index = cursor.getColumnIndex(PostData.TEXT);
					String fulltext = cursor.getString(index);
					new Listload(fulltext).execute();
					mode=1;
				}
			}

		});

		collectionList.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				if(mode==0){
					menu.add(0, 0, 0, R.string.delete_post);   
				}
			}

		});

		collectionList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(mode==0){
					Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
					int index = cursor.getColumnIndex(PostData._ID);
					_id = cursor.getInt(index);
				}
				return false;
			}

		});
	}


	public boolean onContextItemSelected(MenuItem item) {   
		switch (item.getItemId()) {   
		case 0:   
			Toast.makeText(this, "删除成功", 200).show();
			getContentResolver().delete(PostData.CONTENT_URI, PostData._ID+"="+_id,null);
			return true;     
		default:   
			//			return super.onContextItemSelected(item);   
			return false;
		}
	} 




	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if((keyCode == KeyEvent.KEYCODE_BACK)){
			if(mode==1){
				Message message  = new Message();
				message.what = 0x3;
				handler.sendMessage(message);
			}else{
				this.finish();
			}
		}
		return false;
	}

	// 获取文章列表 异步执行类
	private class Listload extends AsyncTask<String,Integer,Void>
	{
		String fullText;
		public Listload(String sourceString){
			fullText=sourceString;
		}
		@Override
		protected void onPreExecute() {
			mProgressBar.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... URL) {
			mParser = new ArticleParser("",handler,false,false);
			try {
				mParser.parser(fullText);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Message message  = new Message();
			message.what = 0x2;
			handler.sendMessage(message);
			super.onPostExecute(result);
		}
	}


	private Handler handler = new Handler() 
	{
		@Override
		public void handleMessage(Message msg) 
		{
			switch (msg.what) 
			{
			case 0x1:
				if(mAdapter!=null)mAdapter.notifyDataSetChanged();
				break;
			case 0x2:
				mArticelAdapter = new ArticelAdapter(CollectionActivity.this,mParser.getList(),true);
				collectionList.setAdapter(mArticelAdapter);
				if(mArticelAdapter.isEmpty()) return;
				mProgressBar.setVisibility(View.GONE);
				break;
			case 0x3:
				collectionList.setAdapter(mAdapter);
				mode=0;
				break;
			}
		}
	};

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btn_back:
			if(mode==1){
				Message message  = new Message();
				message.what = 0x3;
				handler.sendMessage(message);

			}else
				finish();
			break;
		}
	}

}
