package org.sartframework.serializers;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.ExplicitIdStrategy;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;

public class ExplicitTest {

    static class Bar {

        Object foo;

        public Bar(Object foo) {
            super();
            this.foo = foo;
        }
    }

    static class Foo {

        String name;

        int value;

        public Foo(String name, int value) {
            super();
            this.name = name;
            this.value = value;
        }
    }

    static class Foo2 {

        String name;

        int value;

        public Foo2(String name, int value) {
            super();
            this.name = name;
            this.value = value;
        }
    }

    public static class IdStrategyFactory1 implements IdStrategy.Factory {

        ExplicitIdStrategy.Registry r = new ExplicitIdStrategy.Registry();

        public IdStrategyFactory1() {
            super();
            System.out.println("@EXPLICIT 1");
        }

        @Override
        public IdStrategy create() {

            return r.strategy;
        }

        @Override
        public void postCreate() {
            r.registerPojo(Bar.class, 1);
            r.registerPojo(Foo.class, 2);
        }
    }

    public static class IdStrategyFactory2 implements IdStrategy.Factory {

        ExplicitIdStrategy.Registry r = new ExplicitIdStrategy.Registry();

        public IdStrategyFactory2() {
            super();
            System.out.println("@EXPLICIT 2");
        }

        @Override
        public IdStrategy create() {

            return r.strategy;
        }

        @Override
        public void postCreate() {
            r.registerPojo(Bar.class, 1);
            r.registerPojo(Foo2.class, 2);
        }
    }


    // @Test
    public void test1() throws Exception {

        // System.setProperty("protostuff.runtime.id_strategy_factory",
        // "io.protostuff.runtime.DefaultIdStrategy");

        Schema<Foo> FOO_SCHEMA = RuntimeSchema.getSchema(Foo.class);

        Schema<Foo2> FOO2_SCHEMA = RuntimeSchema.getSchema(Foo2.class);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        LinkedBuffer buffer = LinkedBuffer.allocate();

        Foo foo = new Foo("hello", 122);

        ProtobufIOUtil.writeTo(outputStream, foo, FOO_SCHEMA, buffer);

        byte[] byteArray = outputStream.toByteArray();

        Foo2 foo2 = FOO2_SCHEMA.newMessage();

        ProtobufIOUtil.mergeFrom(byteArray, foo2, FOO2_SCHEMA);

        Assert.assertEquals("hello", foo2.name);

        Assert.assertEquals(122, foo2.value);
    }

    //@Test 
    //Run first, own jvm
    public void test2() throws Exception {

        System.setProperty("protostuff.runtime.id_strategy_factory", "org.sartframework.serializers.ExplicitTest$IdStrategyFactory1");

        Schema<Bar> BAR_SCHEMA = RuntimeSchema.getSchema(Bar.class);

        Foo foo = new Foo("hello", 1);

        Bar bar = new Bar(foo);

        writeBar(bar, BAR_SCHEMA);
        
        //write Foo
        Assert.assertEquals(bar.foo.getClass(), Foo.class);
    }
    

    //@Test
    //Run second, own jvm
    public void test3() throws Exception {

        System.setProperty("protostuff.runtime.id_strategy_factory", "org.sartframework.serializers.ExplicitTest$IdStrategyFactory2");

        Schema<Bar> BAR_SCHEMA = RuntimeSchema.getSchema(Bar.class);

        Bar bar = readBar(BAR_SCHEMA);
        
        //read Foo2
        Assert.assertEquals(bar.foo.getClass(), Foo2.class);
    }


    private void writeBar(Bar bar, Schema<Bar> schema) throws IOException {
        try (OutputStream os = new FileOutputStream("/tmp/bar.ser")) {
            LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
            ProtobufIOUtil.writeTo(os, bar, schema, linkedBuffer);
        }
    }

    private Bar readBar(Schema<Bar> schema) throws IOException {
        try (InputStream io = new FileInputStream("/tmp/bar.ser")) {
            Bar bar2 = schema.newMessage();
            ProtobufIOUtil.mergeFrom(io, bar2, schema);
           return bar2;
        }
    }

}
