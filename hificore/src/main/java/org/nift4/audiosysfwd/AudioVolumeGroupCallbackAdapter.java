package org.nift4.audiosysfwd;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/* package */ final class AudioVolumeGroupCallbackAdapter {

    private static final String TAG = "AVolumeGroupAdapter";

    private final AudioVolumeGroupCallback delegate;
    private Object nativeCallback; // INativeAudioVolumeGroupCallback (runtime)

    AudioVolumeGroupCallbackAdapter(AudioVolumeGroupCallback delegate) {
        this.delegate = delegate;
    }

    private static int getInt(Class<?> c, Object o, String name) throws Exception {
        Field f = c.getField(name);
        return f.getInt(o);
    }

    private static boolean getBoolean(Class<?> c, Object o, String name) throws Exception {
        Field f = c.getField(name);
        return f.getBoolean(o);
    }

    @SuppressWarnings("PrivateApi")
    void register() {
        try {
            nativeCallback = createNativeCallback();

            Class<?> audioSystem = Class.forName("android.media.AudioSystem");
            Class<?> cbInterface =
                    Class.forName("android.media.INativeAudioVolumeGroupCallback");

            Method add = audioSystem.getDeclaredMethod(
                    "registerAudioVolumeGroupCallback",
                    cbInterface
            );
            add.setAccessible(true);
            add.invoke(null, nativeCallback);

        } catch (Throwable t) {
            Log.e(TAG, "register failed", t);
        }
    }

    @SuppressWarnings("PrivateApi")
    void unregister() {
        try {
            if (nativeCallback == null) return;

            Class<?> audioSystem = Class.forName("android.media.AudioSystem");
            Class<?> cbInterface =
                    Class.forName("android.media.INativeAudioVolumeGroupCallback");

            Method remove = audioSystem.getDeclaredMethod(
                    "unregisterAudioVolumeGroupCallback",
                    cbInterface
            );
            remove.setAccessible(true);
            remove.invoke(null, nativeCallback);

        } catch (Throwable t) {
            Log.e(TAG, "unregister failed", t);
        }
    }

    private Object createNativeCallback() throws Exception {
        Class<?> cbInterface =
                Class.forName("android.media.INativeAudioVolumeGroupCallback");

        return Proxy.newProxyInstance(
                cbInterface.getClassLoader(),
                new Class<?>[]{cbInterface},
                (proxy, method, args) -> {
                    if ("onAudioVolumeGroupChanged".equals(method.getName())
                            && args != null
                            && args.length == 1) {
                        handleFrameworkEvent(args[0]);
                    }
                    return null;
                }
        );
    }

    private void handleFrameworkEvent(Object frameworkEvent) {
        try {
            Class<?> clazz = frameworkEvent.getClass();

            AudioVolumeGroupChangeEvent event =
                    new AudioVolumeGroupChangeEvent();

            event.flags = getInt(clazz, frameworkEvent, "flags");
            event.groupId = getInt(clazz, frameworkEvent, "groupId");
            event.volumeIndex = getInt(clazz, frameworkEvent, "volumeIndex");
            event.muted = getBoolean(clazz, frameworkEvent, "muted");

            delegate.onAudioVolumeGroupChanged(event);

        } catch (Throwable t) {
            Log.e(TAG, "event convert failed", t);
        }
    }
}
