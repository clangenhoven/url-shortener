package com.clangenhoven.shortly;

import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.AsciiString;
import ratpack.exec.Operation;
import ratpack.exec.Promise;
import ratpack.session.SessionStore;

@Singleton
public class NoOpSessionStore implements SessionStore {

    @Override
    public Operation store(AsciiString sessionId, ByteBuf sessionData) {
        return Operation.noop();
    }

    @Override
    public Promise<ByteBuf> load(AsciiString sessionId) {
        return Promise.value(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public Operation remove(AsciiString sessionId) {
        return Operation.noop();
    }

    @Override
    public Promise<Long> size() {
        return Promise.value(0L);
    }
}
