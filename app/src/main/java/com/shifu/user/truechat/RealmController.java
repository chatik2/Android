package com.shifu.user.truechat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shifu.user.truechat.model.Author;
import com.shifu.user.truechat.model.Msg;
import com.shifu.user.truechat.model.MyRealms;
import com.shifu.user.truechat.model.User;
import com.shifu.user.truechat.model.UserFields;

import java.lang.reflect.Field;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class RealmController {

    private Realm realm;

    private static RealmController instance = null;
    static RealmController getInstance() {
        return instance;
    }

    RealmController(Context context){
        if (instance == null) {
            Realm.init(context);
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();

            Realm.setDefaultConfiguration(config);
            realm = Realm.getDefaultInstance();
            Long size = (Long) realm.where(Msg.class).max(getIdField(Msg.class));
            Log.d("Init max: ", (size==null)?"null":Long.toString(size));
            instance = this;
        }
    }

    /*
     * Create data functions
     */
    public Long newMsgId() {
        Long max = (Long) realm.where(Msg.class).max(getIdField(Msg.class));
        return (max == null)?0:max +1;
    }

    //
//    public void newMsg(final String text){
//        realm.executeTransactionAsync(realm -> {
//            Long out;
//            do {
//                Random r = new Random();
//                out = r.nextLong() + r.nextLong();
//            } while (realm.where(Msg.class).equalTo(Msg.FIELD_ID, out).findFirst() != null);
//            Msg msg = realm.createObject(Msg.class);
//            msg.setUmid(out);
//            msg.setText(text);
//            msg.setSuid(realm.where(Author.class).findFirst().getUuid());
//        });
//    }

    /*
     * Read data functions
     */

    public <T extends RealmObject> Long getSize (Class<T> objClass) {
        if (objClass == null) return null;
        return realm.where(objClass).count();
    }

    // Нужна ли copyFromRealm, или, если просто копируем поля, то это избыточно? Пока оставляю
    public<T extends RealmModel & UserFields & MyRealms> String getName(Class<T> objClass, Long id){
        T item;
        String out;
        if (objClass != null && objClass.equals(User.class) && id != null) {
            item = realm.where(objClass).equalTo(getIdField(objClass), id).findFirst();
        } else if (objClass != null && objClass.equals(Author.class)) {
            item = realm.where(objClass).findFirst();
        } else {
            //TODO вероятно, подобную ситуацию надо обрабатывать как custom exception, но пока так
            //В текущей версии программы подобная ситуация, полагаю, невозможна
            item = null;
        }
        if (item == null) return null;
        out = realm.copyFromRealm(item).getName();

        // Если id == null, то либо запрашивается AuthorName - не требуется обрамление, либо уже был return
        if (id != null && id.equals(getId())) out = "Вы (" + out + ")";

        return out;
    }

    public Long getId(){
        Author out = realm.where(Author.class).findFirst();
        return (out == null)?null:realm.copyFromRealm(out).getSuid();
    }

    public Long getUid(){
        Author out = realm.where(Author.class).findFirst();
        return (out == null)?null:realm.copyFromRealm(out).getUuid();
    }

    public<T extends RealmModel & MyRealms> Boolean exist(Class<T> objClass, Long id){
        if (objClass.equals(User.class) || objClass.equals(Msg.class)) {
            return (realm.where(objClass).equalTo(getIdField(objClass), id).findFirst() != null);
        } else if (objClass.equals(Author.class)) {
            return (realm.where(Author.class).findFirst() != null);
        }
        //если такого класса не существует, то точно и такого номера нет в базе :)
        //в текущей версии, видимо, ситуация невозможна
        return false;
    }

    public <T extends RealmObject> RealmResults<T> getBase(Class<T> objClass, String sortField){
        RealmResults<T> base;

        boolean sort = exist(objClass, sortField);
        if (sort){
            base = realm.where(objClass).sort(sortField).findAll();
        } else {
            base = realm.where(objClass).findAll();
        }

        // Не потокобезопасно! Realms не передаёт свои объекты в другие потоки
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


    // to execute same exceptions in the one place
    public static <T extends RealmModel & MyRealms> String getIdField(Class<T> objClass) {
        String out;
        try {
            out = objClass.newInstance().getIdField();
            return out;
        }catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        // In Exception case - there isn't such field
        return "";
    }

    // getName частично дублирует функциональность, но, возможно, так удобнее?
    public<T extends RealmModel & MyRealms> T getItem(Class<T> objClass, Long id) {
        T out;
        if (objClass != null && objClass.equals(Author.class)) {
            out = realm.where(objClass).findFirst();
        }
        else if (objClass != null && (objClass.equals(Msg.class) || objClass.equals(User.class)) && id != null) {
            out = realm.where(objClass).equalTo(getIdField(objClass), id).findFirst();
        }
        else {
            out = null;
        }
        return (out == null) ? null : realm.copyFromRealm(out);
    }

    /*
     * Update data functions
     */

    public void updateAuthorName(final String name) {
        if (name != null) {
            realm.executeTransactionAsync(realm -> {
                Author author = realm.where(Author.class).findFirst();
                if (author != null) {
                    author.setName(name);
                    User user = realm.where(User.class).equalTo(getIdField(User.class), author.getSuid()).findFirst();
                    user.setName(name);
                }
            });
        }
    }

    //    public void updateDate(final Long mid, final String date) {
//        realm.executeTransactionAsync(realm -> {
//            Msg msg = realm.where(Msg.class).equalTo(Msg.FIELD_ID, mid).findFirst();
//            if (msg != null) msg.setDate(date);
//        });
//    }

    /*
     * Delete data functions
     */
    public void clear() {
        realm.executeTransactionAsync(realm -> {
            realm.deleteAll();
        });
    }

    public<T extends RealmObject> void clear(Class<T> objClass) {
        if (objClass != null) {
            realm.executeTransactionAsync(realm -> realm.where(objClass).findAll().deleteAllFromRealm());
        }
    }

    public void close() {
        if (realm != null) realm.close();
    }
}
