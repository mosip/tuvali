use std::{path::Path};

fn main() {
    let identity_dsl = "./src/identity.udl";
    uniffi_build::generate_scaffolding(identity_dsl).unwrap();
    println!("cargo:rerun-if-changed={}", identity_dsl);

    println!("cargo:rustc-link-search={}", create_tmp_libgcc());
}

// Rust (1.56 as of writing) still requires libgcc during linking, but this does
// not ship with the NDK anymore since NDK r23 beta 3.
// See https://github.com/rust-lang/rust/pull/85806 for a discussion on why libgcc
// is still required even after replacing it with libunwind in the source.
// XXX: Add an upper-bound on the Rust version whenever this is not necessary anymore.

// Implementation as per here - https://github.com/rust-windowing/android-ndk-rs/blob/21b11feff1b612656558d435908249faf78c980f/ndk-build/src/cargo.rs#L83
fn create_tmp_libgcc() -> String {
    let cargo_apk_link_dir = Path::new("target").join("tmp");
    std::fs::create_dir_all(&cargo_apk_link_dir).expect("error creating folder path for temporary libgcc.a");
    let libgcc = cargo_apk_link_dir.join("libgcc.a");
    std::fs::write(&libgcc, "INPUT(-lunwind)").expect("error creating contents for libgcc.a");
    String::from(cargo_apk_link_dir.to_str().unwrap())
}