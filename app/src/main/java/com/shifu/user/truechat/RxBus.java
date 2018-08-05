package com.shifu.user.truechat;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

enum Event {
    NAVIGATE, UPDATE
}

public class RxBus<T> {

    private static RxBus instance;
    private PublishSubject<BusMessage<T>> subject = PublishSubject.create();

    public static RxBus getInstance() {
        if (instance == null) instance = new RxBus();
        return instance;
    }

    void sendMessage(Event chanel, T message) {
        Log.d(chanel.toString(), "sent");
        subject.onNext(new BusMessage<T>(chanel, message));
    }

    Observable<T> getMessages(Event chanel) {
        return subject
                .filter(tBusMessage -> tBusMessage.chanel.equals(chanel))
                .map(tBusMessage -> tBusMessage.value);
    }

    class BusMessage<T> {
        Event chanel;
        T value;

        BusMessage(Event key, T value) {
            this.chanel = key;
            this.value = value;
        }
    }
}
