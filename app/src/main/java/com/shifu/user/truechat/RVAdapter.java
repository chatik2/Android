package com.shifu.user.truechat;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shifu.user.truechat.model.Msg;

import java.util.ArrayList;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RepoViewHolder> {

    private LayoutInflater inflater;
    private List<Msg> items;

    private static RVAdapter instance;
    public static RVAdapter getInstance(){
        return instance;
    }


    RVAdapter(Context context , List<Msg> items){
        //for (Msg item: items) Log.d("RA Init: ",item.toString());
        this.items = (items == null)?new ArrayList <>():items;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        instance = this;
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
            String text = item.getText();
            viewText.setText((text==null)?"":text);
            String strDate = item.getDate();
            strDate = (strDate==null)?"":strDate.replace('T', ' ').substring(0,16)+" MSK";
            viewDate.setText(strDate);
            String strName = RealmController.getInstance().getName(item.getUid());
            viewAuthor.setText((strName == null)?"":strName);

//            if (item.getUid() != null && item.getUid().equals(RealmController.getInstance().getId())){
//                Log.d("RA", "!");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    viewAuthor.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
//                    viewDate.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
//                }
//            }
        }
    }

    public void insertMsgs(List<Msg> msgs) {
        //Log.d("RA","Insert: "+msgs);
        if (msgs != null) {
            this.items.addAll(msgs);
            notifyDataSetChanged();
        }
    }
}
