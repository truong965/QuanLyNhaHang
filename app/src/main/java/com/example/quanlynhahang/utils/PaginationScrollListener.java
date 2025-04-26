package com.example.quanlynhahang.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationScrollListener  extends RecyclerView.OnScrollListener {
    private LinearLayoutManager layoutManager;

    public PaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        if(isLoading() || isLastPage()){
            return;
        }
        if(firstVisibleItemPosition>=0 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount ){
            loadMoreItems();
        }
    }
    public abstract void loadMoreItems();
    public abstract boolean isLoading();
    public abstract boolean isLastPage();
}
