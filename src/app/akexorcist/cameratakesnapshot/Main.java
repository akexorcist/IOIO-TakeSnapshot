package app.akexorcist.cameratakesnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class Main extends IOIOActivity implements SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback {
	AutoFocusCallback myAutoFocusCallback;
    Camera mCamera;
    Camera.Parameters params;
    SurfaceView mPreview;
    Button btnSnap, btnSize, btnScene, btnFlash, btnWB, btnFocus, btnColor, btnEV, btnGallery;
	ArrayAdapter<String> adapterDir;
	List<Camera.Size> previewSize, pictureSize;
	List<String> focusMode, colorEffect, flashMode, sceneMode, whiteBalance, size, scene, flash, wb, focus, color;
    
    int startTime = 0, MaxEV, MinEV, camerastate = 0;

    final int CAMERA_NORMAL = 0;
    final int CAMERA_SNAP = 1;
    
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("System","onCreate");
        super.onCreate(savedInstanceState);
	    getWindow().setFormat(PixelFormat.RGBA_8888);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN 
        		| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);
        
        btnSnap = (Button) findViewById(R.id.btnSnap);
        btnSnap.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Wait for camera
		    	if((int)(System.currentTimeMillis() / 1000) - startTime > 1) {
			    	Log.d("Camera","Snap");
					btnSnap.setEnabled(false);
					if(mCamera.getParameters().getFocusMode().equals("macro"))
		    	        mCamera.takePicture(Main.this, null, null, Main.this);
					else 
						mCamera.autoFocus(myAutoFocusCallback);
		    	}
			}
        });
        
        btnSize = (Button) findViewById(R.id.btnSize);
        btnSize.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Dialog dialog = new Dialog(Main.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
		        dialog.setContentView(R.layout.listviewdialog);
		        dialog.setCancelable(true);

		        LinearLayout noneLayoutDialog = (LinearLayout) dialog.findViewById(R.id.noneLayoutDialog);
		        noneLayoutDialog.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						dialog.cancel();
					}
		        });
		        TextView txtHead = (TextView) dialog.findViewById(R.id.txtHead);
		        txtHead.setText("Picture Size");
		        ListView listView = (ListView) dialog.findViewById(R.id.listView);
		        listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						params.setPictureSize(pictureSize.get(arg2).width,pictureSize.get(arg2).height);
						mCamera.setParameters(params);
				    	Log.d("PictureSize",String.valueOf(pictureSize.get(arg2).width) + " x " + String.valueOf(pictureSize.get(arg2).height));
						dialog.cancel();
					}
		        });

				adapterDir = new CustomListAdapter(Main.this ,R.layout.listviewstyle, size);
		    	listView.setAdapter(adapterDir);
		    	dialog.show();
			}
        });
        
        btnScene = (Button) findViewById(R.id.btnScene);
        btnScene.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Dialog dialog = new Dialog(Main.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
		        dialog.setContentView(R.layout.listviewdialog);
		        dialog.setCancelable(true);
		        LinearLayout noneLayoutDialog = (LinearLayout) dialog.findViewById(R.id.noneLayoutDialog);
		        noneLayoutDialog.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						dialog.cancel();
					}
		        });
		        TextView txtHead = (TextView) dialog.findViewById(R.id.txtHead);
		        txtHead.setText("Scene Mode");
		        ListView listView = (ListView) dialog.findViewById(R.id.listView);
		        listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						params.setSceneMode(sceneMode.get(arg2));
					    mCamera.setParameters(params);
				    	Log.d("sceneMode",sceneMode.get(arg2));
						dialog.cancel();
					}
		        });

				adapterDir = new CustomListAdapter(Main.this ,R.layout.listviewstyle, scene);
		    	listView.setAdapter(adapterDir);
		    	dialog.show();
			}
        });

        btnFlash = (Button) findViewById(R.id.btnFlash);
        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
	        btnFlash.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					final Dialog dialog = new Dialog(Main.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
			        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
			        dialog.setContentView(R.layout.listviewdialog);
			        dialog.setCancelable(true);
			        LinearLayout noneLayoutDialog = (LinearLayout) dialog.findViewById(R.id.noneLayoutDialog);
			        noneLayoutDialog.setOnClickListener(new OnClickListener() {
						public void onClick(View arg0) {
							dialog.cancel();
						}
			        });
			        TextView txtHead = (TextView) dialog.findViewById(R.id.txtHead);
			        txtHead.setText("Flash Mode");
			        ListView listView = (ListView) dialog.findViewById(R.id.listView);
			        listView.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							params.setFlashMode(flashMode.get(arg2));
							mCamera.setParameters(params);
					    	Log.d("flashMode",flashMode.get(arg2));
							dialog.cancel();
						}
			        });
	
					adapterDir = new CustomListAdapter(Main.this ,R.layout.listviewstyle, flash);
			    	listView.setAdapter(adapterDir);
			    	dialog.show();
				}
	        });
        } else {
        	btnFlash.getBackground().setAlpha(150);
        	btnFlash.setEnabled(false);
        }
        
        btnWB = (Button) findViewById(R.id.btnWB);
        btnWB.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Dialog dialog = new Dialog(Main.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
		        dialog.setContentView(R.layout.listviewdialog);
		        dialog.setCancelable(true);
		        LinearLayout noneLayoutDialog = (LinearLayout) dialog.findViewById(R.id.noneLayoutDialog);
		        noneLayoutDialog.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						dialog.cancel();
					}
		        });
		        TextView txtHead = (TextView) dialog.findViewById(R.id.txtHead);
		        txtHead.setText("White Balance");
		        ListView listView = (ListView) dialog.findViewById(R.id.listView);
		        listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						params.setWhiteBalance(whiteBalance.get(arg2));
						mCamera.setParameters(params);
				    	Log.d("whiteBalance",whiteBalance.get(arg2));
						dialog.cancel();
					}
		        });

				adapterDir = new CustomListAdapter(Main.this ,R.layout.listviewstyle, wb);
		    	listView.setAdapter(adapterDir);
		    	dialog.show();
			}
        });
        
        btnFocus = (Button) findViewById(R.id.btnFocus);
        btnFocus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Dialog dialog = new Dialog(Main.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
		        dialog.setContentView(R.layout.listviewdialog);
		        dialog.setCancelable(true);
		        LinearLayout noneLayoutDialog = (LinearLayout) dialog.findViewById(R.id.noneLayoutDialog);
		        noneLayoutDialog.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						dialog.cancel();
					}
		        });
		        TextView txtHead = (TextView) dialog.findViewById(R.id.txtHead);
		        txtHead.setText("Focus Mode");
		        ListView listView = (ListView) dialog.findViewById(R.id.listView);
		        listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						params.setFocusMode(focusMode.get(arg2));
					    mCamera.setParameters(params);
				    	Log.d("focusMode",focusMode.get(arg2));
						dialog.cancel();
					}
		        });

				adapterDir = new CustomListAdapter(Main.this ,R.layout.listviewstyle, focus);
		    	listView.setAdapter(adapterDir);
		    	dialog.show();
			}
        });
        
        btnColor = (Button) findViewById(R.id.btnColor);
        btnColor.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Dialog dialog = new Dialog(Main.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
		        dialog.setContentView(R.layout.listviewdialog);
		        dialog.setCancelable(true);
		        LinearLayout noneLayoutDialog = (LinearLayout) dialog.findViewById(R.id.noneLayoutDialog);
		        noneLayoutDialog.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						dialog.cancel();
					}
		        });
		        TextView txtHead = (TextView) dialog.findViewById(R.id.txtHead);
		        txtHead.setText("Color Effect");
		        ListView listView = (ListView) dialog.findViewById(R.id.listView);
		        listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						params.setColorEffect(colorEffect.get(arg2));
						mCamera.setParameters(params);
				    	Log.d("colorEffect",colorEffect.get(arg2));
						dialog.cancel();
					}
		        });

				adapterDir = new CustomListAdapter(Main.this ,R.layout.listviewstyle, color);
		    	listView.setAdapter(adapterDir);
		    	dialog.show();
			}
        });
        
        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			    Intent intent = new Intent(); 
				intent.setType("image/*");
			    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			    Main.this.startActivity(intent); 

			}
        });
        
        mPreview = (SurfaceView)findViewById(R.id.preview);
        mPreview.getHolder().addCallback(this);
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        mCamera = Camera.open();
        
    	myAutoFocusCallback = new AutoFocusCallback(){
    		public void onAutoFocus(boolean arg0, Camera arg1) { 
				Log.d("CameraAutoFocus", "Focused");
		    	startTime = (int) (System.currentTimeMillis() / 1000);
    	        mCamera.takePicture(Main.this, null, null, Main.this);
    		}
    	};
    }
    
    public void onResume() {
    	Log.d("System","onResume");
        super.onResume();
        try {
            mCamera.startPreview();
        } catch (Exception e) {
        	Log.e("CameraStartPreview",e.toString());
        }
    }
    
    public void onPause() {
    	Log.d("System","onPause");
        super.onPause();
        mCamera.stopPreview();
    }
    
    public void onDestroy() {
    	Log.d("System","onDestroy");
        super.onDestroy();
        mCamera.release();
    }

    public void onShutter() {
    	Log.d("Camera","onShutter");
    }

    public void onPictureTaken(byte[] data, Camera camera) {
    	Log.d("System","onPictureTaken");

    	startTime = (int) (System.currentTimeMillis() / 1000);
    	
    	// Save image to /sdcard/DCIM/Camera/
    	int imageNum = 0;

        File imagesFolder = new File(Environment.getExternalStorageDirectory()
        		, "DCIM/Camera");
        imagesFolder.mkdirs();
        String fileName = "IMG_" + String.valueOf(imageNum) + ".jpg";
        File output = new File(imagesFolder, fileName);
        
        while (output.exists()){
            imageNum++;
            fileName = "IMG_" + String.valueOf(imageNum) + ".jpg";
            output = new File(imagesFolder, fileName);
        }
		
		try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }

        btnSnap.setEnabled(true);
        //camera.startPreview();
        Log.d("Camera","Restart Preview");	
        mCamera.stopPreview();
	    mCamera.setParameters(params);
        mCamera.startPreview();
		camerastate = CAMERA_NORMAL;
    	
    	/*
        final Dialog dialog = new Dialog(Main.this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.simpledialog);
        dialog.setCancelable(true);
        Bitmap bmp=BitmapFactory.decodeByteArray(data,0,data.length);
        Button btnSave = (Button) dialog.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Save image to /sdcard/DCIM/Camera/
		    	int imageNum = 0;
		        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "DCIM/Camera");
		        imagesFolder.mkdirs(); // <----
		        String fileName = "IMG_" + String.valueOf(imageNum) + ".jpg";
		        File output = new File(imagesFolder, fileName);
		        while (output.exists()){
		            imageNum++;
		            fileName = "IMG_" + String.valueOf(imageNum) + ".jpg";
		            output = new File(imagesFolder, fileName);
		        }
		    	Log.i("FileName",output.toString());
		        Uri uriSavedImage = Uri.fromFile(output);
		        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
		        
		     	// Add an Image to the Media Gallery by intent
		        ContentValues image = new ContentValues();
		        String dateTaken = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		        image.put(Images.Media.TITLE, output.toString());
		        image.put(Images.Media.DISPLAY_NAME, output.toString());
		        image.put(Images.Media.DATE_ADDED, dateTaken);
		        image.put(Images.Media.DATE_TAKEN, dateTaken);
		        image.put(Images.Media.DATE_MODIFIED, dateTaken);
		        image.put(Images.Media.MIME_TYPE, "image/jpg");
		        image.put(Images.Media.ORIENTATION, 0);
		        File parent = output.getParentFile();
		        String path = parent.toString().toLowerCase();
		        String name = parent.getName().toLowerCase();
		        image.put(Images.ImageColumns.BUCKET_ID, path.hashCode());
		        image.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
		        image.put(Images.Media.SIZE, output.length());
		        image.put(Images.Media.DATA, output.getAbsolutePath());
		        Uri result = Main.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
		        
		        OutputStream imageFileOS;
		        try {
		            imageFileOS = getContentResolver().openOutputStream(uriSavedImage);
		            imageFileOS.write(data2);
		            imageFileOS.flush();
		            imageFileOS.close();
		            Toast.makeText(Main.this, fileName, Toast.LENGTH_SHORT).show();

		        } catch (FileNotFoundException e) {
		        } catch (IOException e) { }

				dialog.cancel();
		        btnSnap.setEnabled(true);
		        camera2.startPreview();
			}
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.cancel();
		        btnSnap.setEnabled(true);
		        camera2.startPreview();
			}
        });
        
        ImageView imgPreview = (ImageView) dialog.findViewById(R.id.imgPreview);
        imgPreview.setImageBitmap(bmp);
        dialog.show();
        
        */
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	Log.d("System","surfaceChanged");
    	String str;
        params = mCamera.getParameters();
        
        previewSize = params.getSupportedPreviewSizes();
        for(int i = 0 ; i < previewSize.size() ; i++) {
        	str = previewSize.get(i).width + " x " + previewSize.get(i).height;
        	Log.i("previewSize", str);
        }
        
        pictureSize = params.getSupportedPictureSizes();
        for(int i = 0 ; i < pictureSize.size() ; i++) {
        	str = pictureSize.get(i).width + " x " + pictureSize.get(i).height;
        	Log.i("pictureSize", str);
        	size.add(str);
        }
        
        focusMode = params.getSupportedFocusModes();
        for(int i = 0 ; i < focusMode.size() ; i++) {
        	str = focusMode.get(i);
        	Log.i("focusMode", str);
        	focus.add(str);
        }
    	
        if (focusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        
        colorEffect = params.getSupportedColorEffects();
        str = "";
        for(int i = 0 ; i < colorEffect.size() ; i++) {
        	str = colorEffect.get(i);
        	Log.i("colorEffect", str);
        	color.add(str);
        }
        
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            flashMode = params.getSupportedFlashModes();
    	    for(int i = 0 ; i < flashMode.size() ; i++) {
            	str = flashMode.get(i);
            	Log.i("flashMode", str);
            	flash.add(str);
    	    }
        }

        sceneMode = params.getSupportedSceneModes();
        for(int i = 0 ; i < sceneMode.size() ; i++) {
        	str = sceneMode.get(i);
        	Log.i("sceneMode", str);
        	scene.add(str);
        }
        
        whiteBalance = params.getSupportedWhiteBalance();
        for(int i = 0 ; i < whiteBalance.size() ; i++) {
        	str = whiteBalance.get(i);
        	Log.i("whiteBalance", str);
        	wb.add(str);
        }
        
        if(previewSize.get(previewSize.size() - 1).width > previewSize.get(0).width) {
            params.setPreviewSize(previewSize.get(previewSize.size() - 1).width,previewSize.get(previewSize.size() - 1).height);
        } else if(previewSize.get(previewSize.size() - 1).width < previewSize.get(0).width) {
            params.setPreviewSize(previewSize.get(0).width,previewSize.get(0).height);
        }
        
        if(pictureSize.get(pictureSize.size() - 1).width > pictureSize.get(0).width) {
            params.setPictureSize(pictureSize.get(pictureSize.size() - 1).width,pictureSize.get(pictureSize.size() - 1).height);
        } else if(pictureSize.get(pictureSize.size() - 1).width < pictureSize.get(0).width) {
            params.setPictureSize(pictureSize.get(0).width,pictureSize.get(0).height);
        }
        
        Log.i("PreviewSize", String.valueOf(params.getPreviewSize().width + "x" + params.getPreviewSize().height));
        Log.i("PictureSize", String.valueOf(params.getPictureSize().width + "x" + params.getPictureSize().height));
    	
        params.setJpegQuality(100);
        mCamera.setParameters(params);
        mCamera.startPreview();
        
		Log.d("Camera","Restart Preview");	
        mCamera.stopPreview();
	    mCamera.setParameters(params);
        mCamera.startPreview();
        /*
        params.setExposureCompensation(70);*/
    	
    	
        
        
        MaxEV = params.getMaxExposureCompensation();
        MinEV = params.getMinExposureCompensation();
        Log.i("MaxExposure", String.valueOf(params.getMaxExposureCompensation()));
        Log.i("MinExposure", String.valueOf(params.getMinExposureCompensation()));
        
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	Log.d("System","surfaceCreated");
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            size = new ArrayList<String>();
            scene = new ArrayList<String>();
            flash = new ArrayList<String>();
            wb = new ArrayList<String>();
            focus = new ArrayList<String>();
            color = new ArrayList<String>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.d("System","surfaceDestroyed");
    }

	private class CustomListAdapter extends ArrayAdapter {
	    private Context mContext;
	    private int id;
	    private List <String>items ;

	    public CustomListAdapter(Context context, int textViewResourceId , List<String> list ){
	        super(context, textViewResourceId, list);           
	        mContext = context;
	        id = textViewResourceId;
	        items = list ;
	    }

	    public View getView(int position, View v, ViewGroup parent){
	        View mView = v ;
	        if(mView == null){
	            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            mView = vi.inflate(id, null);
	        }
	        TextView text = (TextView) mView.findViewById(R.id.text1);

	        if(items.get(position) != null ){
	            text.setText(items.get(position));
	        }
	        return mView;
	    }
	}
	
	class Looper extends BaseIOIOLooper {
		private DigitalInput left, right, snap;
		private PwmOutput pwm;
		private int pw = 1500;
		
		protected void setup() throws ConnectionLostException {
			left = ioio_.openDigitalInput(4);
			right = ioio_.openDigitalInput(5);
			snap = ioio_.openDigitalInput(6);
			pwm = ioio_.openPwmOutput(45, 50);
			pwm.setPulseWidth(pw);
			
			runOnUiThread(new Runnable() {
				public void run() {
					// When device connected with ioio board 
					// Toast will show "Connected!"
					Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
				}		
			});
		}
		
		public void loop() throws ConnectionLostException {
			pwm.setPulseWidth(pw);
			runOnUiThread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(20);
						if((int)(System.currentTimeMillis() / 1000) - startTime > 1 && camerastate == CAMERA_NORMAL && snap.read() == false) {
							while(!snap.read());
							camerastate = CAMERA_SNAP;
							Log.d("CameraIOIO","Snap");
							btnSnap.setEnabled(false);
							
							if(mCamera.getParameters().getFocusMode().equals("macro")) {
							    mCamera.takePicture(Main.this, null, null, Main.this);
							} else {
								mCamera.autoFocus(myAutoFocusCallback);
							}
						} else if(!right.read()) {
							Log.e("RIGHT","Press Right");
							pw += 20;
							if(pw > 2000) {
								pw = 2000;
							}
							Log.e("CameraIOIO","PW = " + String.valueOf(pw));
						} else if(!left.read()) {
							Log.e("LEFT","Press Left");
							pw -= 20;
							if(pw < 1000) {
								pw = 1000;
							}
							Log.e("CameraIOIO","PW = " + String.valueOf(pw));
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ConnectionLostException e) {
						e.printStackTrace();
					}
				}			
			});

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }
}