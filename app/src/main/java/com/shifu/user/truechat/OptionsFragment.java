package com.shifu.user.truechat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shifu.user.truechat.model.Author;
import com.shifu.user.truechat.model.Msg;

import java.util.Collections;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class OptionsFragment extends Fragment {

    private static CompositeDisposable disposables = new CompositeDisposable();
    private static Disposable observerSend = null;

    private static RealmController rc=RealmController.getInstance();
    private static ApiInterface  api = ApiClient.getInstance().getApi();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.options_fragment, container, false);

        TextView name = view.findViewById(R.id.new_name);
        name.requestFocus();
        Button send = view.findViewById(R.id.button_save);
        send.setOnClickListener(button -> {
            if (name.getText().length() > 0) {
                pushName(name.getText().toString());
                name.setText("");
            }
        });
        return view;
    }

    private Disposable pushName(final String text){
        dispose(observerSend);
        rc.updateName(rc.getId(), text);
        rc.updateAuthorName(text);
        setTitle(text);

        Disposable observer = Single.just(rc.getUid())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(uid -> api.pushName(uid, RequestBody.create(MediaType.parse("text/plain"), text)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> RVAdapter.getInstance().notifyDataSetChanged());
        return observer;
    }

    void setTitle(String add){
        String newTitle = getResources()
                .getString(R.string.app_logged, add);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(newTitle);
    }

    private void dispose(Disposable observer){
        if (observer != null && !observer.isDisposed()) observer.dispose();
    }

    @Override
    public void onResume(){
        super.onResume();
        this.setMenuVisibility(false);
    }

    @Override
    public void onStop(){
        super.onStop();
        this.setMenuVisibility(true);
    }

}
