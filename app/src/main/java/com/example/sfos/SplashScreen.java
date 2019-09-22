package com.example.sfos;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.sfos.Common.Common;
import com.example.sfos.Retrofit.IMyRestaurantAPI;
import com.example.sfos.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class SplashScreen extends AppCompatActivity {

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;
    @Override
    protected void onDestroy()
    {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();


        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {



                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(Account account) {
                                //Toast.makeText(SplashScreen.this, "Already Logged", Toast.LENGTH_SHORT).show();

                                dialog.show();

                                compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY,account.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(userModel -> {

                                    if (userModel.isSuccess())     //if user available in database
                                    {
                                        Common.currentUser=userModel.getResult().get(0);
                                        Intent intent=new Intent(SplashScreen.this,HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else     //if user not available in database , start updateInformation for register
                                        {
                                            Intent intent=new Intent(SplashScreen.this,UpdateInfoActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }


                                    dialog.dismiss();

                                },





                                throwable -> {
                                    Intent fuck = new Intent(SplashScreen.this, MainActivity.class);
                                    startActivity(fuck);
                                    dialog.dismiss();
                                    Toast.makeText(SplashScreen.this,"[GET USER API]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                }));
                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {
                                Toast.makeText(SplashScreen.this, "Not Sign In! Please Sign IN", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                        Toast.makeText(SplashScreen.this, "You Must Accept this permission to use our App", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


//        new Handler().postDelayed(new  Runnable() {
//            @Override
//            public void run() {
//                startActivity(new Intent(SplashScreen.this,MainActivity.class));
//                finish();
//            }
//        },3000);
//
//    }



//    private void printKeyHash()
//    {
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
//
//            PackageManager.GET_SIGNATURES);
//
//            for (Signature signature:info.signatures)
//            {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KEY_HASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));
//            }
//
//        }
//        catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
    }

    private void init() {
        dialog=new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI= RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }
}
