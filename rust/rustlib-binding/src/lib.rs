pub mod androidjni;

uniffi_macros::include_scaffolding!("identity");

fn sprinkle(input: String) -> String {
    format!("MSG from G0D: From sprinkle...{}", input)
}

use ed25519_compact::{SecretKey};
use jwt_compact::{alg::Ed25519, prelude::*, Renamed, TimeOptions};
use serde::{Deserialize, Serialize};
use chrono::{Duration, Utc};

use lazy_static::lazy_static;

/// Custom claims encoded in the token.
#[derive(Debug, PartialEq, Serialize, Deserialize)]
struct CustomClaims {
    /// `sub` is a standard claim which denotes claim subject:
    /// https://tools.ietf.org/html/rfc7519#section-4.1.2
    #[serde(rename = "sub")]
    subject: String,
}

lazy_static! {
    static ref TIME_OPTIONS: TimeOptions  = TimeOptions::default();
    static ref ALG_ED25519: Renamed<Ed25519> = Ed25519::with_specific_name();
}

fn jwtsign(private_key: Vec<u8>, claims: String) -> String {

    // TODO: This header can be passed from higher layer
    let hdr = Header::default().with_key_id("test-key");
    let custom_claims = CustomClaims { subject: claims };

    let claims = Claims::new(custom_claims)
        .set_duration_and_issuance(&TIME_OPTIONS, Duration::days(7))
        .set_not_before(Utc::now() - Duration::hours(1));

    let ed_secret_key = SecretKey::from_slice(private_key.as_slice()).unwrap();

    let token_string = ALG_ED25519
        .token(hdr, &claims, &ed_secret_key)
        .expect("unable to create token");
    println!("JWT token created with claims: {}", token_string);

    token_string
}
