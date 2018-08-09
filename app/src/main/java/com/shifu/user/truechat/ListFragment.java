package com.shifu.user.truechat;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shifu.user.truechat.model.Author;
import com.shifu.user.truechat.model.Msg;
import com.shifu.user.truechat.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

public class ListFragment extends Fragment {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Integer LoadTime=0;

    // Init layout elements
    private NestedScrollView scrollView;
    private RecyclerView rv;
    private TextView currentMsg;

    // Init Rx variables & functions
    private static CompositeDisposable disposables = new CompositeDisposable();
    private static Disposable observerGet =  null;
    private static Disposable observerUid = null;
    private static Disposable observerSet = null;

    // Init instances
    private static RealmController rc=RealmController.getInstance();
    private static ApiInterface  api;
    private static RVAdapter ra;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        LoadTime++;

        //TO Test
//        Realm realm = Realm.getDefaultInstance();
//        realm.executeTransaction(trRealm -> {
//            trRealm.deleteAll();
//            Long testUnicalId = 774L;
//            Long testSecondId = 68590L;
//            String testUsername = "RAS";
//            if (trRealm.where(Author.class).findFirst() == null) {
//                trRealm.copyToRealm(new Author(testUnicalId, testSecondId, testUsername));
//            }
//        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_fragment, container, false);

        rv = view.findViewById(R.id.recycler_view);
        rv.setNestedScrollingEnabled(false);
        scrollView = view.findViewById(R.id.nested_scroll_view);

        api = ApiClient.getInstance().getApi();

        currentMsg = view.findViewById(R.id.msg);
        Button buttonSend = view.findViewById(R.id.send_button);
        buttonSend.setOnClickListener((View onClickView) -> {
            if (currentMsg.getText().length() > 0) {

                // TODO возможно, здесь стоит ставить другую кодировку для корректной записи русских символов
                //byte bytes[] = currentMsg.getText().toString().getBytes();
                //pushMsg(new String(bytes, Charset.forName("UTF-8")));
                observerSet = pushMsg(currentMsg.getText().toString());
                currentMsg.setText("");
            }
        });



