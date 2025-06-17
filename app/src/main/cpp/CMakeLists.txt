cmake_minimum_required(VERSION 3.18.1)

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp)


add_library(native-lib SHARED native-lib.cpp)

add_library(node SHARED IMPORTED)
set_target_properties(node PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libnode.so)

find_library(log-lib log)

target_link_libraries(native-lib node ${log-lib})
