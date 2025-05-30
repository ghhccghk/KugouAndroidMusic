#include <jni.h>
#include <thread>
#include <android/log.h>
#include <unistd.h>
#include <fcntl.h>
#include "node.h"

#include <string>

#define TAG "NodeNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

JavaVM* g_vm = nullptr;
jclass g_bridgeClass = nullptr;
jmethodID g_logMethod = nullptr;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    g_vm = vm;

    JNIEnv *env = nullptr;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);

    jclass cls = env->FindClass("com/ghhccghk/musicplay/util/NodeBridge");
    g_bridgeClass = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
    g_logMethod = env->GetStaticMethodID(g_bridgeClass, "logFromNative", "(Ljava/lang/String;)V");

    return JNI_VERSION_1_6;
}

void sendLogToKotlin(const std::string &message) {
    JNIEnv *env;
    if (g_vm->AttachCurrentThread(&env, nullptr) != 0) return;

    jstring jmsg = env->NewStringUTF(message.c_str());
    env->CallStaticVoidMethod(g_bridgeClass, g_logMethod, jmsg);
    env->DeleteLocalRef(jmsg);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ghhccghk_musicplay_util_NodeBridge_startNode(JNIEnv *env, jobject) {
    int pipefd[2];
    pipe(pipefd);

    dup2(pipefd[1], STDOUT_FILENO);
    dup2(pipefd[1], STDERR_FILENO);
    close(pipefd[1]);

    std::thread reader([fd = pipefd[0]] {
        char buffer[1024];
        ssize_t n;
        while ((n = read(fd, buffer, sizeof(buffer) - 1)) > 0) {
            buffer[n] = '\0';
            sendLogToKotlin(std::string(buffer));
        }
    });
    reader.detach();
    setenv("platform","lite", 1);
    const char *argv[] = {
            "node",
            "/data/data/com.ghhccghk.musicplay/files/nodejs_files/api_js/app.js"
    };
    int argc = sizeof(argv) / sizeof(argv[0]);
    node::Start(argc, const_cast<char **>(argv));
}
