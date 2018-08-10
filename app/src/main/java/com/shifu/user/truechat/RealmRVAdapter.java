package com.shifu.user.truechat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shifu.user.truechat.model.Msg;
import com.shifu.user.truechat.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

import static com.shifu.user.truechat.ListFragment.dateFormat;

class RealmRVAdapter extends RealmRecyclerViewAdapter<Msg, RealmRVAdapter.ViewHolder> {

    private final String TAG = "RA";

    private static RealmRVAdapter instance;
    public static RealmRVAdapter getInstance(){
        return instance;
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        TextView viewAuthor, viewText, viewDate;

        public Msg data;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAuthor = itemView.findViewById(R.id.msg_author);
            viewText = itemView.findViewById(R.id.msg_text);
            viewDate = itemView.findViewById(R.id.msg_date);

        }

        void setItemContent(Msg item){
            data = item;

            String text = item.getText();
            viewText.setText((text==null)?"":text);

            Date strDate = item.getDate();
            viewDate.setText((strDate==null)?"":dateFormat.format(strDate));
            String strName = RealmController.getInstance().getName(User.class, item.getSuid());
            viewAuthor.setText((strName == null)?"":strName);


//            if (item.getUuid() != null && item.getUuid().equals(RealmController.getInstance().getSuid())){
//                Log.d("RA", "!");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    viewAuthor.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
//                    viewDate.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
//                }
//            }
        }
    }

    RealmRVAdapter(OrderedRealmCollection<Msg> data) {
        //for (Msg item: items) Log.d("RA Init: ",item.toString());
        super(data, true);
        Log.d(TAG, "setDataSize: "+data.size()+" from baseSize: "+RealmController.getInstance().getSize(Msg.class));
        setHasStableIds(true);
        instance = this;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_msg, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, int position) {
        viewHolder.setItemContent(getItem(position));
    }

    @Override
    public long getItemId(int index) {
        //Log.d("RA.getItemId", getItem(index).toString());

        // TODO выяснить, как корректно обработать здесь NullPointerException применительно к Realm recycle view
        return getItem(index).getUmid();
    }

    public void setData(OrderedRealmCollection<Msg> data){
        Log.d(TAG, "setDataSize: "+data.size()+" from baseSize: "+RealmController.getInstance().getSize(Msg.class));
        updateData(data);
        notifyDataSetChanged();
    }

}
