package com.example.quanlynhahang.nhap;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlynhahang.R;
import com.example.quanlynhahang.Ultil.PaginationScrollListener;

import java.util.ArrayList;

public class exScroll extends AppCompatActivity {
    private  boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 1;
    private int totalPage = 10;
    private ArrayList<String> data;
    private ProgressBar progressBar;
    private adapter adp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ex_scroll);
        init();
    }
    private ArrayList<String> getList(){
        ArrayList<String> data1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data1.add( "Item ");
        }
        return data1;
    }
    private void init(){
        data = new ArrayList<>();
        data.addAll(getList());
         adp = new adapter(data);
        RecyclerView listView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adp);
        progressBar = findViewById(R.id.progressBar);
        listView.addOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            public void loadMoreItems() {
                isLoading = true;
                progressBar.setVisibility(View.VISIBLE);
               loadNextPage();
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }
        });
    }
    private void loadNextPage(){
        // Simulate a delay for loading more items
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isLoading = false;
                if (currentPage < totalPage) {
                    currentPage++;
                    ArrayList<String> newData = getList();
                    adp.setData(newData);
                } else {
                    isLastPage = true;
                }
                progressBar.setVisibility(View.GONE);
            }
        }, 2000);
    }
//    private List<String> getData() {
//        List<String> data = new ArrayList<>();
//        data =
//        return data;
//    }
}