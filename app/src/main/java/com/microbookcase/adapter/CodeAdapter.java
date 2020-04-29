package com.microbookcase.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andea.microbook.R;
import com.microbookcase.bean.BookInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2020/4/26 13:43
 * .
 *
 * @author yj
 * @org 浙江房超信息科技有限公司
 */
public class CodeAdapter extends RecyclerView.Adapter<CodeAdapter.CodeViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<BookInfo.DataBean> dataList;

    public CodeAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.dataList = new ArrayList<>();
    }

    @NonNull
    @Override
    public CodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_code, parent, false);
        return new CodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CodeViewHolder holder, int position) {
        BookInfo.DataBean dataBean = dataList.get(position);
        holder.mCodeV.setText(dataBean.getName());
    }

    public void clear() {
        dataList.clear();
        notifyDataSetChanged();
    }

    public void add(BookInfo.DataBean dataBean) {
        dataList.add(dataBean);
        notifyDataSetChanged();
    }

    public List<BookInfo.DataBean> getAll() {
        return this.dataList;
    }

    public boolean hasContains(String code) {
        boolean hasExit = false;
        for (BookInfo.DataBean bean : this.dataList) {
            if (bean.getBarcode().equals(code)) {
                hasExit = true;
                break;
            }
        }
        return hasExit;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class CodeViewHolder extends RecyclerView.ViewHolder {
        private TextView mCodeV;

        public CodeViewHolder(View itemView) {
            super(itemView);
            mCodeV = itemView.findViewById(R.id.item_code_name);
        }
    }
}
