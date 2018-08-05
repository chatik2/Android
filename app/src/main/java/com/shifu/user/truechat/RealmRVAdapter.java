package com.shifu.user.truechat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shifu.user.truechat.realm.Msgs;
import com.shifu.user.truechat.realm.Users;

import org.jetbrains.annotations.NotNull;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

class RealmRVAdapter extends RealmRecyclerViewAdapter<Msgs, RealmRVAdapter.ViewHolder> {

    private final static String TAG = "RA";

    private static RealmRVAdapter instance = null;
    public static RealmRVAdapter getInstance(){
        return instance;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView text, author, date;

        public Msgs data;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.msg_text);
            author = itemView.findViewById(R.id.msg_author);
            date = itemView.findViewById(R.id.msg_date);
        }
    }

    RealmRVAdapter(RealmResults<Msgs> data) {
        super(data.sort("date"), true);
        setHasStableIds(true);
        instance = this;

        data.addChangeListener(msgs -> {
           RealmRVAdapter.instance.updateData(msgs);
            RealmRVAdapter.instance.notifyDataSetChanged();
            Log.d("Listener", "changed data: "+RealmRVAdapter.getInstance().getData());
        });

    }

    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_msg, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, int position) {
        final Msgs obj = getItem(position);
        viewHolder.data = obj;

        viewHolder.text.setText(obj.getText());
        viewHolder.date.setText(obj.getDate());

        final Users auth = RealmController.getInstance().getItem(Users.class, Users.FIELD_ID, obj.getUid());
        viewHolder.author.setText((auth == null)?null:auth.getName());
        }

    @Override
    public long getItemId(int index) {
        return getItem(index).getMid();
    }

    public void setData(OrderedRealmCollection<Msgs> data){
        Log.d(TAG, "setDataSize: "+data.size()+" from baseSize: "+RealmController.getInstance().getSize(Msgs.class));
        updateData(data);
        notifyDataSetChanged();
    }

}
