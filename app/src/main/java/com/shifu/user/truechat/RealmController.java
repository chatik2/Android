package com.shifu.user.truechat;

import android.content.Context;
import android.util.Log;

import com.shifu.user.truechat.model.Author;
import com.shifu.user.truechat.model.Msg;
import com.shifu.user.truechat.model.User;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmModel;

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
            Log.d("Init max: ", Long.toString((Long) realm.where(Msg.class).max(Msg.FIELD_ID)));
            instance = this;
        }
    }

    /**
     * Create data functions
     */
    public Long newMsgId() {
        Long max = (Long) realm.where(Msg.class).max(Msg.FIELD_ID);
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
//            msg.setId(out);
//            msg.setText(text);
//            msg.setUid(realm.where(Author.class).findFirst().getUid());
//        });
//    }

    /**
     * Read data functions
     */
    public String getName(Long id){
        User user = realm.where(User.class).equalTo(User.FIELD_ID, id).findFirst();
        if (user == null) return null;

        String out = realm.copyFromRealm(user).getName();
        if (id.equals(getId())) out = "Вы ("+out+")";

        return out;
    }

    public String getAuthorName(){
        Author user = realm.where(Author.class).findFirst();
        return (user == null)?"":user.getUsername();
    }


    public Boolean existUser(Long id){
        return (realm.where(User.class).equalTo(User.FIELD_ID, id).findFirst() != null);
    }

    public Boolean existMsg(Long id){
        return (realm.where(Msg.class).equalTo(User.FIELD_ID, id).findFirst() != null);
    }

    public List<Msg> getDBMsgs(){
        return realm.copyFromRealm(realm.where(Msg.class).findAll().sort(Msg.FIELD_ID));
    }

    public Long getUid(){
        Author out = realm.where(Author.class).findFirst();
        return (out == null)?null:realm.copyFromRealm(out).getUid();
    }

    public Long getId(){
        Author out = realm.where(Author.class).findFirst();
        return (out == null)?null:realm.copyFromRealm(out).getId();
    }

    public Author getAuthor() {
        Author out = realm.where(Author.class).findFirst();
        return (out == null)?null:realm.copyFromRealm(out);
    }

    public Msg getMsg(Long id){
        Msg out = realm.where(Msg.class).equalTo(Msg.FIELD_ID, id).findFirst();
        return (out == null)?null:realm.copyFromRealm(out);
    }

    /**
     * Update data functions
     */
    public void updateName(final Long id, final String name) {
        realm.executeTransactionAsync(realm -> {
            User user = realm.where(User.class).equalTo(User.FIELD_ID, id).findFirst();
            if (user != null) user.setName(name);
        });
    }

    public void updateAuthorName(final String name) {
        realm.executeTransactionAsync(realm -> {
            Author user = realm.where(Author.class).findFirst();
            if (user != null) user.setUsername(name);
        });
    }

    //    public void updateDate(final Long mid, final String date) {
//        realm.executeTransactionAsync(realm -> {
//            Msg msg = realm.where(Msg.class).equalTo(Msg.FIELD_ID, mid).findFirst();
//            if (msg != null) msg.setDate(date);
//        });
//    }

    /**
     * Delete data functions
     */
    public void clear() {
        realm.executeTransactionAsync(realm -> {
            realm.where(Msg.class).findAll().deleteAllFromRealm();
            realm.where(User.class).findAll().deleteAllFromRealm();
            realm.where(Author.class).findAll().deleteAllFromRealm();
        });
    }

    public void clearAuthor() {
        realm.executeTransactionAsync(realm -> {
            realm.where(Author.class).findAll().deleteAllFromRealm();
        });
    }

    public void close() {
        if (realm != null) realm.close();
    }
}
