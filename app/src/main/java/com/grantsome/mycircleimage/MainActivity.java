package com.grantsome.mycircleimage;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private CircleImageView mCircleImageView;

    private Bitmap bitmap;

    private CircleImageView mCicleImageView_1;

    private CircleImageView mCicleImageView_2;

    private Button mTakePhotoButton;

    private Button mGetPhotoButton;

    private Uri imageUri;

    private static final int TAKE_PHOTO = 1;

    private static final int GET_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleImageView = (CircleImageView) findViewById(R.id.civ);
        String uri = null;
        try {
            uri = "https://pic4.zhimg.com//v2-fb17ef8809e9631bbcb8608f014f9edf.jpg";
            getHttpBitmap(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCicleImageView_1 = (CircleImageView) findViewById(R.id.civ_1);
        mTakePhotoButton = (Button) findViewById(R.id.take_photo);
        mGetPhotoButton = (Button) findViewById(R.id.get_photo);
        mCicleImageView_2 = (CircleImageView) findViewById(R.id.civ_2);
        setTakePhotoButtonOnClickListener();
        setGetPhotoButtonOnClickListener();
    }

   //新建一个handler对象
   Handler handler = new Handler(){
       //复写HandlerMessage方法
       @Override
       public void handleMessage(android.os.Message msg){
           //msg.what一个int类型的传值对象
           switch (msg.what){
               case 100:
                   //把msg.object转化成bitmap
                   Bitmap bitmap = (Bitmap) msg.obj;
                   //为ImageView设置bitmap
                   mCircleImageView.setImageBitmap(bitmap);
                   break;
               case 500:
                   Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                   break;
               default:
                   break;
           }
       }
   };

   private void  getHttpBitmap(final String uri){
       //开启子线程
       new Thread(new Runnable() {
           @Override
           public void run() {
              URL mURL = null;
              Bitmap bitmap = null;
               try{
                   //用String的URL新建URL的url
                   mURL = new URL(uri);
                   //mURL.openConnection()方法返回一个URLConnection对象,转型成为HttpURLConnection
                   HttpURLConnection httpURLConnection = (HttpURLConnection) mURL.openConnection();
                   //设置连接时间
                   httpURLConnection.setConnectTimeout(5000);
                   //网络请求设置参数
                   httpURLConnection.setDoInput(true);
                   //设置使用缓存
                   httpURLConnection.setUseCaches(true);
                   //以输入流的方式读取httpConnection
                   InputStream inputStream = httpURLConnection.getInputStream();
                   //把刚刚读进来的东西用BitmapFactory.decodeStream()转化成Bitmap
                   bitmap = BitmapFactory.decodeStream(inputStream);
                   //处理器发送message;里面的message对象是handler.obtain返回的参数,该方法第一个是msg.what的int；类型,第二个是object
                   handler.sendMessage(handler.obtainMessage(100,bitmap));
                   //关闭流
                   inputStream.close();
               }catch (Exception e){
                   e.printStackTrace();
                   handler.sendMessage(handler.obtainMessage(500,"网络连接失败"));
               }
           }
       }).start();//开启子线程；
   }

    private void setTakePhotoButtonOnClickListener(){

        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT>24){
                    imageUri = FileProvider.getUriForFile(MainActivity.this,"com.grantsome.mycicleimage.provider",outputImage);
                }else {
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });
    }

    private void setGetPhotoButtonOnClickListener(){

        mGetPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                   ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
               }else {
                   openAlbum();
               }
            }
        });
    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,GET_PHOTO);
    }



    private String getImagePath(Uri uri,String selection){
        String path = null;
        Cursor curosr = getContentResolver().query(uri,null,selection,null,null);
        try {
            if(curosr.moveToFirst()){
                path = curosr.getString(curosr.getColumnIndex(MediaStore.Images.Media.DATA));
            }else {
                curosr.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return path;
    }

    private void displayImages(String imagepath){
        if(imagepath !=null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            mCicleImageView_2.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this, "获取照片失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
          switch (requestCode){
              case TAKE_PHOTO:
                  if(resultCode ==RESULT_OK) {
                      try {
                          Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                          mCicleImageView_1.setImageBitmap(bitmap);
                      } catch (Exception e) {
                          e.printStackTrace();
                      }
                  }
                  break;
              case GET_PHOTO:
                  if(resultCode == RESULT_OK){
                      if(Build.VERSION.SDK_INT>=19){
                         handleImageOnKitKat(data);
                      }else {
                          handleImageBeforeKitKat(data);
                      }
                  }
                  break;
              default:
                  break;
          }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        String TAG = "MainActivity";
        Log.d(TAG,"uri = data.getDate() = "+uri);
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            Log.d(TAG,"docId =  DocumentsContract.getDocumentId(uri) = "+docId);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                Log.d(TAG,"id =docId.split(\":\")[1] = "+id);
                String selection = MediaStore.Images.Media._ID+"="+id;
                Log.d(TAG,"selection = MediaStore.Images.Media._ID+\"=\"+id = "+selection);
                imagePath = getImagePath(uri,selection);
                Log.d(TAG," imagePath = getImagePath(uri,selection) "+ imagePath);
            }else if("com.android.providers.download.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                Log.d(TAG," contentUri = ContentUris.withAppendedId "+ contentUri);
                imagePath = getImagePath(contentUri,null);
                Log.d(TAG,"imagePath = getImagePath(contentUri,null) "+ imagePath);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);
            Log.d(TAG,"imagePath = getImagePath(uri,null) "+ imagePath);
        }else if("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
            Log.d(TAG,"imagePath = getPath() "+ imagePath);
        }
        displayImages(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImages(imagePath);
    }

}
