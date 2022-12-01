ios_inc_dir=./ios/include
ios_libs_dir=./ios/libs
ios_generated_src_dir=./ios/generated

shared_lib_dir=./rustlib-binding

shared_lib_output_dir=$(shared_lib_dir)/output
shared_lib_ios_output=$(shared_lib_output_dir)/ios
shared_lib_android_output=$(shared_lib_output_dir)/android
shared_lib_android_jni_output=$(shared_lib_output_dir)/android/jniLibs
shared_lib_generated_headers=$(shared_lib_output_dir)/headers
shared_lib_generated_java_files=$(shared_lib_output_dir)/java_files
shared_lib_generated_java_source_dir=$(shared_lib_generated_java_files)/src/main/java/io/mosip

android_root=./android
android_app_src_main=$(android_root)/app/src/main
android_jni_libs_dir=$(android_app_src_main)/jniLibs

setup_dirs:
	mkdir -p $(ios_inc_dir) $(ios_libs_dir) $(shared_lib_ios_output) $(ios_generated_src_dir)
	mkdir -p $(shared_lib_android_output) $(shared_lib_generated_java_source_dir)
	mkdir -p $(shared_lib_android_jni_output) $(shared_lib_generated_headers)
	mkdir -p $(android_jni_libs_dir)/x86_64
	mkdir -p $(android_jni_libs_dir)/armeabi-v7a
	mkdir -p $(android_jni_libs_dir)/arm64-v8a
	mkdir -p $(shared_lib_output_dir)/kotlin $(shared_lib_output_dir)/swift

setup_tools:
	cargo install uniffi_bindgen@0.21.0

clean_dirs:
	rm -rf $(ios_inc_dir) $(ios_libs_dir) $(shared_lib_output_dir) $(ios_generated_src_dir)
	rm -rf $(android_jni_libs_dir) $(shared_lib_uniffi_dir)

clean:
	rm -rf $(ios_inc_dir)/* $(ios_libs_dir)/* $(shared_lib_ios_output)/* $(ios_generated_src_dir)/*
	rm -rf $(shared_lib_android_output)/* $(shared_lib_generated_java_source_dir)/*
	rm -rf $(shared_lib_android_jni_output)/* $(shared_lib_generated_headers)/*
	rm -rf $(android_jni_libs_dir)/x86_64/*
	rm -rf $(android_jni_libs_dir)/armeabi-v7a/*
	rm -rf $(android_jni_libs_dir)/arm64-v8a/*
	rm -rf $(shared_lib_output_dir)/kotlin/* $(shared_lib_output_dir)/swift/*
	cargo clean --manifest-path=$(shared_lib_dir)/Cargo.toml

build: setup_dirs build_ios_shared_lib _copy_shared_to_ios build_android_shared_lib _copy_shared_to_android

build_android_shared_lib_x86_64:
	cargo build --target x86_64-linux-android --manifest-path=$(shared_lib_dir)/Cargo.toml

build_android_shared_lib_aarch64:
	cargo build --target aarch64-linux-android --manifest-path=$(shared_lib_dir)/Cargo.toml

build_android_shared_lib_armv7:
	cargo build --target armv7-linux-androideabi  --manifest-path=$(shared_lib_dir)/Cargo.toml

build_android_shared_lib_i686:
	cargo build --target i686-linux-android  --manifest-path=$(shared_lib_dir)/Cargo.toml

build_android_shared_lib: generate_kotlin_bindings build_android_shared_lib_x86_64 build_android_shared_lib_armv7 build_android_shared_lib_aarch64

build_ios_shared_lib: generate_swift_bindings
	cargo lipo --targets x86_64-apple-ios,aarch64-apple-ios --manifest-path=$(shared_lib_dir)/Cargo.toml

copy_shared_to_ios: build_ios_shared_lib _copy_shared_to_ios
copy_shared_to_android: build_android_shared_lib _copy_shared_to_android

generate_kotlin_bindings:
	uniffi-bindgen generate --language kotlin --config $(shared_lib_dir)/uniffi.toml --out-dir $(shared_lib_output_dir)/kotlin $(shared_lib_dir)/src/identity.udl

generate_swift_bindings:
	uniffi-bindgen generate --language swift --config $(shared_lib_dir)/uniffi.toml --out-dir $(shared_lib_output_dir)/swift $(shared_lib_dir)/src/identity.udl

_copy_shared_to_ios:
	cp -f $(shared_lib_output_dir)/swift/*.h $(ios_inc_dir)
	cp -f $(shared_lib_output_dir)/swift/*.swift $(ios_generated_src_dir)
	cp -f $(shared_lib_dir)/target/universal/debug/librustylib_binding.a $(shared_lib_ios_output)
	cp -f $(shared_lib_ios_output)/* $(ios_libs_dir)

_copy_shared_to_android:
	cp -f $(shared_lib_dir)/target/x86_64-linux-android/debug/librustylib_binding.so $(android_jni_libs_dir)/x86_64
	cp -f $(shared_lib_dir)/target/aarch64-linux-android/debug/librustylib_binding.so $(android_jni_libs_dir)/arm64-v8a
	cp -f $(shared_lib_dir)/target/armv7-linux-androideabi/debug/librustylib_binding.so $(android_jni_libs_dir)/armeabi-v7a
	cp -fR $(shared_lib_output_dir)/kotlin $(android_app_src_main)

setup_ios: setup_dirs copy_shared_to_ios