package com.lzq.trafficdirector.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class FileScanFragment extends Fragment {
	/**
	 * 在这个选项卡中主要完成以下功能：
	 * 1.列举出SDcard下的所有文件信息，用户可以浏览所有文件夹及其子文件夹
	 * 2.长按某一选项可以删除文件夹或文件
	 */

	final String ROOTPATH=Environment.getExternalStorageDirectory().getPath();
	TextView CurrentPathView=null;
	ListView FileListView=null;
	View FileScanView=null;
	ToggleButton ServiceToggler=null;
	FileScanFragment self=this;
	ArrayList<String> FileName=null;
	ArrayList<String> FilePath=null;
	ArrayList<Map<String,Object>> FileList=null;
	SimpleAdapter ListAdapter=null;
	
	ProgressDialog loading=null;
	
	String currentPath=null;

	boolean loadover=false;
	
	
	
	
	Handler FileScanHandler = new Handler();
	/**
	 * 在这个实现了Runnable接口的类中，根据ListAdapter中保存的内容去初始化FileListView，让界面中显示出文件夹信息
	 */
	Runnable UpdateFileList =new Runnable()
	{
		public void run()
		{
			loading.dismiss();
			FileListView.setAdapter(ListAdapter);
			FileListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		

		}
	};
//	Runnable UpdateUI = new Runnable() {
//		// String path=current_path;
//		public void run() {
//			refreshMyFileList(current_path);
//		}
//	};
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		
	}
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stubreturn super.onCreateView(inflater,
		// container, savedInstanceState);
		FileScanView = inflater.inflate(R.layout.filescan_fragment_layout, null);
		initialFileList();
		refreshFileList(ROOTPATH);
		return FileScanView;
	}
	
	
	private void initialFileList()
	{

		CurrentPathView=(TextView)FileScanView.findViewById(R.id.FileScanTextView);
		FileListView=(ListView)FileScanView.findViewById(R.id.FileScanListView);
		/**
		 * 设置开启服务按钮
		 */
		ServiceToggler=(ToggleButton)FileScanView.findViewById(R.id.ToggleServiceButton);
		ServiceToggler.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
			
		});
		/**
		 ** 设置单击选项监听器
		 */
		FileListView.setOnItemClickListener(new FileItemClickedListener());	
		/**
		 * 设置长按选项监听器
		 */
		FileListView.setOnItemLongClickListener(new FileItemLongClickedListener());
	}
	
	private void refreshFileList(String path)
	{
		FileName=new ArrayList();
		FilePath=new ArrayList();
		FileList=new ArrayList();
		currentPath=path;
		CurrentPathView.setText("current path: "+path);
		loading=ProgressDialog.show(FileScanView.getContext(), "refreshing file list......", "Please waiting ......");	
		refreshThread rthread=new refreshThread(path);	
		rthread.start();
	
	}
	
	
	
	/**
	 * 用于监听FileList中有文件选项被按下时的事件的监听器，处理选项被单击时的情形
	 * @author LZQ
	 *
	 */
	class FileItemClickedListener implements OnItemClickListener
	{

		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long ID) {
			// TODO Auto-generated method stub
			
			String clickedFilePath=FilePath.get(position);
			//System.out.println("Item clicked!  Clicked file path:"+clickedFilePath);
			if(new File(clickedFilePath).isDirectory())
		     	refreshFileList(clickedFilePath);
		}
		
	}
	/**
	 * 用于监听FileList中选项被长按时的事件
	 */
	class FileItemLongClickedListener implements OnItemLongClickListener
	{

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long ID) {
			/**
			 * 长按时弹出对话框询问是否要删除文件
			 */
			// TODO Auto-generated method stub
			final String clickedFilePath=FilePath.get(position);
			
			 new AlertDialog.Builder(FileScanView.getContext()) 
             .setTitle("删除提醒").setIcon(R.drawable.android)
             .setMessage("确定删除文件 "+FileName.get(position)+" 吗？") 
             .setPositiveButton("确定", new DialogInterface.OnClickListener() 
                 { 
                     public void onClick(DialogInterface dialog,int which) 
                     { 
                    	 final ProgressDialog pd=ProgressDialog.show(FileScanView.getContext(), "删除中", "正在删除文件："+clickedFilePath);
                    	 new Thread()
                    	 {
                    		 public void run()
                    		 {
                    			 deleteFile(new File(clickedFilePath));
                    			 pd.dismiss();
                    		 }
                    	 }.start();                   	
                    	 refreshFileList(currentPath);

                     } 
                 } 
             ) 
             .setNegativeButton("取消",null) .show(); 
			return true;
		}
		
	}
	
    /**
     * 删除文件夹及其子文件
     * @param oldPath 待删除文件夹
     */
    public void deleteFile(File oldPath) {
        if (oldPath.isDirectory()) {
            File[] files = oldPath.listFiles();
            for (File file : files) {
                deleteFile(file);
            }
            oldPath.delete();
        } else {
            oldPath.delete();
        }
    }

	/**
	 * 用来执行更新FileList的一个线程类，重载了它的run方法
	 * 在run方法中，读取path目录下的所有文件信息，并将要显示的文件名和文件图标信息保存入ListAdapter中
	 * @author LZQ
	 *
	 */
	class refreshThread extends Thread
	{
		private String path;
		public refreshThread(String path)
		{
			this.path=path;
		}
		public void run()
		{

			File rootFile =new File(path);
			File[] subFiles=rootFile.listFiles();
			
			if(!path.equals(ROOTPATH))
			{
				FileName.add("...");
				FilePath.add(rootFile.getParent());
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("file_ico", R.drawable.folder_yellow);
				map.put("file_name", "...");
				FileList.add(map);			
			}
			/**
			 * 优先列出文件夹信息
			 */
			for(File file:subFiles)
			{
				if(file.isDirectory()&&!(file.getName().startsWith(".")))//排除.android_secure这样的隐藏文件
				{			
					FileName.add(file.getName());
					FilePath.add(file.getAbsolutePath());
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("file_ico", R.drawable.folder_yellow);
					map.put("file_name",file.getName());
					FileList.add(map);	
				}
			}
			/**
			 * 列出其它非文件夹信息
			 */
			for(File file:subFiles)
			{
				if(!file.isDirectory())
				{
					//System.err.println("add "+file.getName()+"  "+file.getAbsolutePath());
					FileName.add(file.getName());
					FilePath.add(file.getAbsolutePath());
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("file_ico", R.drawable.folder);
					map.put("file_name",file.getName());
					FileList.add(map);	
				}
			}
			/**
			 * 将要显示的文件信息保存在这个ListAdapter中，然后让主线程中的Handeler去完成更新UI的操作
			 */
		    ListAdapter = new SimpleAdapter(FileScanView.getContext(),
					FileList, R.layout.filelist_item, new String[] { "file_ico",
							"file_name" }, new int[] { R.id.file_ico,
							R.id.file_name });
		    //System.out.println("post");
		    FileScanHandler.post(UpdateFileList);
		}
	}
			
}
