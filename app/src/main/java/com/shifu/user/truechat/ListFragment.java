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
import android.widget.Toast;

import com.shifu.user.truechat.model.Author;
import com.shifu.user.truechat.model.Msg;
import com.shifu.user.truechat.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
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


import static com.shifu.user.truechat.RealmController.getIdField;
import static com.shifu.user.truechat.MainActivity.timeout;

public class ListFragment extends Fragment {

    public static final String strDateFormat = "yyyy-MM-dd HH:mm";
    public static final DateFormat dateFormat = new SimpleDateFormat(strDateFormat, Locale.US);

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));//Europe/Moscow"));
    }//new Locale("ru"));

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
    private static RealmController rc = RealmController.getInstance();
    private static RealmRVAdapter ra = RealmRVAdapter.getInstance();
    private static ApiInterface api = ApiClient.getInstance(MainActivity.timeout).getApi();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        LoadTime++;

        //myTest
        //Log.d("Msgs:", rc.getBase(Msg.class, getIdField(Msg.class)).toString());
//        Realm realm = Realm.getDefaultInstance();
//        realm.executeTransaction(trRealm -> {
//            trRealm.deleteAll();
//            Long testUnicalId = 160L;
//            Long testSecondId = 99092L;
//            String testUsername = "BrowserUser";
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

        currentMsg = view.findViewById(R.id.msg);
        Button buttonSend = view.findViewById(R.id.send_button);
        buttonSend.setOnClickListener((View onClickView) -> {
            if (currentMsg.getText().length() > 0) {

                // TODO возможно, здесь стоит ставить другую кодировку для корректной записи русских символов
                //byte bytes[] = currentMsg.getText().toString().getBytes();
                //pushMsg(new String(bytes, Charset.forName("UTF-8")));
                observerSet = pushMsg(currentMsg.getText().toString());
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
            setAdapterData();

            if (rc.getUid() == null) {

                observerUid = Flowable.interval(1, TimeUnit.SECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .filter(i -> i%timeout == 0)

                        /*
                         * REST GET uid
                         */
                        .observeOn(Schedulers.io())
                        .flatMap(i -> api.getUid())
                        .map(response -> {
                            //Log.d("getAuthor", "response" + response);
                            return response;
                        })
                        .subscribeOn(Schedulers.io())

                        /*
                         * Realm Author
                         */
                        .observeOn(Schedulers.computation())
                        .map(author -> {
                            if (author.code() != 520) {
                                Realm realm = Realm.getDefaultInstance();
                                realm.executeTransaction(trRealm -> {
                                    trRealm.where(Author.class).findAll().deleteAllFromRealm();
                                    trRealm.copyToRealm(author.body());
                                    User user = trRealm.createObject(User.class, author.body().getSuid());
                                    user.setName(author.body().getName());
                                });
                            }
                            return author;
                        })

                        /*
                         * UI запуск функции получения данных
                         */
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(i -> {
                            if (i.code() == 520) {
                                Toast.makeText(getContext(),
                                        "Ошибка соединения: "
                                                + i.message()
                                                + "\nповтор попытки соединения через "
                                                + MainActivity.timeout
                                                + " секунд", Toast.LENGTH_LONG).show();

                            } else {
                                //dispose(observerUid);
                                observerGet = getData("web");
                                disposables.add(observerGet);
                            }
                        });
                disposables.add(observerUid);
            } else {
                //Log.d("Logged", "author:" + rc.getAuthor());
                if (observerGet == null) {
                    observerGet = getData("mobile");
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

    void setAdapterData(){
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(ra);
        //scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        rv.smoothScrollToPosition(ra.getItemCount());
        currentMsg.requestFocus();
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return  info!=null && info.isConnected();
    }

    /**
     * Основная цепочка запросов получения данных___________________________________________________
     * @return
     */
    public Disposable getData(String getUsersRequestType){
        final Long uid = rc.getUid();

        dispose(observerUid);

        /*
         * Генерация раз сигнала обновления c данным интервалом. TODO настройка интервала обновления чата
         */
        Disposable observer = Flowable.interval(1, TimeUnit.SECONDS)
                .filter(i -> {
                    //Log.d("getData", "channel: "+channelNum+" i: "+i);
                    return (i-1)%15 == 0;
                })
                .onBackpressureLatest()
                .subscribeOn(Schedulers.computation())

                /*
                 * REST GET /users
                 */
                .observeOn(Schedulers.io())
                .flatMap(i -> api.getUsers(getUsersRequestType, uid))

                /*
                 * Realm User
                 */
                .observeOn(Schedulers.computation())
                .map(users -> {
                   if (users.code() != 520) {
                       //Log.d("getMsgs", "loading users: " + users);
                       Realm realm = Realm.getDefaultInstance();
                       if (realm.where(User.class).count() == 0) {
                           realm.executeTransaction(trRealm -> trRealm.copyToRealm(users.body()));
                       } else {
                           realm.executeTransaction(trRealm -> {
                               for (User obj : users.body()) {
                                   User userIn = trRealm.where(User.class).equalTo(getIdField(User.class), obj.getSuid()).findFirst();
                                   if (obj.getSuid() != null && userIn == null) {
                                       trRealm.copyToRealm(obj);
                                   } else if (obj.getName() != null && !userIn.getName().equals(obj.getName())) {
                                       userIn.setName(obj.getName());
                                   }
                               }
                           });
                       }
                   }
                   return 0;
                })

                /*
                 * REST GET /msgs
                 */
                .observeOn(Schedulers.io())
                .concatMap(i -> api.getMsgs(ApiInterface.type, uid))

                /*
                 * Realm Msg
                 */
                .observeOn(Schedulers.computation())
                .map(msgs -> {
                    if (msgs.code() != 520) {
                        Log.d("getMsgs", "loading msgs: " + msgs);
                        Realm realm = Realm.getDefaultInstance();
                        Long casheSize = realm.where(Msg.class).count();

                        /*
                         * netmid != null - сообщение корректно записано в базе
                         * suid != null - указан пользователь - сообщение корректно записано в базе
                         * msgIn == null (netmidIn != netmId) - сообщение новое, такого в базе ещё нет
                         * свое или нет - не важно, т.к. если свои, но не дошли до бекенда (тогда newmId у нас будет null => отмечены (TODO)
                         */
                        realm.executeTransaction(trRealm -> {
                            for (Msg obj : msgs.body()) {
                                if (obj.getNetmid() != null && obj.getSuid() != null) {

                                    Msg msgIn = trRealm.where(Msg.class).equalTo(Msg.getNetIdField(), obj.getNetmid()).findFirst();
                                    if (casheSize == 0 || msgIn == null) {
                                        Long max = (Long) trRealm.where(Msg.class).max(RealmController.getIdField(Msg.class));
                                        max = (max == null) ? 0 : max + 1;
                                        obj.setUmid(max);
                                        obj.setDate(obj.getDate());
                                        trRealm.copyToRealm(obj);
                                        Log.d("getMsgs","Load in Realm: "+obj.toString());
                                    }
                                }
                            }
                        });
                    }
                    return msgs;
                })

                /*
                 * UI update
                 */
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.code() == 520) {
                        Toast.makeText(getContext(), response.message(), Toast.LENGTH_LONG).show();
                    } else {
                        setTitle(rc.getName(Author.class, null));
                        ra.notifyDataSetChanged();
                        //setAdapterData(msgs);
                        setAdapterData();
                    }
                });

        return observer;
    }

    /**
     * Цепочка запросов отправки данных_____________________________________________________________
     * @return
     */
    private Disposable pushMsg(final String text){
        final Long currentMsgId = rc.newMsgId();
        final Long uid = rc.getUid();

        if (uid == null) {
            Toast.makeText(getContext(), "Вы не зарегистрированы в чате, сообщение не будет доставлено",Toast.LENGTH_LONG).show();
            return null;
        }

        currentMsg.setText("");
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        Disposable observer;
        observer = Single.just(0)
                .subscribeOn(AndroidSchedulers.mainThread())

                /*
                 * Realm Msg new
                 */
                .observeOn(Schedulers.computation())
                .map(i-> {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(trRealm -> {
                        Msg msg = trRealm.createObject(Msg.class);
                        msg.setUmid(currentMsgId);
                        msg.setText(text);

                        // "костыльное решение"
                        // - иначе будет рассинхронизация со фронтендом из-за старого решения, как взаимодействовать с бекендом
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new Date());
                        cal.add(Calendar.HOUR_OF_DAY, 3);
                        msg.setDate(cal.getTime());

                        msg.setSuid(trRealm.where(Author.class).findFirst().getSuid());
                    });
                    return realm.copyFromRealm(realm.where(Msg.class).equalTo(RealmController.getIdField(Msg.class), currentMsgId).findFirst());
                })

                /*
                 * UI update
                 */
                .observeOn(AndroidSchedulers.mainThread())
                .map(msg -> {
                    ra.notifyDataSetChanged();
                    return msg;
                })

                /*
                 * REST POST new_msg
                 */
                .observeOn(Schedulers.io())
                .flatMap(i -> api.pushMsg(uid, RequestBody.create(MediaType.parse("text/plain"), text)))
                /*
                 * Realm Msg - update Date, NetMid.
                 */
                .observeOn(Schedulers.io())
                .subscribe(msg -> {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(trRealm -> {
                        Msg msgIn = trRealm.where(Msg.class).equalTo(getIdField(Msg.class), currentMsgId).findFirst();
                        if (msg.code() == 520) {
                            msgIn.setText("ОШИБКА СОЕДИНЕНИЯ\n Не загружено на сервер:\n"+msgIn.getText());
                        } else {
                            msgIn.setNetmid(msg.body().getNetmid());
                        }
                    });
                });

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
        setAdapterData();
        //scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
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
