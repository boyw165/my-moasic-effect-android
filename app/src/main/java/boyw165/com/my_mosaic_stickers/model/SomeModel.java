package boyw165.com.my_mosaic_stickers.model;

import android.graphics.Bitmap;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class SomeModel {

    private PublishSubject<Object> mBus = PublishSubject.create();

    public SomeModel() {
    }

    public Observable<Object> getObservable() {
        return null;
    }
}
