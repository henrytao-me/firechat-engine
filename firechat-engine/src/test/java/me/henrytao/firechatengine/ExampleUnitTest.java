package me.henrytao.firechatengine;

import com.google.firebase.database.DataSnapshot;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.junit.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

  private int i = 0;

  public static class SampleModel {

    private final String sample;

    public SampleModel(String hello) {
      this.sample = hello;
    }

    public String getHello() {
      return sample;
    }
  }

  @Test
  public void testKyro() throws Exception {
    SampleModel model = new SampleModel("moto");

    Kryo kryo = new Kryo();
    kryo.register(SampleModel.class);
    kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Output output = new Output(stream);
    kryo.writeObject(output, model);
    output.close();

    byte[] bytes = stream.toByteArray();

    Input input = new Input(bytes);
    SampleModel result = kryo.readObject(input, SampleModel.class);
    input.close();
  }

  @Test
  public void addition_isCorrect() throws Exception {
    Observable.just(false)
        //.combineLatest(getInt().mergeWith(getInt2()), getBoolean(), (integer, aBoolean1) -> {
        //  System.out.println(String.format(Locale.US, "custom | %d | %b", integer, aBoolean1));
        //  return false;
        //})
        .flatMap(aBoolean1 -> {
          System.out.println(String.format(Locale.US, "custom | start"));
          return getInt();
        })
        .flatMap(aBoolean -> {
          return getBoolean().retryWhen(observable -> {
            return observable.flatMap(o -> {
              System.out.println(String.format(Locale.US, "custom | retry"));
              i++;
              if (i > 3) {
                return Observable.error(new RuntimeException("hello"));
              }
              return Observable.timer(500, TimeUnit.MILLISECONDS);
            });
          });
        })
        //.flatMap(aBoolean -> Observable.merge(getInt(), getInt2()))
        //.zipWith(getBoolean(), (integer, aBoolean1) -> {
        //  System.out.println(String.format(Locale.US, "custom | %d | %b", integer, aBoolean1));
        //  return false;
        //})
        .subscribe(aBoolean2 -> {
          System.out.println(String.format(Locale.US, "custom | %b", aBoolean2));
        }, Throwable::printStackTrace);
    Thread.sleep(8000);
  }

  private Observable<Boolean> getBoolean() {
    return Observable.create(subscriber -> {
      if (i % 2 == 0) {
        subscriber.onError(new RuntimeException("error"));
      } else {
        i++;
        subscriber.onNext(true);
      }
    });
  }

  private Observable<Integer> getInt() {
    return Observable.create(subscriber -> {
      Observable.interval(1000, TimeUnit.MILLISECONDS).subscribe(aLong -> {
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