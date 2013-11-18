package com.lzq.sharevideolocaltest;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity implements SurfaceHolder.Callback{
	private SurfaceView mSurface;
	private SurfaceHolder mSurfaceHolder;
	private LibVLC mLibVLC;
	private EventManager mEventManger;
	private boolean mIsPlaying;
	private int mVideoHeight;  
	private int mVideoWidth;  
	private int mSarNum;
	private int mSarDen;
	private int mSurfaceAlign;
	private static final int SURFACE_SIZE = 3;  	      
	private static final int SURFACE_BEST_FIT = 0;  
	private static final int SURFACE_FIT_HORIZONTAL = 1;  
	private static final int SURFACE_FIT_VERTICAL = 2;  
	private static final int SURFACE_FILL = 3;  
	private static final int SURFACE_16_9 = 4;  
	private static final int SURFACE_4_3 = 5;  
	private static final int SURFACE_ORIGINAL = 6;  
	private int mCurrentSize = SURFACE_BEST_FIT; 
	private static final String uri = "rtsp://217.146.95.166:554/live/ch6bqvga.3gp";
	private static final String TAG = "DTV";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurface = (SurfaceView) findViewById(R.id.surface);
        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurface.setKeepScreenOn(true);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int pitch;
        String chroma = pref.getString("chroma_format", "");
        if(Util.isGingerbreadOrLater() && chroma.equals("YV12")) {
            mSurfaceHolder.setFormat(ImageFormat.YV12);
            pitch = ImageFormat.getBitsPerPixel(ImageFormat.YV12) / 8;
        } else if (chroma.equals("RV16")) {
            mSurfaceHolder.setFormat(PixelFormat.RGB_565);
            PixelFormat info = new PixelFormat();
            PixelFormat.getPixelFormatInfo(PixelFormat.RGB_565, info);
            pitch = info.bytesPerPixel;
        } else {
            mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
            PixelFormat info = new PixelFormat();
            PixelFormat.getPixelFormatInfo(PixelFormat.RGBX_8888, info);
            pitch = info.bytesPerPixel;
        }
        mSurfaceAlign = 16 / pitch - 1;
        enableIOMX(true);
        try {
        	
			mLibVLC = LibVLC.getInstance();		
		} catch (LibVlcException e) {
			Log.i(TAG, "LibVLC.getInstance() error:"+e.toString());
			e.printStackTrace();
			return ;
		}
        mEventManger = EventManager.getInstance();
        mEventManger.addHandler(mEventHandler);
    }
    
    private void enableIOMX(boolean enableIomx){
    	SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(VLCApplication.getAppContext());
    	Editor e = p.edit();
    	e.putBoolean("enable_iomx", enableIomx);
    	LibVLC.restart();
    }
    private DtvCallbackTask mDtvCallbackTask = new DtvCallbackTask(this) {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int n = 25;
			while((n-- != 0)&& !mIsPlaying){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(!mIsPlaying){
				Log.i(TAG, "could not open media or internet not access");
			}
			
		}
	};
    private final VideoEventHandler mEventHandler = new VideoEventHandler(this);
    private class VideoEventHandler extends WeakHandler<MainActivity>{
    	public VideoEventHandler(MainActivity owner) {
            super(owner);
        }
    	@Override
    	public void handleMessage(Message msg) {
    		MainActivity activity = getOwner();    		
    		 if(activity == null) return;
    		 switch (msg.getData().getInt("event")) {
             case EventManager.MediaPlayerPlaying:
                 Log.i(TAG, "MediaPlayerPlaying");
                 mIsPlaying = true;
                 break;
             case EventManager.MediaPlayerPaused:
                 Log.i(TAG, "MediaPlayerPaused");
                 mIsPlaying = false;
                 break;
             case EventManager.MediaPlayerStopped:
                 Log.i(TAG, "MediaPlayerStopped");
                 mIsPlaying = false;
                 break;
             case EventManager.MediaPlayerEndReached:
                 Log.i(TAG, "MediaPlayerEndReached");
                 break;
             case EventManager.MediaPlayerVout:
                 break;
             case EventManager.MediaPlayerPositionChanged:
                 //don't spam the logs
                 break;
             default:
                 Log.e(TAG, String.format("Event not handled (0x%x)", msg.getData().getInt("event")));
                 break;
         }
    		super.handleMessage(msg);
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {	          
		mLibVLC.attachSurface(mSurfaceHolder.getSurface(), MainActivity.this,width,height);
		Log.i(TAG, " width="+ width+" height="+height);
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stu
		
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mLibVLC.detachSurface();
		
	}
	 public void setSurfaceSize(int width, int height, int sar_num, int sar_den) {  
		 if (width * height == 0)
	            return;
	        // store video size
	        mVideoHeight = height;
	        mVideoWidth = width;
	        mSarNum = sar_num;
	        mSarDen = sar_den;
	        Message msg = mHandler.obtainMessage(SURFACE_SIZE);
	        mHandler.sendMessage(msg);
	    }  
	  
	    private final Handler mHandler = new VideoPlayerHandler(this);  
	  
	    private static class VideoPlayerHandler extends WeakHandler<MainActivity> {  
	        public VideoPlayerHandler(MainActivity owner) {  
	            super(owner);  
	        }  
	  
	        @Override  
	        public void handleMessage(Message msg) {  
	        	MainActivity activity = getOwner();  
	            if(activity == null) // WeakReference could be GC'ed early  
	                return;  
	  
	            switch (msg.what) {  
	                case SURFACE_SIZE:  
	                    activity.changeSurfaceSize();  
	                    break;  
	            }  
	        }  
	    };  
	      @Override
	    protected void onResume() { 
	    	super.onResume();
	    	if(mLibVLC != null){
	    		try{
	    		mLibVLC.readMedia(uri, false);
	    		}catch(Exception e){
	    			Log.i(TAG,e.toString());
	    			return;
	    		}
	    		mDtvCallbackTask.execute();
			}else {
				return;
			}
	    	
	    }
	      
	      private void changeSurfaceSize() {
	          // get screen size
	          int dw = getWindow().getDecorView().getWidth();
	          int dh = getWindow().getDecorView().getHeight();

	          // getWindow().getDecorView() doesn't always take orientation into account, we have to correct the values
	          boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	          if (dw > dh && isPortrait || dw < dh && !isPortrait) {
	              int d = dw;
	              dw = dh;
	              dh = d;
	          }

	          // sanity check
	          if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
	              Log.e(TAG, "Invalid surface size");
	              return;
	          }

	          // compute the aspect ratio
	          double ar, vw;
	          double density = (double)mSarNum / (double)mSarDen;
	          if (density == 1.0) {
	              /* No indication about the density, assuming 1:1 */
	              vw = mVideoWidth;
	              ar = (double)mVideoWidth / (double)mVideoHeight;
	          } else {
	              /* Use the specified aspect ratio */
	              vw = mVideoWidth * density;
	              ar = vw / mVideoHeight;
	          }

	          // compute the display aspect ratio
	          double dar = (double) dw / (double) dh;

	          switch (mCurrentSize) {
	              case SURFACE_BEST_FIT:
	                  if (dar < ar)
	                      dh = (int) (dw / ar);
	                  else
	                      dw = (int) (dh * ar);
	                  break;
	              case SURFACE_FIT_HORIZONTAL:
	                  dh = (int) (dw / ar);
	                  break;
	              case SURFACE_FIT_VERTICAL:
	                  dw = (int) (dh * ar);
	                  break;
	              case SURFACE_FILL:
	                  break;
	              case SURFACE_16_9:
	                  ar = 16.0 / 9.0;
	                  if (dar < ar)
	                      dh = (int) (dw / ar);
	                  else
	                      dw = (int) (dh * ar);
	                  break;
	              case SURFACE_4_3:
	                  ar = 4.0 / 3.0;
	                  if (dar < ar)
	                      dh = (int) (dw / ar);
	                  else
	                      dw = (int) (dh * ar);
	                  break;
	              case SURFACE_ORIGINAL:
	                  dh = mVideoHeight;
	                  dw = (int) vw;
	                  break;
	          }

	          // align width on 16bytes
	          int alignedWidth = (mVideoWidth + mSurfaceAlign) & ~mSurfaceAlign;

	          // force surface buffer size
	          mSurfaceHolder.setFixedSize(alignedWidth, mVideoHeight);

	          // set display size
	          LayoutParams lp = mSurface.getLayoutParams();
	          lp.width = dw * alignedWidth / mVideoWidth;
	          lp.height = dh;
	          mSurface.setLayoutParams(lp);
	          mSurface.invalidate();
	      }
	    @Override
	    protected void onDestroy() {
	    	if(mLibVLC.isPlaying()){
	    		mLibVLC.stop();
	    	}
	    	mLibVLC = null;
	    	super.onDestroy();
	    }
}