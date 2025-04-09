package com.example.quanlynhahang.ManageState;

import android.content.Context;
import android.widget.Toast;


import com.example.quanlynhahang.Entity.Food;
import com.example.quanlynhahang.Ultil.CustomToast;

import java.util.ArrayList;


public class ManagmentCart {
    private Context context;
    private TinyDB tinyDB;
    private CustomToast customToast;

    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB=new TinyDB(context);
        customToast = new CustomToast(context);
    }

    public void insertFood(Food item) {
        ArrayList<Food> listpop = getListCart();
        boolean existAlready = false;
        int n = 0;
        for (int i = 0; i < listpop.size(); i++) {
            if (listpop.get(i).getId() == item.getId()) {
                existAlready = true;
                n = i;
                break;
            }
        }
        if(existAlready){
            listpop.get(n).setNumberInCart( listpop.get(n).getNumberInCart()+ item.getNumberInCart());
        }else{
            item.setNumberInCart(1);
            listpop.add(item);
        }
        tinyDB.putListObject("CartList",listpop);
        customToast.showToast("Thêm vào giỏ hàng thành công", true);
    }

    public ArrayList<Food> getListCart() {
        return tinyDB.getListObject("CartList");
    }

    public Double getTotalFee(){
        ArrayList<Food> listItem=getListCart();
        double fee=0;
        for (int i = 0; i < listItem.size(); i++) {
            fee=fee+(listItem.get(i).getPrice()*listItem.get(i).getNumberInCart());
        }
        return fee;
    }
    public void minusNumberItem(ArrayList<Food> listItem,int position,ChangeNumberItemsListener changeNumberItemsListener){
        if(listItem.get(position).getNumberInCart()==1){
            listItem.remove(position);
        }else{
            listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart()-1);
        }
        tinyDB.putListObject("CartList",listItem);
        changeNumberItemsListener.change();
    }
    public  void plusNumberItem(ArrayList<Food> listItem,int position,ChangeNumberItemsListener changeNumberItemsListener){

        listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart()+1);
        tinyDB.putListObject("CartList",listItem);
        changeNumberItemsListener.change();
    }
    public void removeItem(ArrayList<Food> listItem,int position,ChangeNumberItemsListener changeNumberItemsListener){
        listItem.remove(position);
        tinyDB.putListObject("CartList",listItem);
        changeNumberItemsListener.change();
        customToast.showToast("Xóa thành công", true);
    }
    public void clearCart(){
        tinyDB.putListObject("CartList",new ArrayList<>());
    }
}
