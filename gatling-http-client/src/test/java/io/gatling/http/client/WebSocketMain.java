/*
 * Copyright 2011-2018 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.http.client;

import io.gatling.http.client.ahc.uri.Uri;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.CountDownLatch;

public class WebSocketMain {

  public static void main(String[] args) throws Exception {
    try (GatlingHttpClient client = new GatlingHttpClient(new HttpClientConfig())) {

      Request request = new RequestBuilder(HttpMethod.GET, Uri.create("wss://echo.websocket.org"))
        .setNameResolver(client.getNameResolver())
        .setRequestTimeout(10000)
        .build(true);

      final CountDownLatch latch = new CountDownLatch(1);
      client.execute(request, 0, true, new WebSocketListener() {
          @Override
          public void onWebSocketOpen() {
            System.out.println(">>>>>>onWebSocketOpen");
            sendFrame(new TextWebSocketFrame("COUCOU!!!"));
          }

          @Override
          public void onTextFrame(TextWebSocketFrame frame) {
            System.out.println(">>>>>>onTextFrame " + frame.text());
            sendFrame(new CloseWebSocketFrame());
            latch.countDown();
          }

          @Override
          public void onBinaryFrame(BinaryWebSocketFrame frame) {
            System.out.println(">>>>>>onBinaryFrame");
          }

          @Override
          public void onPongFrame(PongWebSocketFrame frame) {
            System.out.println(">>>>>>onPongFrame");
          }

          @Override
          public void onCloseFrame(CloseWebSocketFrame frame) {
            System.out.println(">>>>>>onCloseFrame");
          }

          @Override
          public void onHttpResponse(HttpResponseStatus status, HttpHeaders headers) {
            System.out.println(">>>>>>onHttpResponse " + status);
          }

          @Override
          public void onHttpResponseBodyChunk(ByteBuf chunk, boolean last) {
          }

          @Override
          public void onThrowable(Throwable e) {
            System.out.println(">>>>>>onThrowable");
            e.printStackTrace();
            latch.countDown();
          }
        });
      latch.await();
    }
  }
}
