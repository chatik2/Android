package com.shifu.user.truechat;

import android.content.Context;
import android.util.Log;

import com.shifu.user.truechat.realm.Author;
import com.shifu.user.truechat.realm.Msg;
import com.shifu.user.truechat.realm.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

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
            instance = this;
        }
    }

    public void clear() {
        realm.executeTransactionAsync(realm -> {
            realm.where(Msg.class).findAll().deleteAllFromRealm();
            realm.where(User.class).findAll().deleteAllFromRealm();
            realm.where(Author.class).findAll().deleteAllFromRealm();
        });
    }

    public List<User> getDBUsers(){
        return realm.copyFromRealm(realm.where(User.class).findAll().sort("name"));
    }

    public List<Msg> getDBMsgs(){
        return realm.copyFromRealm(realm.where(Msg.class).findAll().sort("name"));
    }

    public Long getUid(){
        Long uid = null;
        Log.d("getUid","works");
        if (realm.where(Author.class).count() > 0) {
            uid = realm.where(Author.class).findFirst().getUid();
        }
        return uid;
    }

    public Author getAuthor() {
        Author out = realm.where(Author.class).findFirst();
        return (out == null)?null:realm.copyFromRealm(out);
    }

    public Msg getMsg(Long id){
        Msg out = realm.where(Msg.class).equalTo(Msg.FIELD_ID, id).findFirst();
        return (out == null)?null:realm.copyFromRealm(out);
    }

    public void close() {
        if (realm != null) realm.close();
    }
}
