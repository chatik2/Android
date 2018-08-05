package com.shifu.user.truechat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.shifu.user.truechat.json.JsonUid;
import com.shifu.user.truechat.realm.Author;
import com.shifu.user.truechat.realm.Msgs;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static RealmController rc;

    private static CompositeDisposable disposables = new CompositeDisposable();
    private static Disposable navigate = null;
    private static Disposable getData = null;

    public static void dispose(){
        if (getData != null && !getData.isDisposed()) {
            disposables.remove(getData);
            getData = null;
        }
    }

    private static boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        rc = new RealmController(getApplicationContext());
        new RealmRVAdapter(rc.getBase(Msgs.class, null));

        navigate = RxBus.getInstance()
                .getMessages(Event.NAVIGATE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> navigateTo((String) response));

        disposables.add(navigate);

        rc.clear();
//        if (rc.getSize(Author.class) == 0) {
            getData = RestController.login()
                    .onBackpressureDrop()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rc::addAuthor, t -> RestController.handleError(t, "login"));
//        }

//        rc.clear();
//        //rc.clear(Author.class);
//        //rc.clear(Users.class);
//        rc.addAuthor(Response.success(new JsonUid(555L, "user1")));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        disposables.add(navigate);
    }

    @Override
    protected void onPause() {
        super.onPause();
        disposables.remove(navigate);
    }

    private void navigateTo(String fragmentName) {
        Log.d(Event.NAVIGATE.toString(), "received");
        try {
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, (Fragment) Class.forName(fragmentName).newInstance());

            if (!isFirstLoad) transaction.addToBackStack(null);
            isFirstLoad = false;

            transaction.commit();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rc.close();
        disposables.clear();
    }
}
