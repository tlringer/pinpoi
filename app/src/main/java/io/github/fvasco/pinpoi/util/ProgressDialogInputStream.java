package io.github.fvasco.pinpoi.util;

import sparta.checkers.quals.Source;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Input stream that control a progress dialog
 *
 * @author Francesco Vasco
 */
public class ProgressDialogInputStream extends FilterInputStream implements DialogInterface.OnCancelListener {
    private final @Source({}) ProgressDialog progressDialog;

    public ProgressDialogInputStream(final InputStream in, final ProgressDialog progressDialog) {
        super(in);
        Objects.requireNonNull(in);
        Objects.requireNonNull(progressDialog);
        this.progressDialog = progressDialog;
        progressDialog.setProgressNumberFormat("%1$,d / %2$,d");
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(this);
    }

    @Override
    public @Source({}) int read() throws IOException {
        final int b = super.read();
        if (b >= 0) progressDialog.incrementProgressBy(1);
        return b;
    }

    @Override
    public @Source({}) int read(@Source({}) byte /*@Source({})*/ [] buffer) throws IOException {
        return this.read(buffer, 0, buffer.length);
    }

    @Override
    public @Source({}) int read(@Source({}) byte /*@Source({})*/ [] buffer, @Source({}) int byteOffset, @Source({}) int byteCount) throws IOException {
        final int count = super.read(buffer, byteOffset, byteCount);
        if (count >= 0) progressDialog.incrementProgressBy(count);
        return count;
    }

    @Override
    public @Source({}) long skip(@Source({}) long byteCount) throws IOException {
        final long count = super.skip(byteCount);
        if (count >= 0) progressDialog.incrementProgressBy((int) count);
        return count;
    }

    @Override
    public @Source({}) boolean markSupported() {
        return false;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        try {
            in.close();
        } catch (IOException e) {
            // ignore error
        }
    }
}
