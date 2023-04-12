package com.shuzhi.cache.core.codec;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author wangxingzhe
 */
public class LongCodec implements ObjectSerializer {

		@Override
		public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) {
			SerializeWriter out = serializer.out;

			if (object == null) {
				out.writeNull(SerializerFeature.WriteNullNumberAsZero);
			} else {
				long value = (Long) object;
				out.writeLong(value);
				if (out.isEnabled(SerializerFeature.WriteClassName)
						&& value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE
						) {
					out.write('L');
				}
			}
		}
	}