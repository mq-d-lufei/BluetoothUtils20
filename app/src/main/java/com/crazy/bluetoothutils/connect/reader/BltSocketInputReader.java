package com.crazy.bluetoothutils.connect.reader;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Created by feaoes on 2018/7/2.
 */

public class BltSocketInputReader extends Reader {

    final BltSocketInputReader self = this;

    private InputStream inputStream;

    public BltSocketInputReader(InputStream inputStream) {
        super(inputStream);
        this.inputStream = inputStream;
    }

    private boolean __i__isOpen() {
        return this.inputStream != null;
    }

    @Override
    public int read(@NonNull char[] cbuf, int off, int len) throws IOException {
        throw new IOException("read() is not support for SocketInputReader, try readBytes().");
    }

    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (null != this.inputStream) {
                this.inputStream.close();
                this.inputStream = null;
            }
        }
    }

    public byte[] readData(int bufferSize) throws IOException {

        if (bufferSize <= 0) {
            return null;
        }

        synchronized (lock) {

            if (!__i__isOpen()) {
                throw new IOException("InputStreamReader is closed");
            }

            byte[] receiveBufferArray = new byte[bufferSize];

            int readNumber = self.inputStream.read(receiveBufferArray, 0, receiveBufferArray.length);

            if (-1 == readNumber) {
                throw new IOException("readToData(), Server Disconnected, readNumber == -1");
            }

            if (readNumber > 0) {

                byte[] realBuffer = new byte[readNumber];

                System.arraycopy(receiveBufferArray, 0, realBuffer, 0, readNumber);

                return realBuffer;
            }

            return null;
        }
    }

    /**
     * C30延迟200ms读取数据
     */
    public byte[] readData(int bufferSize, long delayTime) throws IOException {
        if (bufferSize <= 0 || delayTime <= 0) {
            throw new IOException("bufferSize <= 0 || delayTime <= 0");
        }

        synchronized (lock) {

            long startTime, durationTime;

            byte[] receiveBufferArray = new byte[bufferSize];

            startTime = SystemClock.elapsedRealtime();

            int readNumber = self.inputStream.read(receiveBufferArray, 0, receiveBufferArray.length);

            if (-1 == readNumber) {
                throw new IOException("readToData(), Server Disconnected, readNumber == -1");
            }

            durationTime = SystemClock.elapsedRealtime() - startTime;

            if (durationTime < delayTime) {
                try {
                    Thread.sleep(delayTime - durationTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (readNumber > 0) {

                byte[] realBuffer = new byte[readNumber];

                System.arraycopy(receiveBufferArray, 0, realBuffer, 0, readNumber);

                return realBuffer;
            }

            return null;
        }

    }

}
