// Headers
#include <jni.h>
#include <string>
#include <vector>
#include <sstream>

#include "seal/seal.h"
#include "JNIBridge.h"

// Namespaces
using namespace std;
using namespace seal;

// Unique pointers
static unique_ptr<SEALContext>  g_context;
static unique_ptr<KeyGenerator> g_keygen;
static unique_ptr<Encryptor>    g_encryptor;
static unique_ptr<Decryptor>    g_decryptor;
static unique_ptr<Evaluator>    g_evaluator;
static unique_ptr<BatchEncoder> g_encoder;
static unique_ptr<PublicKey>    g_public_key;
static unique_ptr<SecretKey>    g_secret_key;

// JNI Functions
// - Sample function
extern "C" JNIEXPORT jstring JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject) {
    string hello = "Hello from C++ (SEAL Ready!)";
    return env->NewStringUTF(hello.c_str());
}