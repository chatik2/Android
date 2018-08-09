package com.shifu.user.truechat;

import android.content.Context;
import android.util.Log;

import com.shifu.user.truechat.model.Author;
import com.shifu.user.truechat.model.Msg;
import com.shifu.user.truechat.model.User;

import java.util.List;
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

    public void clearAuthor() {
        realm.executeTransactionAsync(realm -> {
            realm.where(Author.class).findAll().deleteAllFromRealm();
        });
    }


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

    public Boolean existMsg(String id){
        return (realm.where(Msg.class).equalTo(User.FIELD_ID, id).findFirst() != null);
    }

    public List<Msg> getDBMsgs(){
        return realm.copyFromRealm(realm.where(Msg.class).findAll().sort("date"));
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

    public Msg getMsg(String id){
        Msg out = realm.where(Msg.class).equalTo(Msg.FIELD_ID, id).findFirst();
        return (out == null)?null:realm.copyFromRealm(out);
    }

    public String getNewMsgId() {
        String out;
        do {
            out = UUID.randomUUID().toString();
        } while (realm.where(Msg.class).equalTo(Msg.FIELD_ID, out).findFirst() != null);
        return out;
    }

    public void updateDate(final String mid, final String date) {
        realm.executeTransactionAsync(realm -> {
            Msg msg = realm.where(Msg.class).equalTo(Msg.FIELD_ID, mid).findFirst();
            if (msg != null) msg.setDate(date);
        });
    }

    public void newMsg(final String text){
        realm.executeTransactionAsync(realm -> {
            String out;
            do {
                out = UUID.randomUUID().toString();
            } while (realm.where(Msg.class).equalTo(Msg.FIELD_ID, out).findFirst() != null);
            Msg msg = realm.createObject(Msg.class);
            msg.setId(out);
            msg.setText(text);
            msg.setUid(realm.where(Author.class).findFirst().getUid());
        });
    }

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

    public void close() {
        if (realm != null) realm.close();
    }
}