        if (LoadTime == 1) {

            if (isNetworkAvailable()) {
                setTitle("соединение...");
            } else {
                setTitle("не в сети");
                return view;
            }
            LoadTime++;
            setAdapterData(rc.getDBMsgs());

            //TODO кнопка синхронизации при неудаче - сообщение о том, что сообщения не доходят!
            if (rc.getUid() == null) {
                observerUid = api.getUid()
                        .map(response -> {
                            //Log.d("getAuthor", "response" + response);
                            return response;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .map(author -> {
                            Realm realm = Realm.getDefaultInstance();
                            realm.executeTransaction(trRealm -> {
                                trRealm.where(Author.class).findAll().deleteAllFromRealm();
                                trRealm.copyToRealm(author.body());
                            });
                            return 0;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(i -> {
                            //dispose(observerUid);
                            observerGet = getData();
                            disposables.add(observerGet);
                        });
                disposables.add(observerUid);
            } else {
                //Log.d("Logged", "author:" + rc.getAuthor());
                if (observerGet == null) {
                    observerGet = getData();
                    disposables.add(observerGet);
                }
            }
        }
        return view;
    }

    void setTitle(String add){
        Log.d("setTitle: ", add);
        String newTitle = getResources().getString(R.string.app_logged, add);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(newTitle);
    }

    void setAdapterData(List<Msg> msgs){
        if (ra == null && msgs == null) {
            return;
        }
        else if (ra == null) {
            ra = new RVAdapter(getContext(), msgs);
        }
        else {
            ra.insertMsgs(msgs);
        }
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

    public Disposable getData(){
        final Long uid = rc.getUid();

        Disposable observer = Flowable.interval(1, TimeUnit.SECONDS)
                .filter(i -> {
                    //Log.d("getData", "channel: "+channelNum+" i: "+i);
                    return (i-1)%15 == 0;
                })
                .onBackpressureLatest()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .flatMap(i -> {
                    //Log.d("getData", "start");
                    return api.getUsers(ApiInterface.type, uid);
                })
                .map(Response::body)
                .observeOn(AndroidSchedulers.mainThread())
                .map(list -> {
                    List<User> out = new ArrayList <>();
                    for (User obj: list){
                        if (obj.getId() != null && !rc.existUser(obj.getId())) {
                            out.add(obj);
                        }
                        else if  (obj.getName() != null && !rc.getName(obj.getId()).equals(obj.getName())){
                            rc.updateName(obj.getId(), obj.getName());
                        }
                    }
                    return out;
                })
                .observeOn(Schedulers.computation())
                .map(users -> {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(trRealm -> trRealm.copyToRealm(users));
                    return 0;
                })
                .observeOn(Schedulers.io())
                .concatMap(i -> {
                    //Log.d("getMsgs", "start");
                    return  api.getMsgs(ApiInterface.type, uid);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::body)
                .map(list -> {
                    //Log.d("getMsgs", "received:"+list);
                    List<Msg> out = new ArrayList <>();
                    for (Msg obj: list){
                        if (!obj.getUid().equals(rc.getId()) && obj.getId() != null && !rc.existMsg(obj.getId())) {
                            //Log.d("getMsgs:", obj.toString());
                            out.add(obj);
                        }
                    }
                    return out;
                })
                .observeOn(Schedulers.computation())
                .map(msgs -> {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(trRealm -> {
                        for (Msg item : msgs){
                            Long max = (Long) trRealm.where(Msg.class).max(Msg.FIELD_ID);
                            max = (max==null)?0:max+1;
                            //Log.d("getMsgs","Load in Realm: "+item.toString());
                            trRealm.copyToRealm(item);
                            item.setId(max);
                        }
                    });
                    return msgs;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msgs -> {
                    //Log.d("getData", "end");
                    setTitle(rc.getAuthorName());
                    setAdapterData(msgs);
                    currentMsg.requestFocus();
                });

        return observer;
    }

    private Disposable pushMsg(final String text){
        final Long currentMsgId = rc.newMsgId();
        final Long uid = rc.getUid();

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        Disposable observer;
        observer = Single.just(0)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.computation())
                .map(i-> {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(realm1 -> {
                        Msg msg = realm1.createObject(Msg.class);
                        msg.setId(currentMsgId);
                        msg.setText(text);
                        msg.setDate(dateFormat.format(new Date())+" msk");
                        msg.setUid(realm1.where(Author.class).findFirst().getId());
                    });
                    return realm.copyFromRealm(realm.where(Msg.class).equalTo(Msg.FIELD_ID, currentMsgId).findFirst());
                })
                .timeout(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(msg -> {
                    //Log.d("msg", msg.toString());
                    ra.insertMsgs(Collections.singletonList(msg));
                    return msg;
                })
                .observeOn(Schedulers.io())
                .flatMap(i -> api.pushMsg(uid, RequestBody.create(MediaType.parse("text/plain"), text)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();//i -> Log.d("msg", "in Realm:"+rc.getMsg(currentMsgId)));

        return observer;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);

        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.textColor), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options:
                ((MainActivity) getActivity()).navigateTo(new OptionsFragment(), true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dispose(Disposable observer){
        if (observer != null && !observer.isDisposed()) observer.dispose();
    }

    @Override
    public void onResume(){
        super.onResume();
        this.setMenuVisibility(isNetworkAvailable());

        if (LoadTime == 2 && ra == null) {
            LoadTime++;
            List <Msg> tmp = rc.getDBMsgs();
            if (tmp != null) ra = new RVAdapter(getContext(), tmp);
        } else if (LoadTime == 2) {
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setAdapter(ra);
        }
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    public void onPause(){
        super.onPause();
        dispose(observerSet);
//        dispose(observerGet);
        dispose(observerUid);
    }
    @Override
    public void onStop(){
        super.onStop();
        this.setMenuVisibility(isNetworkAvailable());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        disposables.clear();
        RealmController.getInstance().close();
    }

}
