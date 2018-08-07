package com.shifu.user.truechat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shifu.user.truechat.json.ApiInterface;
import com.shifu.user.truechat.json.JsonMsg;
import com.shifu.user.truechat.realm.Author;
import com.shifu.user.truechat.realm.Msg;
import com.shifu.user.truechat.realm.User;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ListFragment extends Fragment {

    // Init layout elements
    private NestedScrollView scrollView;
    private RecyclerView rv;

    // Init Rx variables & functions
    private static CompositeDisposable disposables = new CompositeDisposable();
    private static Disposable observerSend;

    // Init instances
    private static RealmController rc=RealmController.getInstance();
    private static RVAdapter ra;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_fragment, container, false);

        rv = view.findViewById(R.id.recycler_view);
        rv.setNestedScrollingEnabled(false);
        scrollView = view.findViewById(R.id.nested_scroll_view);

        ra = new RVAdapter(getContext(), null);

        ApiInterface api = ApiClient.getInstance().getApi();

        TextView currentMsg = view.findViewById(R.id.msg);
        Button buttonSend = view.findViewById(R.id.send_button);
        buttonSend.setOnClickListener((View onClickView) -> {
            if (currentMsg.getText().length() > 0) {

                final Long currentMsgId = Msg.increment();
                observerSend = Flowable.just(0)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(Schedulers.computation())
                        .map(i -> {
                            Realm realm = Realm.getDefaultInstance();
                            realm.executeTransaction(trRealm -> {
                                Msg item = trRealm.createObject(Msg.class, currentMsgId);
                                item.setText(currentMsg.getText().toString());
                                item.setUid(trRealm.where(Author.class).findFirst().getUid());
                            });
                            return realm.where(Author.class).findFirst().getUid();
                        })
                        .observeOn(Schedulers.io())
                        .flatMap(uid -> {
                            Log.d("pushMsg", "work");
                            String text = "uid="+uid+"&text="+currentMsg.getText();
                            return api.pushMsg(uid, RequestBody.create(MediaType.parse("text/plain"), text));
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            List<Msg> msgs = new ArrayList <>();
                            Msg msg = rc.getMsg(currentMsgId);
                            msg.setDate(response.body().getDate());
                            msgs.add(msg);
                            ra.insertMsgs(msgs);
                            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

                        });
            }
        });

        if (isNetworkAvailable()){
            rc.clear();
            disposables.add(api.getUid()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(response -> {
                        String newTitle = getResources()
                                .getString(R.string.app_logged, response.body().getUsername());
                        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(newTitle);
                        return response;
                    })
                    .observeOn(Schedulers.computation())
                    .map(author -> {
                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(trRealm -> trRealm.copyToRealm(author.body()));
                        return author.body().getUid();
                    })
                    .observeOn(Schedulers.io())
                    .flatMap(api::getUsers)
                    .observeOn(Schedulers.computation())
                    .map(users -> {
                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(trRealm -> trRealm.copyToRealm(users.body()));
                        return realm.where(Author.class).findFirst().getUid();
                    })
                    .observeOn(Schedulers.io())
                    .concatMap(api::getMsgs)
                    .observeOn(Schedulers.computation())
                    .map(msgs -> {
                        Realm realm = Realm.getDefaultInstance();
                        realm.executeTransaction(trRealm -> {
                            for (JsonMsg item : msgs.body()){
                                Msg obj = trRealm.createObject(Msg.class, Msg.increment());
                                obj.setText(item.getText());
                                String date = item.getDate().replace('T', ' ').substring(0, 16);
                                obj.setDate(date);
                                obj.setUid(item.getAuthor().getId());
                            }
                        });
                        return realm.copyFromRealm(realm.where(Msg.class).findAll().sort("date"));
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setAdapterData));
        } else{
            setAdapterData(rc.getDBMsgs());
        }


        return view;
    }

    void setAdapterData(List<Msg> msgs){
        ra.insertMsgs(msgs);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(ra);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return  info!=null && info.isConnected();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        disposables.clear();
    }
}
