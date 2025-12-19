package org.nift4.alacdecoder;

import androidx.annotation.Nullable;
import androidx.media3.decoder.DecoderException;

public class AlacDecoderException extends DecoderException {

    public AlacDecoderException(String message) {
        super(message);
    }

    public AlacDecoderException(@Nullable Throwable cause) {
        super(cause);
    }
}
