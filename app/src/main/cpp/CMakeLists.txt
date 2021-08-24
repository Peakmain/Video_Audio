
cmake_minimum_required(VERSION 3.4.1)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#判断编译器类型,如果是gcc编译器,则在编译选项中加入c++11支持

if(CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "-std=c++11 ${CMAKE_CXX_FLAGS}")
    message(STATUS "optional:-std=c++11")
endif(CMAKE_COMPILER_IS_GNUCXX)



project("Video_Audio")
include_directories(./include)

set(source_dir ./src)
aux_source_directory(${source_dir} src_list)
aux_source_directory(${source_dir}/crash src_list)
aux_source_directory(${source_dir}/mmkv src_list)
add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        ${src_list})



find_library(
        log-lib

        log)
find_library(  jnigraphics-lib
        jnigraphics )

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${jnigraphics-lib})