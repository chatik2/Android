package com.shifu.user.truechat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.shifu.user.truechat.json.JsonUser;
import com.shifu.user.truechat.json.JsonMsg;
import com.shifu.user.truechat.json.JsonUid;
import com.shifu.user.truechat.realm.Author;
import com.shifu.user.truechat.realm.Msgs;
import com.shifu.user.truechat.realm.Users;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import retrofit2.Response;

public class RealmController {

    private final static String CLASS_TAG = "RC.";
    private Realm realm;

    private static RealmController instance = null;
    public static RealmController getInstance() {
        return instance;
    }

    private Disposable observerForAdapter = null;

    RealmController(Context context) {
        Realm.init(context);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();

        realm = Realm.getInstance(config);
        instance = this;
    }


    /**
     * CREATE DATA FUNCTIONS _______________________________________________________________________
    */
    void addAuthor(final Response<JsonUid> data) {
        final String TAG = CLASS_TAG+"addAuthor";
        realm.executeTransactionAsync(realm -> {
            Author author = realm.createObject(Author.class, data.body().getUid());
            author.setUsername(data.body().getName());

            MainActivity.dispose();
            RxBus.getInstance().sendMessage(Event.NAVIGATE, ListFragment.class.getCanonicalName());
        });
    }

    void addUsers(final Response<List<JsonUser>> data) {
        final String TAG = CLASS_TAG+"addUsers";
        //Log.d(TAG, "DB load");
        realm.executeTransactionAsync(realm -> {
            for (JsonUser obj : data.body()) {
                Users item = realm.createObject(Users.class, obj.getId());
                item.setName(obj.getName());
            }
            //Log.d(TAG, "Loaded: "+realm.where(Users.class).findAll());
            ListFragment.publishProcessorMsgs.onNext(realm.where(Author.class).findFirst().getUid());
        });
    }

    void addMsgs(final Response<List<JsonMsg>> data) {
        final String TAG = CLASS_TAG+"addMsgs";
        //Log.d(TAG, "DB load");
        realm.executeTransactionAsync((Realm realm) -> {
            for (JsonMsg obj : data.body()) {
                Long i = realm.where(Msgs.class).count();
                Msgs item = realm.createObject(Msgs.class, i+Msgs.increment());
                item.setText(obj.getText());
                item.setDate(obj.getDate());
                item.setUid((long) obj.getAuthor().getId());
            }
            //Log.d(TAG, "Loaded: "+realm.where(Msgs.class).findAll().sort("date"));
            RxBus.getInstance().sendMessage(Event.UPDATE, "");
        });
    }

    private void handleError(Throwable t, String TAG){
        Log.e(TAG, "Failure: " + t.toString());
    }

    void addMsg(final String text) {
        final String TAG = CLASS_TAG+"addMsg";
        realm.executeTransactionAsync(realm -> {
            Author user = realm.where(Author.class).findFirst();
            Msgs item = realm.createObject(Msgs.class, Msgs.increment());
            item.setText(text);
            item.setUid(user.getUid());

            //TODO ставить дату сообщения. Получать с сервера? Формировать на клиенте?
            item.setDate("");

//                RestController.pushMsg(obj, h);
        });
    }


    /**
     * READ DATA FUNCTIONS _________________________________________________________________________
     */

    public <T extends RealmObject> Long getSize (Class<T> objClass) {
        if (objClass == null) return null;
        return realm.where(objClass).count();
    }

    public <T extends RealmObject> RealmResults<T> getBase(Class<T> objClass, String sortField){
        RealmResults<T> base;
        final String TAG = "RC.getBase";
        boolean sort = exist(objClass, sortField);
        if (sort) {
            base = realm.where(objClass).findAll().sort(sortField);
        } else {
            base = realm.where(objClass).findAll();
        }
        return base;
    }

