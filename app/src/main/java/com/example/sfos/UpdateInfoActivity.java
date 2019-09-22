package com.example.sfos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.sfos.Common.Common;
import com.example.sfos.Retrofit.IMyRestaurantAPI;
import com.example.sfos.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateInfoActivity extends AppCompatActivity {

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable=new CompositeDisposable();
    AlertDialog dialog;

    @BindView(R.id.edit_user_name)
    EditText edit_user_name;
    @BindView(R.id.edit_user_address)
    EditText edit_user_address;
    @BindView(R.id.button_update)
    Button button_update;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onDestroy()
    {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        ButterKnife.bind(this);

        init();
        initView();
    }

    //override back arrow

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id=item.getItemId();
        if(id==android.R.id.home)
        {

            finish();  //close this activity
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        toolbar.setTitle(getString(R.string.update_information));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        button_update.setOnClickListener(view -> {

            dialog.show();
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    compositeDisposable.add(
                            myRestaurantAPI.updateUserInfo(Common.API_KEY,
                                    account.getPhoneNumber().toString(),
                                    edit_user_name.getText().toString(),
                                    edit_user_address.getText().toString(),
                                    account.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(updateUserModel -> {

                                if(updateUserModel.isSuccess())
                                {
                                    //if user has been update,just refresh again
                                    compositeDisposable.add(
                                            myRestaurantAPI.getUser(Common.API_KEY,account.getId())
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(userModel -> {

                                                        if(userModel.isSuccess())
                                                        {
                                                            Common.currentUser=userModel.getResult().get(0);
                                                            startActivity(new Intent(UpdateInfoActivity.this,HomeActivity.class));
                                                            finish();
                                                        }
                                                        else
                                                            {
                                                                Toast.makeText(UpdateInfoActivity.this,"[GET USER RESULT]"+userModel.getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                                dialog.dismiss();
                                                            },
                                                            throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(UpdateInfoActivity.this,"[GET USER ]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();



                                                            })
                                    );

                                }
                                else
                                    {
                                        dialog.dismiss();
                                        Toast.makeText(UpdateInfoActivity.this,"[UPDATE USER API RETURN]"+updateUserModel.getMessage(),Toast.LENGTH_SHORT).show();

                                    }


                                    },
                                    throwable -> {
                                dialog.dismiss();
                                Toast.makeText(UpdateInfoActivity.this,"[UPDATE USER API]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                    })
                    );

                }

                @Override
                public void onError(AccountKitError accountKitError) {

                    Toast.makeText(UpdateInfoActivity.this,"[Account Kit Error ]"+accountKitError.getErrorType().getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        });

        if (Common.currentUser!=null&&!TextUtils.isEmpty(Common.currentUser.getName()))
              edit_user_name.setText(Common.currentUser.getName());

        if (Common.currentUser!=null&&!TextUtils.isEmpty(Common.currentUser.getAddress()))
              edit_user_address.setText(Common.currentUser.getAddress());


    }

    private void init() {

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        myRestaurantAPI= RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);



    }
}
