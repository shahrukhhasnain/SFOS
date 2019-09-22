package com.example.sfos.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sfos.Common.Common;
import com.example.sfos.Database.CartDataSource;
import com.example.sfos.Database.CartDatabase;
import com.example.sfos.Database.CartItem;
import com.example.sfos.Database.LocalCartDataSource;
import com.example.sfos.FoodDetailActivity;
import com.example.sfos.Interface.IFoodDetailOrCartClickListener;
import com.example.sfos.Model.EventBus.FoodDetailEvent;
import com.example.sfos.Model.FavoriteModel;
import com.example.sfos.Model.FavoriteOnlyId;
import com.example.sfos.Model.Food;
import com.example.sfos.R;
import com.example.sfos.Retrofit.IMyRestaurantAPI;
import com.example.sfos.Retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyFoodAdapter extends RecyclerView.Adapter<MyFoodAdapter.MyViewHolder> {

    Context context;
    List<Food> foodList;
    CompositeDisposable compositeDisposable;
    CartDataSource cartDataSource;
    IMyRestaurantAPI myRestaurantAPI;

    public void onStop()
    {
        compositeDisposable.clear();
    }


    public MyFoodAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
        compositeDisposable=new CompositeDisposable();
        cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        myRestaurantAPI= RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_food,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Picasso.get().load(foodList.get(position).getImage())
                .placeholder(R.drawable.app_icon)
                .into(holder.img_food);

        holder.txt_food_name.setText(foodList.get(position).getName());
        holder.txt_food_price.setText(new StringBuilder(context.getString(R.string.money_sign)).append(foodList.get(position).getPrice()));

        //Check favorite
        if(Common.currentFavOfRestaurant!=null&&Common.currentFavOfRestaurant.size()>0)
        {
            if(Common.checkFavorite(foodList.get(position).getId()))
            {
                holder.img_fav.setImageResource(R.drawable.ic_favorite_primary_color_24dp);
                holder.img_fav.setTag(true);
            }
            else
            {
                holder.img_fav.setImageResource(R.drawable.ic_favorite_border_primary_color_24dp);
                holder.img_fav.setTag(false);

            }

        }
        else
            {
                //Default,all item is no favorite
                holder.img_fav.setTag(false);

            }



        //Event
        holder.img_fav.setOnClickListener(view -> {
            ImageView fav=(ImageView)view;
            if((Boolean)fav.getTag())
            {
                //If tag=true->Favorite item clicked
                compositeDisposable.add(myRestaurantAPI.removeFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        foodList.get(position).getId(),
                        Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favoriteModel -> {
                    if(favoriteModel.isSuccess() && favoriteModel.getMessage().contains("SUCCESS"))
                    {
                        fav.setImageResource(R.drawable.ic_favorite_border_primary_color_24dp);
                        fav.setTag(false);
                        if(Common.currentFavOfRestaurant!=null)
                            Common.removeFavorite(foodList.get(position).getId());
                    }

                }, throwable -> {
                    //Toast.makeText(context,"[REMOVE FAV]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();

                }));
            }
            else
            {
                //If tag=true->Favorite item clicked
                compositeDisposable.add(myRestaurantAPI.insertFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        foodList.get(position).getId(),
                        Common.currentRestaurant.getId(),
                        Common.currentRestaurant.getName(),
                        foodList.get(position).getName(),
                        foodList.get(position).getImage(),
                        foodList.get(position).getPrice())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if(favoriteModel.isSuccess() && favoriteModel.getMessage().contains("SUCCESS"))
                            {
                                fav.setImageResource(R.drawable.ic_favorite_primary_color_24dp);
                                fav.setTag(true);
                                if(Common.currentFavOfRestaurant!=null)
                                    Common.currentFavOfRestaurant.add(new FavoriteOnlyId(foodList.get(position).getId()));
                            }

                        }, throwable -> {
                            Toast.makeText(context,"[ADD FAV]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();

                        }));

            }

        });

        holder.setListener((view, position1, isDetail) -> {

            if (isDetail)
            {
                context.startActivity(new Intent(context, FoodDetailActivity.class));
                EventBus.getDefault().postSticky(new FoodDetailEvent(true,foodList.get(position)));

            }
            else
                {
                    //cart create
                    CartItem cartItem=new CartItem();
                    cartItem.setFoodId(foodList.get(position).getId());
                    cartItem.setFoodName(foodList.get(position).getName());
                    cartItem.setFoodPrice(foodList.get(position).getPrice());
                    cartItem.setFoodImage(foodList.get(position).getImage());
                    cartItem.setFoodQuantity(1);
                    cartItem.setUserPhone(Common.currentUser.getUserPhone());
                    cartItem.setRestaurantId(Common.currentRestaurant.getId());
                    cartItem.setFoodAddon("NORMAL");
                    cartItem.setFoodSize("NORMAL");
                    cartItem.setFoodExtraPrice(0.0);
                    cartItem.setFbid(Common.currentUser.getFbid());

                    compositeDisposable.add(
                            cartDataSource.insertOrReplaceAll(cartItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() ->{

                                Toast.makeText(context,"Added TO Cart",Toast.LENGTH_SHORT).show();

                                    },
                                    throwable -> {
                                Toast.makeText(context,"[ADD CART]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                    })
                    );


                }

        });


    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.img_fav)
        ImageView img_fav;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_detail)
        ImageView img_detail;
        @BindView(R.id.img_cart)
        ImageView img_add_cart;

        IFoodDetailOrCartClickListener listener;

        public void setListener(IFoodDetailOrCartClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            unbinder= ButterKnife.bind(this,itemView);

            img_detail.setOnClickListener(this);
            img_add_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if(view.getId()==R.id.img_detail)
                listener.onFoodItemClickListener(view,getAdapterPosition(),true);
            else if(view.getId()==R.id.img_cart)
                listener.onFoodItemClickListener(view,getAdapterPosition(),false);

        }
    }
}
