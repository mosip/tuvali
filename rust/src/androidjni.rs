
#[cfg(target_os = "android")]
#[allow(non_snake_case)]
pub mod android {
    use chrono::{Duration, Utc};
    use jni::objects::{JClass, JString};
    use jni::strings::JNIString;
    use jni::sys::jbyteArray;
    use jni::sys::jstring;
    use jni::JNIEnv;
    use jni_sys::jboolean;

    use ed25519_compact::{PublicKey, SecretKey};
    use jwt_compact::{alg::Ed25519, prelude::*, Renamed, TimeOptions};
    use serde::{Deserialize, Serialize};
    use std::time::Instant;

    static mut TIME_OPTIONS: Option<TimeOptions> = None;
    static mut ALG_ED25519: Option<Renamed<Ed25519>> = None;

    /// Custom claims encoded in the token.
    #[derive(Debug, PartialEq, Serialize, Deserialize)]
    struct CustomClaims {
        /// `sub` is a standard claim which denotes claim subject:
        /// https://tools.ietf.org/html/rfc7519#section-4.1.2
        #[serde(rename = "sub")]
        subject: String,
    }

    #[no_mangle]
    pub extern "C" fn Java_io_mosip_greetings_Conversation_init(_env: JNIEnv, _class: JClass) {
        android_logger::init_once(
            android_logger::Config::default()
                .with_min_level(log::Level::Debug)
                .with_tag("RUST-LAYER"),
        );

        // libsodium init
        // unsafe {
        //     let r = libsodium_sys::sodium_init();
        //     dbg!(r);
        // }

        // jwt_compact init
        unsafe {
            // unsafe is needed as static mut can cause race conditions
            TIME_OPTIONS = Some(TimeOptions::default());
            ALG_ED25519 = Some(Ed25519::with_specific_name());
        }
    }

    #[no_mangle]
    pub extern "C" fn Java_io_mosip_greetings_Conversation_greet(
        env: JNIEnv,
        // This is the class that owns our static method. It's not going to be used,
        // but still must be present to match the expected signature of a static
        // native method.
        _class: JClass,
        input: JString,
    ) -> jstring {
        log::info!("{}", format!("inside greet"));

        // debug!("this is a debug {}", "message");
        // First, we have to get the string out of Java. Check out the `strings`
        // module for more info on how this works.
        let input: String = env
            .get_string(input)
            .expect("Couldn't get java string!")
            .into();

        log::info!("{}", format!("inside greet with data {}", input));

        // Then we have to create a new Java string to return. Again, more info
        // in the `strings` module.
        let output = env
            .new_string(format!("From Rust: Hello, {}!", input))
            .expect("Couldn't create java string!");

        // Finally, extract the raw pointer to return.
        output.into_raw()
    }

    // encrypt & decrypt a dynamic string
    // #[no_mangle]
    // pub extern "C" fn Java_io_mosip_greetings_Conversation_encrypt(
    //     env: JNIEnv,
    //     _class: JClass,
    //     receiver_public_key: jbyteArray,
    //     sender_private_key: jbyteArray,
    //     data: JString,
    // ) -> jbyteArray {
    //     log::info!("{}", format!("encyrpt func start"));
    //     let java_data: String = env.get_string(data).expect("expected string data").into();
    //     log::info!("{}", format!("data from java: {:?}", java_data));

    //     let pub_key = env.convert_byte_array(receiver_public_key).unwrap();
    //     let priv_key = env.convert_byte_array(sender_private_key).unwrap();

    //     log::info!("{}", format!("pub key from java: {:?}", pub_key));
    //     log::info!("{}", format!("priv key from java: {:?}", priv_key));

    //     let mut cipher_text: Vec<u8> = Vec::new();
    //     cipher_text.resize_with(
    //         (libsodium_sys::crypto_box_MACBYTES + java_data.len() as u32) as usize,
    //         Default::default,
    //     );

    //     libsodium_encrypt(java_data, pub_key, priv_key, &mut cipher_text);

    //     log::info!("{}", format!("encyrpt func end: {:?}", cipher_text));
    //     env.byte_array_from_slice(&cipher_text).unwrap()
    // }

    // fn libsodium_encrypt(
    //     text: String,
    //     pub_key: Vec<u8>,
    //     priv_key: Vec<u8>,
    //     cipher_text: &mut Vec<u8>,
    // ) {
    //     let text_ptr: *const u8 = text.as_ptr();

    //     let cipher_text_ptr: *mut u8 = cipher_text.as_mut_ptr();

    //     let nonce = [0; libsodium_sys::crypto_box_NONCEBYTES as usize];
    //     let nonce_ptr: *const u8 = nonce.as_ptr();

    //     let pub_key_ptr = pub_key.as_ptr();
    //     let priv_key_ptr = priv_key.as_ptr();

    //     unsafe {
    //         let r = libsodium_sys::crypto_box_easy(
    //             cipher_text_ptr,
    //             text_ptr,
    //             text.len() as u64,
    //             nonce_ptr,
    //             pub_key_ptr,
    //             priv_key_ptr,
    //         );
    //         log::info!("{}", format!("crypto operation: {:?}", r));
    //     }
    // }

    // JWT sign & verification of dynamic string
    #[no_mangle]
    pub extern "C" fn Java_io_mosip_greetings_Conversation_jwtsign(
        env: JNIEnv,
        _class: JClass,
        private_key: jbyteArray,
        claims_subject: JString,
    ) -> jstring {
        let priv_key = env.convert_byte_array(private_key).unwrap();

        let now = Instant::now();

        // TODO: This header can be passed from higher layer
        let hdr = Header::default().with_key_id("test-key");

        let subject = env
            .get_string(claims_subject)
            .expect("expected string data")
            .into();
        let custom_claims = CustomClaims { subject: subject };

        let claims = Claims::new(custom_claims)
            .set_duration_and_issuance(unsafe { &TIME_OPTIONS.unwrap() }, Duration::days(7))
            .set_not_before(Utc::now() - Duration::hours(1));

        let edSecretKey = SecretKey::from_slice(priv_key.as_slice()).unwrap();

        let token_string = unsafe { ALG_ED25519.unwrap() }
            .token(hdr, &claims, &edSecretKey)
            .expect("unable to create token");
        println!(
            "ED25519 JWT token generation in ns: {}",
            now.elapsed().as_nanos()
        );
        println!("JWT token created with claims: {}", token_string);

        let jni_token_string: JNIString = token_string.into();
        env.new_string(jni_token_string).unwrap().into_raw()
    }

    #[no_mangle]
    pub extern "C" fn Java_io_mosip_greetings_Conversation_jwtverify(
        env: JNIEnv,
        _class: JClass,
        public_key: jbyteArray,
        token: JString,
    ) -> jboolean {
        let token_string: String = env.get_string(token).expect("expected string token").into();
        let pub_key = env.convert_byte_array(public_key).unwrap();
        let edPublicKey = PublicKey::from_slice(pub_key.as_slice()).unwrap();

        let un_trusted_token =
            UntrustedToken::new(&token_string).expect("parsing JWT token failed");
        let token: Token<CustomClaims> = unsafe { ALG_ED25519.unwrap() }
            .validate_integrity(&un_trusted_token, &edPublicKey)
            .expect("JWT integrity verification failed");
        token
            .claims()
            .validate_expiration(&unsafe { TIME_OPTIONS.unwrap() })
            .expect("invalid expiry")
            .validate_maturity(&unsafe { TIME_OPTIONS.unwrap() })
            .expect("invalid maturity");
        1
    }
}
