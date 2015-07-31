package com.skytech.chatim.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chatuidemo.R;
import com.skytech.chatim.sky.vo.QueryUser;
import com.squareup.picasso.Picasso;

public class UserListAdapter extends BaseAdapter {
    private static final String TAG = UserListAdapter.class.getSimpleName();
    private ArrayList<QueryUser> dataList;
    private Activity activity;
    private LayoutInflater inflater;

    public UserListAdapter(Activity activity, ArrayList<QueryUser> dataList) {
        this.inflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sky_item_query_user, null);
            viewHolder = createHolder(convertView, dataList.get(position));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        bingHolder(viewHolder, dataList.get(position));
        viewHolder.index = position;
        return convertView;
    }

    private void bingHolder(ViewHolder viewHolder, QueryUser data) {
        viewHolder.tv_userName.setText(data.getUid());
        viewHolder.tv_nick.setText(data.getNickName());
        Picasso.with(activity).load(data.getAvatar())
                .placeholder(R.drawable.default_avatar).into(viewHolder.avatar);
        viewHolder.dataRow = data;

    }

    private ViewHolder createHolder(View convertView, QueryUser data) {
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.dataRow = data;
        viewHolder.tv_userName = (TextView) convertView
                .findViewById(R.id.userName);
        viewHolder.tv_nick = (TextView) convertView.findViewById(R.id.nickName);
        viewHolder.addButton = (Button) convertView.findViewById(R.id.add);
        viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.add:
                    ((UserListActivity) activity).addUser(activity,
                            viewHolder.dataRow.getUid());
                    break;

                default:
                    break;
                }

            }

        };
        viewHolder.addButton.setOnClickListener(l);
        return viewHolder;
    }

    public static class ViewHolder {
        int index;
        QueryUser dataRow;
        Button addButton;
        TextView tv_userName;
        TextView tv_nick;
        ImageView avatar;
    }

}
