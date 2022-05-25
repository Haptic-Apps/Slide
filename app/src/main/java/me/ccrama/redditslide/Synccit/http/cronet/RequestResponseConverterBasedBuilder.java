/*
 * Copyright 2022 Google LLC
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

package me.ccrama.redditslide.Synccit.http.cronet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.chromium.net.CronetEngine;
import org.chromium.net.UploadDataProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

abstract class RequestResponseConverterBasedBuilder<
    SubBuilderT extends RequestResponseConverterBasedBuilder<?, ? extends ObjectBeingBuiltT>,
    ObjectBeingBuiltT> {
  private static final int DEFAULT_THREAD_POOL_SIZE = 4;

  private final CronetEngine cronetEngine;
  private int uploadDataProviderExecutorSize = DEFAULT_THREAD_POOL_SIZE;
  // Not setting the default straight away to lazy initialize the object if it ends up not being
  // used.
  private RedirectStrategy redirectStrategy = null;
  private final SubBuilderT castedThis;

  @SuppressWarnings("unchecked") // checked as a precondition
  RequestResponseConverterBasedBuilder(CronetEngine cronetEngine, Class<SubBuilderT> clazz) {
    this.cronetEngine = checkNotNull(cronetEngine);
    checkArgument(this.getClass().equals(clazz));
    castedThis = (SubBuilderT) this;
  }

  /**
   * Sets the size of upload data provider executor. The same executor is used for all upload data
   * providers within the interceptor.
   *
   * @see org.chromium.net.UrlRequest.Builder#setUploadDataProvider(UploadDataProvider, Executor)
   */
  public final SubBuilderT setUploadDataProviderExecutorSize(int size) {
    checkArgument(size > 0, "The number of threads must be positive!");
    uploadDataProviderExecutorSize = size;
    return castedThis;
  }

  /**
   * Sets the strategy for following redirects.
   *
   * <p>Note that the Cronet (i.e. Chromium) wide safeguards will still apply if one attempts to
   * follow redirects too many times.
   */
  public final SubBuilderT setRedirectStrategy(RedirectStrategy redirectStrategy) {
    checkNotNull(redirectStrategy);
    this.redirectStrategy = redirectStrategy;
    return castedThis;
  }

  abstract ObjectBeingBuiltT build(RequestResponseConverter converter);

  public final ObjectBeingBuiltT build() {
    if (redirectStrategy == null) {
      redirectStrategy = RedirectStrategy.defaultStrategy();
    }

    RequestResponseConverter converter =
        new RequestResponseConverter(
            cronetEngine,
            Executors.newFixedThreadPool(uploadDataProviderExecutorSize),
            // There must always be enough executors to blocking-read the OkHttp request bodies
            // otherwise deadlocks can occur.
            RequestBodyConverterImpl.create(Executors.newCachedThreadPool()),
            new ResponseConverter(),
            redirectStrategy);

    return build(converter);
  }
}
