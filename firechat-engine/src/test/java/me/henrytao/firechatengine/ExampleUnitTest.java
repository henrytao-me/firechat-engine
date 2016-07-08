package me.henrytao.firechatengine;

import org.junit.Test;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

  @Test
  public void addition_isCorrect() throws Exception {
    Observable.just(false)
        .combineLatest(getInt().mergeWith(getInt2()), getBoolean(), (integer, aBoolean1) -> {
          System.out.println(String.format(Locale.US, "custom | %d | %b", integer, aBoolean1));
          return false;
        })
        .flatMap(aBoolean -> Observable.merge(getInt(), getInt2()))
        //.zipWith(getBoolean(), (integer, aBoolean1) -> {
        //  System.out.println(String.format(Locale.US, "custom | %d | %b", integer, aBoolean1));
        //  return false;
        //})
        .subscribe(aBoolean2 -> {
          //System.out.println(String.format(Locale.US, "custom | %d", aBoolean2));
        });
    Thread.sleep(4000);
  }

  private Observable<Boolean> getBoolean() {
    return Observable.create(subscriber -> {
      //Observable.interval(1000, TimeUnit.MILLISECONDS).subscribe(aLong -> {
      //  subscriber.onNext(true);
      //});
      subscriber.onNext(true);
    });
  }

  private Observable<Integer> getInt() {
    return Observable.create(subscriber -> {
      Observable.timer(1000, TimeUnit.MILLISECONDS).subscribe(aLong -> {
        subscriber.onNext(1);
      });
    });
  }

  private Observable<Integer> getInt2() {
    return Observable.create(subscriber -> {
      Observable.timer(2000, TimeUnit.MILLISECONDS).subscribe(aLong -> {
        subscriber.onNext(2);
      });
    });
  }
}