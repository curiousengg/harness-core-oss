package io.harness.serializer.kryo;

import static io.harness.serializer.kryo.SerializationClasses.serializationClasses;
import static java.util.Arrays.asList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

import com.esotericsoftware.kryo.Kryo;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.cglib.CGLibProxySerializer;
import de.javakaffee.kryoserializers.guava.ArrayListMultimapSerializer;
import de.javakaffee.kryoserializers.guava.HashMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import de.javakaffee.kryoserializers.guava.LinkedHashMultimapSerializer;
import de.javakaffee.kryoserializers.guava.LinkedListMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ReverseListSerializer;
import de.javakaffee.kryoserializers.guava.TreeMultimapSerializer;
import de.javakaffee.kryoserializers.guava.UnmodifiableNavigableSetSerializer;
import io.harness.serializer.KryoRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

public class OldRegistrar implements KryoRegistrar {
  private static final Logger logger = LoggerFactory.getLogger(OldRegistrar.class);
  @Override
  public void register(Kryo kryo) throws Exception {
    // Register Kryo default serializers
    kryo.register(byte[].class, 10);
    kryo.register(char[].class, 11);
    kryo.register(short[].class, 12);
    kryo.register(int[].class, 13);
    kryo.register(long[].class, 14);
    kryo.register(float[].class, 15);
    kryo.register(double[].class, 16);
    kryo.register(boolean[].class, 17);
    kryo.register(String[].class, 18);
    kryo.register(Object[].class, 19);
    kryo.register(BigInteger.class, 20);
    kryo.register(BigDecimal.class, 21);
    kryo.register(Class.class, 22);
    kryo.register(Date.class, 23);
    //    kryo.register(Enum.class, 24);
    kryo.register(EnumSet.class, 25);
    kryo.register(Currency.class, 26);
    kryo.register(StringBuffer.class, 27);
    kryo.register(StringBuilder.class, 28);
    kryo.register(Collections.EMPTY_LIST.getClass(), 29);
    kryo.register(Collections.EMPTY_MAP.getClass(), 30);
    kryo.register(Collections.EMPTY_SET.getClass(), 31);
    kryo.register(Collections.singletonList(null).getClass(), 32);
    kryo.register(Collections.singletonMap(null, null).getClass(), 33);
    kryo.register(Collections.singleton(null).getClass(), 34);
    kryo.register(ArrayList.class, 35);
    kryo.register(HashMap.class, 36);
    kryo.register(TreeSet.class, 37);
    kryo.register(Collection.class, 38);
    kryo.register(TreeMap.class, 39);
    kryo.register(Map.class, 40);
    kryo.register(TimeZone.class, 41);
    kryo.register(Calendar.class, 42);
    kryo.register(Locale.class, 43);
    kryo.register(Charset.class, 44);
    kryo.register(URL.class, 45);
    kryo.register(Optional.class, 46);
    kryo.register(asList("").getClass(), new ArraysAsListSerializer(), 47);
    kryo.register(java.util.Vector.class, 48);
    kryo.register(java.util.HashSet.class, 49);
    kryo.register(java.util.LinkedHashMap.class, 50);
    // guava ArrayListMultimap, HashMultimap, LinkedHashMultimap, LinkedListMultimap, TreeMultimap
    kryo.register(ArrayListMultimap.class, new ArrayListMultimapSerializer(), 51);
    kryo.register(HashMultimap.class, new HashMultimapSerializer(), 52);
    kryo.register(LinkedHashMultimap.class, new LinkedHashMultimapSerializer(), 53);
    kryo.register(LinkedListMultimap.class, new LinkedListMultimapSerializer(), 54);
    kryo.register(TreeMultimap.class, new TreeMultimapSerializer(), 55);
    kryo.register(InterruptedException.class, 56);
    kryo.register(
        Sets.unmodifiableNavigableSet(new TreeSet<>()).getClass(), new UnmodifiableNavigableSetSerializer(), 57);
    kryo.register(Lists.reverse(Lists.newLinkedList()).getClass(), ReverseListSerializer.forReverseList(), 58);
    kryo.register(
        Lists.reverse(Lists.newArrayList()).getClass(), ReverseListSerializer.forRandomAccessReverseList(), 59);

    kryo.register(InvocationHandler.class, new JdkProxySerializer(), 61);
    kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer(), 62);
    // register CGLibProxySerializer, works in combination with the appropriate action in
    // handleUnregisteredClass (see below)
    kryo.register(CGLibProxySerializer.CGLibProxyMarker.class, new CGLibProxySerializer(), 63);
    kryo.register(RuntimeException.class, 64);
    kryo.register(NullPointerException.class, 65);
    kryo.register(IllegalStateException.class, 66);
    kryo.register(java.io.IOException.class, 67);
    kryo.register(IllegalArgumentException.class, 68);
    kryo.register(java.net.SocketTimeoutException.class, 69);
    kryo.register(ExceptionInInitializerError.class, 70);
    kryo.register(java.net.UnknownHostException.class, 71);
    kryo.register(NoSuchMethodException.class, 72);
    kryo.register(NoClassDefFoundError.class, 73);
    kryo.register(javax.net.ssl.SSLHandshakeException.class, 74);
    kryo.register(java.util.concurrent.atomic.AtomicInteger.class, 75);
    kryo.register(java.net.ConnectException.class, 76);
    kryo.register(StringIndexOutOfBoundsException.class, 77);
    kryo.register(java.util.LinkedList.class, 78);

    // External Serializers
    UnmodifiableCollectionsSerializer.registerSerializers(kryo);
    SynchronizedCollectionsSerializer.registerSerializers(kryo);

    // custom serializers for non-jdk libs

    // guava ImmutableList, ImmutableSet, ImmutableMap, ImmutableMultimap, ReverseList, UnmodifiableNavigableSet
    ImmutableListSerializer.registerSerializers(kryo);
    ImmutableSetSerializer.registerSerializers(kryo);
    ImmutableMapSerializer.registerSerializers(kryo);
    ImmutableMultimapSerializer.registerSerializers(kryo);

    // Harness classes
    Map<String, Integer> classIds = serializationClasses();
    if (classIds != null) {
      for (Entry<String, Integer> entry : classIds.entrySet()) {
        kryo.register(Class.forName(entry.getKey()), entry.getValue());
      }
    }
  }
}
