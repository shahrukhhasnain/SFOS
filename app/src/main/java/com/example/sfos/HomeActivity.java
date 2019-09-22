package com.example.sfos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.sfos.Adapter.MyRestaurantAdapter;
import com.example.sfos.Adapter.RestaurantSliderAdapter;
import com.example.sfos.Common.Common;
import com.example.sfos.Model.EventBus.RestaurantLoadEvent;
import com.example.sfos.Model.Restaurant;
import com.example.sfos.Retrofit.IMyRestaurantAPI;
import com.example.sfos.Retrofit.RetrofitClient;
import com.example.sfos.Services.PicassoImageLoadingService;
import com.facebook.accountkit.AccountKit;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ss.com.bannerslider.Slider;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView txt_user_name,txt_user_phone;

    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_restaurant)
    RecyclerView recycler_restaurant;

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    android.app.AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View headerView=navigationView.getHeaderView(0);
//        txt_user_name = (TextView)headerView.findViewById(R.id.txt_user_name);
//        txt_user_phone = (TextView)headerView.findViewById(R.id.txt_user_phone);

//        txt_user_name.setText(Common.currentUser.getName());
//        txt_user_phone.setText(Common.currentUser.getUserPhone());

        init();
        initView();
        loadRestaurant();
    }

    private void loadRestaurant() {
        dialog.show();

                compositeDisposable.add(
                        myRestaurantAPI.getRestaurant(Common.API_KEY)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(restaurantModel -> {

                                         // here we will use EventBus to send local event set adapter and slider
                                            EventBus.getDefault().post(new RestaurantLoadEvent(true,restaurantModel.getResult()));



                                        },
                                        throwable -> {
                                            EventBus.getDefault().post(new RestaurantLoadEvent(false,throwable.getMessage()) );


                                        })
                );





    }

    private void initView() {

        ButterKnife.bind(this);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recycler_restaurant.setLayoutManager(layoutManager);
        recycler_restaurant.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
    }

    private void init() {
        dialog=new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI= RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

        Slider.init(new PicassoImageLoadingService());

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_log_out) {
            // Handle the camera action
            signOut();
        }
        else if(id==R.id.nav_nearby)
        {

        }
        else if(id==R.id.nav_order_history)
        {
            startActivity(new Intent(HomeActivity.this,ViewOrderActivity.class));

        }
        else if(id==R.id.nav_update_info)
        {
            startActivity(new Intent(HomeActivity.this,ViewOrderActivity.class));

        }
        else if(id==R.id.nav_fav)
        {
            startActivity(new Intent(HomeActivity.this,FavoriteActivity.class));
        }
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_tools) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        //here we will made start.dialog to confirm
        AlertDialog confirmDialog=new AlertDialog.Builder(this)
                .setTitle("Sign out")
                .setMessage("Do you really want to sign out")
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    Common.currentUser=null;
                    Common.currentRestaurant=null;

                    AccountKit.logOut();
                    Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //clear a;; previous activity
                    startActivity(intent);
                    finish();
                }).create();

        confirmDialog.show();
    }

    /*Register EVENT BUS*/

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    //Listen EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void processRestaurantLoadEvent(RestaurantLoadEvent event)
    {
        if(event.isSuccess())
        {
//            displayBanner(event.getRestaurantList());
            displayRestaurant(event.getRestaurantList());


        }
        else
            {
Toast.makeText(this,"[RESTAURANT LOAD]"+event.getMessage(),Toast.LENGTH_SHORT).show();
            }
        dialog.dismiss();
    }

    private void displayRestaurant(List<Restaurant> restaurantList) {
        MyRestaurantAdapter adapter=new MyRestaurantAdapter(this,restaurantList);
    }

    private void displayBanner(List<Restaurant> restaurantList)
    {
        banner_slider.setAdapter(new RestaurantSliderAdapter(restaurantList));
    }
}
