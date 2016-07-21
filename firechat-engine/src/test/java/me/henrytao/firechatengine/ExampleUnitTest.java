package me.henrytao.firechatengine;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

  @Test
  public void testFirebase() throws Exception {
    FirebaseDatabase.getInstance().getReference().child("demo").addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onCancelled(DatabaseError databaseError) {
        System.out.println(String.format(Locale.US, "onCancelled | %s", databaseError.toString()));
      }

      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        System.out.println(String.format(Locale.US, "onDataChange | %s", dataSnapshot.toString()));
      }
    });
    Thread.sleep(5000);
  }

  @Test
  public void testFirebaseValueOff() throws Exception {
    FirebaseDatabase.getInstance().getReference().child("messages").child("-KN0vw3OQM9FqHmNFSJc").addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onCancelled(DatabaseError databaseError) {

          }

          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            int i = 0;
            i = 5;
          }
        });
    Thread.sleep(5000);
  }

  @Test
  public void testKyro() throws Exception {
    SuperSampleModel superSampleModel = new SuperSampleModel("moto", "abc");

    Kryo kryo = new Kryo();
    kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    Output output = new Output(stream);
    kryo.writeObject(output, superSampleModel);
    output.close();

    byte[] bytes = stream.toByteArray();

    Input input = new Input(bytes);
    SampleModel result = kryo.readObject(input, SuperSampleModel.class);
    input.close();
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

  public static class SampleModel {

    private final String sample;

    private final SubClass subClass;

    public SampleModel(String hello) {
      this.sample = hello;
      this.subClass = new SubClass("okie salem");
    }

    public String getHello() {
      return sample;
    }

    public SubClass getSubClass() {
      return subClass;
    }
  }

  public static class SubClass {

    private final String test;

    public SubClass(String test) {
      this.test = test;
    }
  }

  public static class SuperSampleModel extends SampleModel {

    private final String abc;

    public SuperSampleModel(String hello, String abc) {
      super(hello);
      this.abc = abc;
    }
  }
}