//
// Created by nick on 09.03.25.
//

#ifndef GRAMOPHONE_HELPERS_H
#define GRAMOPHONE_HELPERS_H

#define DLSYM_OR_ELSE(LIB, FUNC) if (!FUNC) { \
    if (!LIB##_handle) { \
        ALOGE("dlsym handle of " #LIB ".so is not open - code bug"); \
    } else { \
        FUNC = (FUNC##_t) dlsym(LIB##_handle, "_" #FUNC); \
        if (!FUNC) { \
            ALOGI("dlsym returned nullptr for _" #FUNC " in " #LIB ".so: %s", dlerror()); \
        } \
    } \
} \
if (!FUNC)

#define DLSYM_OR_RETURN(LIB, FUNC, RET) if (!FUNC) { \
    if (!LIB##_handle) { \
        ALOGE("dlsym handle of " #LIB ".so is not open - code bug"); \
        return RET; \
    } \
    FUNC = (FUNC##_t) dlsym(LIB##_handle, "_" #FUNC); \
    if (!FUNC) { \
        ALOGE("dlsym returned nullptr for _" #FUNC " in " #LIB ".so: %s", dlerror()); \
        return RET; \
    } \
}

#endif //GRAMOPHONE_HELPERS_H
