package com.example.sfos;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sfos.Common.Common;
import com.example.sfos.Database.CartDataSource;
import com.example.sfos.Database.CartDatabase;
import com.example.sfos.Database.CartItem;
import com.example.sfos.Database.LocalCartDataSource;
import com.example.sfos.Model.CreateOrderModel;
import com.example.sfos.Model.EventBus.SendTotalCashEvent;
import com.example.sfos.Model.UpdateOrderModel;
import com.example.sfos.Retrofit.IMyRestaurantAPI;
import com.example.sfos.Retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PlaceOrderActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    @BindView(R.id.edt_date)
    EditText edt_date;
    @BindView(R.id.txt_total_cash)
    TextView txt_total_cash;
    @BindView(R.id.txt_user_phone)
    TextView txt_user_phone;
    @BindView(R.id.txt_user_address)
    TextView txt_user_address;
    @BindView(R.id.txt_new_address)
    TextView txt_new_address;
    @BindView(R.id.btn_add_new_address)
    Button btn_add_new_address;
    @BindView(R.id.ckb_default_address)
    CheckBox ckb_default_address;
    @BindView(R.id.rd1_cod)
    RadioButton rd1_cod;
    @BindView(R.id.rd1_online_payment)
    RadioButton rd1_online_payment;
    @BindView(R.id.btn_proceed)
    Button btn_proceed;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    IMyRestaurantAPI myRestaurantAPI;
    AlertDialog dialog;
    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable=new CompositeDisposable();

    boolean isSelectedDate=false,isAddNewAddress=false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        init();
        initView();
    }

    private void initView() {
        ButterKnife.bind(this);

        txt_user_phone.setText(Common.currentUser.getUserPhone());
        txt_user_address.setText(Common.currentUser.getAddress());

        toolbar.setTitle(getString(R.string.place_order));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_add_new_address.setOnClickListener(view -> {
            isAddNewAddress=true;
            ckb_default_address.setChecked(false);

            View layout_add_new_address= LayoutInflater.from(PlaceOrderActivity.this)
                    .inflate(R.layout.layout_add_new_address,null);

            EditText edt_new_address=(EditText)layout_add_new_address.findViewById(R.id.edt_add_new_address);
            edt_new_address.setText(txt_new_address.getText().toString());

            androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(PlaceOrderActivity.this)
                .setTitle("Add New Address")
                    .setView(layout_add_new_address)
                    .setNegativeButton("CANCEL",((dialogInterface,i) -> dialogInterface.dismiss()))
                    .setPositiveButton("ADD",((dialogInterface,i) -> txt_new_address.setText(edt_new_address.getText().toString())));

            androidx.appcompat.app.AlertDialog addNewAddressDialog=builder.create();
            addNewAddressDialog.show();

        });

        edt_date.setOnClickListener(view -> {

            Calendar now=Calendar.getInstance();

            DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(PlaceOrderActivity.this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH));

            dpd.show(getSupportFragmentManager(),"DatePickerDialog");

        });

        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSelectedDate)
                {
                    Toast.makeText(PlaceOrderActivity.this,"Please Select Date",Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    String dateString=edt_date.getText().toString();
                    DateFormat df= new SimpleDateFormat("MM/dd/yyyy");
                    try {
                        Date orderDate = df.parse(dateString);

                        //Get current date
                        Calendar calendar  = Calendar.getInstance();

                        Date currentDate = df.parse(df.format(calendar.getTime()));

                        if (!DateUtils.isToday(orderDate.getTime()))
                        {
                            if (orderDate.before(currentDate))
                            {
                                Toast.makeText(PlaceOrderActivity.this,"Please choose current date or future date",Toast.LENGTH_SHORT).show();
                                return;
                            }

                        }



                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
                if (!isAddNewAddress)
                {
                    if(!ckb_default_address.isChecked())
                    {
                        Toast.makeText(PlaceOrderActivity.this,"Please choose default Address set new address",Toast.LENGTH_SHORT).show();
                         return;
                    }
                }
                if(rd1_cod.isChecked())
                {
                    getOrderNumber(false);

                }
                else if(rd1_online_payment.isChecked())
                {
                    //Process Online Payment
                }

            }
        });

    }

    private void getOrderNumber(boolean isOnlinePayment) {

        dialog.show();
        if (!isOnlinePayment)
        {
            String address=ckb_default_address.isChecked()?txt_user_address.getText().toString():txt_new_address.getText().toString();

            compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid(),
                    Common.currentRestaurant.getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(cartItems -> {

                //Get Order Number from server
                compositeDisposable.add(
                        myRestaurantAPI.createOrder(Common.API_KEY,
                                Common.currentUser.getFbid(),
                                Common.currentUser.getUserPhone(),
                                Common.currentUser.getName(),
                                address,
                                edt_date.getText().toString(),
                                Common.currentRestaurant.getId(),
                                "NONE",
                                true,
                                Double.valueOf(txt_total_cash.getText().toString()),
                                cartItems.size())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(createOrderModel -> {
                            if (createOrderModel.isSuccess())
                            {
                                //after have order number we wil update all item of this order to order Detail
                                //first select cart items
                                compositeDisposable.add(myRestaurantAPI.updateOrder(Common.API_KEY,
                                        String.valueOf(createOrderModel.getResult().get(0).getOrderNumber()),
                                        new Gson().toJson(cartItems))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(updateOrderModel -> {

                                            if (updateOrderModel.isSucccess())
                                            {
                                                //after updating item we will clear cart and shhow msg success
                                                cartDataSource.cleanCart(Common.currentUser.getFbid(),
                                                        Common.currentRestaurant.getId())
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new SingleObserver<Integer>() {
                                                            @Override
                                                            public void onSubscribe(Disposable d) {

                                                            }

                                                            @Override
                                                            public void onSuccess(Integer integer) {
                                                                Toast.makeText(PlaceOrderActivity.this,"Order Placed",Toast.LENGTH_SHORT).show();
                                                                Intent homeActivity=new Intent(PlaceOrderActivity.this,HomeActivity.class);
                                                                homeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                startActivity(homeActivity);
                                                                finish();

                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                Toast.makeText(PlaceOrderActivity.this,"[CLEAR CART]"+e.getMessage(),Toast.LENGTH_SHORT).show();

                                                            }
                                                        });
                                            }

                                            if (dialog.isShowing())
                                                dialog.dismiss();

                                        }, throwable -> {
                                            dialog.dismiss();
                                           // Toast.makeText(this, "[UPDATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                        })
                                );
                            }

                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(this, "[CREATE ORDER]"+createOrderModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }, throwable -> {
                            dialog.dismiss();
                            Toast.makeText(this, "[CREATE ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();

                        })
                );

            }, throwable -> {
                Toast.makeText(this,"[GET ALL CART]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();

            }));
        }
    }

    private void init() {
        myRestaurantAPI= RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        cartDataSource =new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        edt_date.setText(new StringBuilder("")
        .append(monthOfYear+1)
        .append("/")
        .append(dayOfMonth)
        .append("/")
        .append(year));

    }

    //EvEnt Bus


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void setTotalCash(SendTotalCashEvent event)
    {
        txt_total_cash.setText(String.valueOf(event.getCash()));
    }
}
