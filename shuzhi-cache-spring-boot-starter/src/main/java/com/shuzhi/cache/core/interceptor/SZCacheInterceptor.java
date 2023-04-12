/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shuzhi.cache.core.interceptor;

import com.shuzhi.cache.core.support.CacheAspectSupport;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * AOP Alliance MethodInterceptor for declarative cache
 * management using the common Spring caching infrastructure
 * ({@link org.springframework.cache.Cache}).
 *
 * <p>Derives from the {@link CacheAspectSupport} class which
 * contains the integration with Spring's underlying caching API.
 * CacheInterceptor simply calls the relevant superclass methods
 * in the correct order.
 *
 * <p>CacheInterceptors are thread-safe.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
@SuppressWarnings("serial")
public class SZCacheInterceptor extends CacheAspectSupport implements MethodInterceptor, Serializable {

    private static class ThrowableWrapperException extends RuntimeException {
        private final Throwable original;

        ThrowableWrapperException(Throwable original) {
            this.original = original;
        }
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        Invoker aopAllianceInvoker = () -> {
            try {
                return invocation.proceed();
            } catch (Throwable ex) {
                throw new ThrowableWrapperException(ex);
            }
        };

        try {
            return execute(aopAllianceInvoker, invocation.getThis(), method, invocation.getArguments());
        } catch (ThrowableWrapperException th) {
            throw th.original;
        }
    }
}