    private <T extends RealmObject> boolean exist(Class<T> objClass, String checkField) {
        boolean check = false;
        if (checkField != null)
        {
            for (Field f: objClass.getDeclaredFields()) {
                if (f.getName().equals(checkField)){
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    public <T extends RealmObject> T getItem(Class<T> objClass, String field, Object value){
        if (objClass == null) return null;
        boolean has = false;
        if (field != null && value != null)
        {
            for (Field f: Msgs.class.getDeclaredFields()) {
                if (f.getName().equals(field)){
                    has = true;
                    break;
                }
            }
        }
        T item;
        if (has){
            if (value instanceof String) {
                item = realm.where(objClass).equalTo(field, (String) value).findFirst();
            } else if (value instanceof Long) {
                item = realm.where(objClass).equalTo(field, (Long) value).findFirst();
            } else if (value instanceof Integer) {
                item = realm.where(objClass).equalTo(field, (Integer) value).findFirst();
            } else if (value instanceof Boolean) {
                item = realm.where(objClass).equalTo(field, (Boolean) value).findFirst();
            } else {
                item = null;
            }
        } else {
            item = realm.where(objClass).findFirst();
        }
        return (item == null)?null:realm.copyFromRealm(item);
    }

    /**
     * UPDATE DATA FUNCTIONS _______________________________________________________________________
    */

//    /**
//     *
//     * @param obj - username, uid, idToken, refreshToken
//     * @param h - handler
//     */
//    public void changeUserName(final Bundle obj, final Handler h) {
//        final String TAG = "RC.changeUserName";
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                Author item = realm.where(Author.class).findFirst();
//                item.setName(obj.getString("username"));
//                Log.d(TAG, "Set uname: "+item.getName());
//                RealmResults<Msgs> msgs = realm.where(Msgs.class).findAll().sort("date");
//                ActivityMain.setRA(new RealmRVAdapter(msgs, item.getName()));
//                ActivityMain.getRA().notifyDataSetChanged();
////                realm.where(Author.class).findAll().deleteAllFromRealm();
////                Author item = realm.createObject(Author.class, obj.getString("uid"));
////                item.setUid(obj.getString("username"));
////                item.setIdToken(obj.getString("idToken"));
////                item.setRefreshToken(obj.getString("refreshToken"));
//
//                h.sendMessage(Message.obtain(h, 1, TAG));
//            }
//        });
//    }

//    /**
//     *
//     * @param obj - Msg: firebase_id, uuid
//     * @param h - handler
//     */
//    public void setMsgFid(final Bundle obj, final Handler h) {
//        final String TAG = "RC.setMsgFid";
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                Msgs item = realm.where(Msgs.class).equalTo(Msgs.FIELD_ID, obj.getString("uuid")).findFirst();
//                item.setFirebase_id(obj.getString("firebase_id"));
//                Log.d(TAG, "Success for:"+item.getText());
//            }
//        });
//    }

//    /**
//     *
//     * @param obj - idToken
//     * @param h - handler
//     */
//    public void changeToken(final Bundle obj, Handler h) {
//        final String TAG = "RC.changeToken";
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                Author item = realm.where(Author.class).findFirst();
//                item.setIdToken(obj.getString("idToken"));
//                Log.d(TAG, "Success for:"+item.getName());
//            }
//        });
//    }


//    /**
//     *
//     * @param obj - username, uid, idToken, refreshToken
//     * @param h
//     */
//    public void changeUser(final Bundle obj, final Handler h) {
//        final String TAG = "RC.changeUser";
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(@NotNull Realm realm) {
//                realm.where(Author.class).findAll().deleteAllFromRealm();
//                Author item = realm.createObject(Author.class, obj.getString("uid"));
//                item.setName(obj.getString("username"));
//                item.setIdToken(obj.getString("idToken"));
//                item.setRefreshToken(obj.getString("refreshToken"));
//                Log.d(TAG, "Set user: "+item.toString());
//
//                Bundle obj = new Bundle();
//                obj.putString("uid", item.getUid());
//                obj.putString("username", item.getName());
//                obj.putString("idToken", item.getIdToken());
//                obj.putString("refreshToken", item.getRefreshToken());
//
//                RestController.loadMsgs(obj, h);
//                h.sendMessage(Message.obtain(h, 1, TAG));
//            }
//        });
//    }

//    public void refreshUser(final String source, final Bundle arg, final Handler h) {
//        final String TAG = "RC.refreshUser";
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(@NotNull Realm realm) {
//                Author item = realm.where(Author.class).findFirst();
//                item.setIdToken(arg.getString("idToken"));
//                item.setRefreshToken(arg.getString("refreshToken"));
//                Log.d(TAG, "From:"+source);
//                switch (source.split("\\.")[1]){
//
//                    case "loadMsgs": loadMsgs(arg, h);
//                        break;
//                    case "delMsg": delMsg(arg, h);
//                        break;
//                    case "pushMsg": pushMsg(arg, h);
//                        break;
//                    case "pushUser": pushUser(arg, h);
//                        break;
//                    case "updateMsg": updateMsg(arg, h);
//                        break;
//                    case "updateName": updateName(arg, h);
//                        break;
//                }
//                h.sendMessage(Message.obtain(h, 1, TAG));
//            }
//        });
//    }
//
//    public void changeMsg(final String id, final String text, final Handler h) {
//        final String TAG = "RC.changeMsg";
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(@NotNull Realm realm) {
//                Msgs item = realm.where(Msgs.class).equalTo(FIELD_ID, id).findFirst();
//                Author user = realm.where(Author.class).findFirst();
//                try {
//                    item.setText(text);
//                    ActivityMain.getRA().notifyDataSetChanged();
//
//                    Bundle obj = new Bundle();
//                    obj.putString("text", text);
//                    obj.putLong("date", item.getDate());
//                    obj.putString("firebase_id", item.getFirebase_id());
//                    obj.putString("uid", user.getUid());
//                    obj.putString("idToken", user.getIdToken());
//                    obj.putString("refreshToken", user.getRefreshToken());
//
//                    //TODO если будет обработка обрывов связи, то нужно как-то проверить, существует ли это сообщение в базе
//                    if (item.getFirebase_id() != null) {
//                        RestController.updateMsg(obj, h);
//                    }
//                } catch (NullPointerException e) {
//                    h.sendMessage(Message.obtain(h, 0, TAG+":"+e.toString()));
//                }
//            }
//        });
//    }

    /**
     * DELETE DATA FUNCTIONS _______________________________________________________________________
     */

    public void clear () {
        realm.executeTransactionAsync(realm -> {
            realm.where(Msgs.class).findAll().deleteAllFromRealm();
            realm.where(Author.class).findAll().deleteAllFromRealm();
            realm.where(Users.class).findAll().deleteAllFromRealm();

        });
    }

    public <T extends RealmObject> void clear (final Class<T> objClass) {
        realm.executeTransactionAsync(realm -> {
            realm.where(objClass).findAll().deleteAllFromRealm();
            //h.sendMessage(Message.obtain(h, 2, objClass.getSimpleName()));
        });
    }

//    public <T extends RealmObject> void removeItemById(final Class<T> objClass, final String id, final Handler h) {
//            if (objClass == null) return;
//        final String TAG = "RC.removeItemById";
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(@NotNull Realm realm) {
//                try {
//                    T item = realm.where(objClass).equalTo((String)objClass.getField("FIELD_ID").get(null), id).findFirst();
//                    if (item != null) {
//                        String firebase_id=null;
//                        //TODO если буду делать удаление из таблицы юзеров - писать сюда
//                        if (item instanceof Msgs) {
//                            firebase_id = ((Msgs) item).getFirebase_id();
//                        }
//
//                        item.deleteFromRealm();
//                        ActivityMain.getRA().notifyDataSetChanged();
//
//                        //TODO если будет обработка обрывов связи, то нужно как-то проверить, существует ли это сообщение в базе
//                        if (firebase_id != null) {
//                            Author user = realm.where(Author.class).findFirst();
//                            Bundle obj = new Bundle();
//                            obj.putString("firebase_id", firebase_id);
//                            obj.putString("idToken", user.getIdToken());
//                            obj.putString("refreshToken", user.getRefreshToken());
//                            RestController.delMsg(obj, h);
//                        }
//                    }
//                } catch (NoSuchFieldException | IllegalAccessException e) {
//                    e.printStackTrace();
//                    h.sendMessage(Message.obtain(h, 0, TAG));
//                }
//            }
//        });
//    }

    public void close() {
        if (observerForAdapter != null && !observerForAdapter.isDisposed()) observerForAdapter.dispose();
        realm.close();
    }
}