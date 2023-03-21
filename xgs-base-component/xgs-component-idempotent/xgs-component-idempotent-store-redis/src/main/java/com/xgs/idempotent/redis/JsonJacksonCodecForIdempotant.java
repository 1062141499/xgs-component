/**
 * Copyright 2018 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xgs.idempotent.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.xgs.idempotent.exception.BusinessException;
import com.xgs.idempotent.exception.SystemException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.codec.CborJacksonCodec;
import org.redisson.codec.MsgPackJacksonCodec;

/**
 *
 * @see CborJacksonCodec
 * @see MsgPackJacksonCodec
 *
 * @author xiongguoshuang
 *
 */
@Slf4j
public class JsonJacksonCodecForIdempotant extends BaseCodec {

    public static final BaseCodec INSTANCE = new JsonJacksonCodecForIdempotant();

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
    @JsonAutoDetect(fieldVisibility = Visibility.ANY,
                    getterVisibility = Visibility.PUBLIC_ONLY,
                    setterVisibility = Visibility.NONE,
                    isGetterVisibility = Visibility.NONE)
    public static class ThrowableMixIn {
    }


    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
    @JsonAutoDetect(fieldVisibility = Visibility.ANY,
            getterVisibility = Visibility.PUBLIC_ONLY,
            setterVisibility = Visibility.NONE,
            isGetterVisibility = Visibility.NONE)
    public static class BusinessExceptionMixIn {
        @JsonCreator
        public BusinessExceptionMixIn(@JsonProperty("code") int code,
                              @JsonProperty("msg") String msg) {
        }
    }

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
    @JsonAutoDetect(fieldVisibility = Visibility.ANY,
            getterVisibility = Visibility.PUBLIC_ONLY,
            setterVisibility = Visibility.NONE,
            isGetterVisibility = Visibility.NONE)
    public static class SystemExceptionMixIn {
        @JsonCreator
        public SystemExceptionMixIn(@JsonProperty("code") int code,
                                      @JsonProperty("msg") String msg) {
        }
    }



    protected final ObjectMapper mapObjectMapper;

    private final Encoder encoder = new Encoder() {
        @Override
        public ByteBuf encode(Object in) throws IOException {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream os = new ByteBufOutputStream(out);
                mapObjectMapper.writeValue((OutputStream)os, in);
                return os.buffer();
            } catch (IOException e) {
                out.release();
                throw e;
            } catch (Exception e) {
                out.release();
                throw new IOException(e);
            }
        }
    };

    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            try{
                return mapObjectMapper.readValue((InputStream)new ByteBufInputStream(buf), Object.class);
            }catch (Throwable throwable){
                //反序列化失败
                log.warn("Decoder error", throwable);
                byte[] bytes = ByteBufUtil.getBytes(buf);
                FailOnDecoderObj failOnDecoderObj = new FailOnDecoderObj();
                failOnDecoderObj.setBytes(bytes);
                failOnDecoderObj.setThrowable(throwable);
                return failOnDecoderObj;
            }
        }
    };

    /**
     * 反序列化失败时
     */
    @Data
    public static class FailOnDecoderObj{
        Throwable throwable;
        byte[] bytes;
    }

    public JsonJacksonCodecForIdempotant() {
        this(new ObjectMapper());
    }

    public JsonJacksonCodecForIdempotant(ClassLoader classLoader) {
        this(createObjectMapper(classLoader, new ObjectMapper()));
    }

    protected static ObjectMapper createObjectMapper(ClassLoader classLoader, ObjectMapper om) {
        TypeFactory tf = TypeFactory.defaultInstance().withClassLoader(classLoader);
        om.setTypeFactory(tf);
        return om;
    }

    public JsonJacksonCodecForIdempotant(ObjectMapper mapObjectMapper) {
        this.mapObjectMapper = mapObjectMapper.copy();
        init(this.mapObjectMapper);
        initTypeInclusion(this.mapObjectMapper);
    }

    protected void initTypeInclusion(ObjectMapper mapObjectMapper) {
        TypeResolverBuilder<?> mapTyper = new DefaultTypeResolverBuilder(DefaultTyping.NON_FINAL) {
            @Override
            public boolean useForType(JavaType t) {
                switch (_appliesFor) {
                case NON_CONCRETE_AND_ARRAYS:
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                    // fall through
                case OBJECT_AND_NON_CONCRETE:
                    return (t.getRawClass() == Object.class) || !t.isConcrete();
                case NON_FINAL:
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                    // to fix problem with wrong long to int conversion
                    if (t.getRawClass() == Long.class) {
                        return true;
                    }
                    if (t.getRawClass() == XMLGregorianCalendar.class) {
                        return false;
                    }
                    return !t.isFinal(); // includes Object.class
                default:
                    // case JAVA_LANG_OBJECT:
                    return (t.getRawClass() == Object.class);
                }
            }
        };
        mapTyper.init(JsonTypeInfo.Id.CLASS, null);
        mapTyper.inclusion(JsonTypeInfo.As.PROPERTY);
        mapObjectMapper.setDefaultTyping(mapTyper);
        
        // warm up codec
        try {
            byte[] s = mapObjectMapper.writeValueAsBytes(1);
            mapObjectMapper.readValue(s, Object.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected void init(ObjectMapper objectMapper) {
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setVisibility(objectMapper.getSerializationConfig()
                                                    .getDefaultVisibilityChecker()
                                                        .withFieldVisibility(Visibility.ANY)
                                                        .withGetterVisibility(Visibility.NONE)
                                                        .withSetterVisibility(Visibility.NONE)
                                                        .withCreatorVisibility(Visibility.NONE));
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enable(Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        objectMapper.addMixIn(Throwable.class, ThrowableMixIn.class);
        objectMapper.addMixIn(BusinessException.class,BusinessExceptionMixIn.class);
        objectMapper.addMixIn(SystemException.class,SystemExceptionMixIn.class);
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
    
    @Override
    public ClassLoader getClassLoader() {
        if (mapObjectMapper.getTypeFactory().getClassLoader() != null) {
            return mapObjectMapper.getTypeFactory().getClassLoader();
        }

        return super.getClassLoader();
    }

    public ObjectMapper getObjectMapper() {
        return mapObjectMapper;
    }
}
