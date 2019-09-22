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
import com.example.sfos.FoodListActivity;
import com.example.sfos.Interface.IOnRecyclerViewClickListener;
import com.example.sfos.Model.Category;
import com.example.sfos.Model.EventBus.FoodListEvent;
import com.example.sfos.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCategoryAdapter extends RecyclerView.Adapter<MyCategoryAdapter.MyViewHolder> {

    Context context;
    List<Category> categoryList;

    public MyCategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_category,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Picasso.get().load(categoryList.get(position).getImage()).into(holder.img_category);
        holder.txt_category.setText(categoryList.get(position).getName());

        holder.setListener(new IOnRecyclerViewClickListener() {
            @Override
            public void onclick(View view, int position) {
                //send stickypost event to food list activity
                EventBus.getDefault().postSticky(new FoodListEvent(true,categoryList.get(position)));
                context.startActivity(new Intent(context, FoodListActivity.class));
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.img_category)
        ImageView img_category;
        @BindView(R.id.txt_category)
        TextView txt_category;

        IOnRecyclerViewClickListener listener;

        public void setListener(IOnRecyclerViewClickListener listener)
        {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            unbinder= ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(this);
        }

        public void onClick(View view)
        {
            listener.onclick(view,getAdapterPosition());
        }

    }

    @Override
    public int getItemViewType(int position)
    {
        if(categoryList.size()==1)

            return Common.DEFAULT_COLUMN_COUNT;

        else
        {
            if(categoryList.size()%2==0)
                return Common.DEFAULT_COLUMN_COUNT;

            else
                return (position>1&&position==categoryList.size()-1)?Common.FULL_WIDTH_COLUMN:Common.DEFAULT_COLUMN_COUNT;
        }

    }
}
