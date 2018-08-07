package com.shifu.user.truechat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shifu.user.truechat.realm.Msg;
import com.shifu.user.truechat.realm.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RepoViewHolder> {

    private LayoutInflater inflater;
    private List<Msg> items;
    private Map<Long, String> users = new HashMap <>();

    private static RVAdapter instance;
    public RVAdapter getInstance(){
        return instance;
    }


    RVAdapter(Context context , List<Msg> items){
        this.items = items;
        for (User obj : RealmController.getInstance().getDBUsers()){
            users.put(obj.getId(), obj.getName());
        }
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RVAdapter.RepoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.list_msg,parent,false);
        return new RepoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVAdapter.RepoViewHolder holder, int position) {
        holder.setItemContent(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class RepoViewHolder extends RecyclerView.ViewHolder{

        TextView viewAuthor, viewText, viewDate;

        RepoViewHolder(View itemView) {
            super(itemView);
            viewAuthor = itemView.findViewById(R.id.msg_author);
            viewText = itemView.findViewById(R.id.msg_text);
            viewDate = itemView.findViewById(R.id.msg_date);
        }

        void setItemContent(Msg item){
            viewText.setText(item.getText());
            viewDate.setText(item.getDate());
            viewAuthor.setText(users.get(item.getUid()));

        }
    }

    public void insertMsgs(List<Msg> msgs) {
        this.items.addAll(msgs);
    }
}
