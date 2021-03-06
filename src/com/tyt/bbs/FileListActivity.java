﻿/**
 * 
 */
package com.tyt.bbs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tyt.bbs.adapter.ListFileAdapter;
import com.tyt.bbs.utils.FileOperate;
import com.tyt.bbs.utils.ImageOperate;
import com.tyt.bbs.view.LoadingDrawable;

/**
 * @author yyj2011
 *com.tyt.bbs
 */
public class FileListActivity extends BaseActivity implements OnClickListener{

	public Handler handler = null;	
	public static final int TEXT_CHANGE = 0x0;
	public final int  SELECT_PIC=0x1;
	private File mFile;
	private MyFileFilter mFilter = null;
	private Comparator<File> comparator;	
	private ListFileAdapter mAdapter;
	private List<File> mSubFile ;

	private View layout;
	private TextView tv = null;
	private ProgressBar    mProgressBar;
	private ListView    mListView;
	private Button btn_selectOK;
	private ArrayList<String> mListPicPath;
	private ArrayList<Integer> checkId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_filelist);
		handler =new MyHandler();
		comparator = new Comparator<File>(){

			@Override
			public int compare(File f1, File f2) {
				// TODO Auto-generated method stub
				if(f1.isDirectory()&&f2.isDirectory()) return  f1.getName().compareToIgnoreCase(f2.getName());
				else if(f1.isDirectory()) return -1;
				else if(f2.isDirectory()) return 1;
				else return f1.getName().compareToIgnoreCase(f2.getName());
			}

		};
		mFile=new File("/sdcard");
		mSubFile =new ArrayList<File>();
		mAdapter=new ListFileAdapter(this,getmSubFile());
		mFilter=new MyFileFilter();
		initialView();
		initialListView();
	}

	private void initialView(){
		tv=(TextView)findViewById(R.id.listHeader);
		tv.setText(mFile.getPath());
		layout = findViewById(R.id.layout_select);
		//		layout.setVisibility(View.VISIBLE);		
		btn_selectOK= (Button) findViewById(R.id.btn_selPicOK);
		btn_selectOK.setOnClickListener(this);
		mProgressBar=(ProgressBar) findViewById(R.id.progressbar_filelist);
		mProgressBar.setIndeterminateDrawable(new LoadingDrawable(0,Color.parseColor("#dF337fd3"), Color.parseColor("#0d337fd3"), Color.TRANSPARENT, 200));
		mProgressBar.setVisibility(View.GONE);

	}

	private void initialListView(){

		mListView=(ListView) findViewById(R.id.filelist);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				File file = mAdapter.getItem(position);

				String strfilename = file.getName().toLowerCase();
				if(file.isDirectory()&&file.canRead()) {
					mFile=file;
					Message message = new Message();
					message.what =TEXT_CHANGE;    
					handler.sendMessage(message); 
				}else 
					if(strfilename.endsWith(".jpg")||strfilename.endsWith(".jpeg")
							||strfilename.endsWith(".png")||strfilename.endsWith(".bmp")
							||strfilename.endsWith(".gif")){
						
						checkId=mAdapter.getChecked();
						if(!checkId.contains(position)){
							checkId.add(new Integer(position));
						}

						else if(checkId.contains(position))
							checkId.remove(new Integer(position));
						view.setBackgroundColor(R.color.delete_color_filter);
						Message message  = new Message();
						message.what =SELECT_PIC;
						handler.sendMessage(message);
					}

			}

		});

	}

	public List<File> getmSubFile(){
		mSubFile.clear();
		File[] tmpfiles=mFile.listFiles(mFilter);
		for(int i=0;i<tmpfiles.length;i++){
			File tempFile=tmpfiles[i];
			mSubFile.add(tempFile);
		}
		Collections.sort(mSubFile, comparator);
		if(mFile.getParentFile() != null)	
			mSubFile.add(0, mFile.getParentFile());
		return  mSubFile;
	}

	private class MyFileFilter implements FileFilter{
		@Override
		public boolean accept(final File file) {
			return file.canRead();
		}

	}

	private class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case TEXT_CHANGE:
				getmSubFile();
				mAdapter.setAllUnCheckd();
				mAdapter.notifyDataSetInvalidated();
				tv.setText(mFile.getName());
				layout.setVisibility(View.GONE);
				break;

			case SELECT_PIC:
				//				mListView.setAdapter(mAdapter);
				mAdapter.setCheckd(checkId);
				mAdapter.notifyDataSetChanged();
				int size =checkId.size();
				if(size!=0){
					layout.setVisibility(View.VISIBLE);
					btn_selectOK.setText("确定("+size+")");

				}else{	
					btn_selectOK.setText("确定");
					layout.setVisibility(View.GONE);
				}

				break;
			default:
				break;
			}

		}

	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){

		case R.id.btn_selPicOK:
			mListPicPath=new ArrayList<String>();
			int size=checkId.size();
			for(int i=0;i<size;i++){
				mListPicPath.add(mAdapter.getItem(checkId.get(i)).getAbsolutePath());
			}

			Intent it=new Intent();
			it.putExtra("filePath", mListPicPath);
			setResult(Activity.RESULT_OK, it);
			finish();

			break;
		}
	}

}
