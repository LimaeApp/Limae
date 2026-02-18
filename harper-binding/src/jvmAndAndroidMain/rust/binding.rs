use serde::Serialize;
use lazy_static::lazy_static;
use std::sync::Arc;

use harper_core::linting::{LintGroup, Linter as LinterTrait};
use harper_core::parsers::PlainEnglish;
use harper_core::spell::FstDictionary;
use harper_core::{Dialect, Document};

#[cfg(not(target_arch = "wasm32"))]
use jni::objects::{JClass, JString};
#[cfg(not(target_arch = "wasm32"))]
use jni::sys::jstring;
#[cfg(not(target_arch = "wasm32"))]
use jni::JNIEnv;

#[cfg(target_arch = "wasm32")]
use wasm_bindgen::prelude::*;

lazy_static! {
    static ref DICTIONARY: Arc<FstDictionary> = {
        FstDictionary::curated()
    };
}

#[derive(Serialize)]
struct LimaeError {
    #[serde(rename = "startIndex")]
    start_index: usize,
    #[serde(rename = "endIndex")]
    end_index: usize,
    message: String,
    suggestions: Vec<String>,
    kind: String,
}

fn lint_text(input_text: &str) -> String {
    let parser = PlainEnglish;
    let document = Document::new(input_text, &parser, &*DICTIONARY);
    let mut linter = LintGroup::new_curated(DICTIONARY.clone(), Dialect::American);
    let lints = linter.lint(&document);

    let results: Vec<LimaeError> = lints
        .iter()
        .map(|lint| {
            let suggestions: Vec<String> = lint
                .suggestions
                .iter()
                .map(|s| s.to_string())
                .collect();

            LimaeError {
                start_index: lint.span.start,
                end_index: lint.span.end,
                message: lint.message.to_string(),
                suggestions,
                kind: format!("{:?}", lint.lint_kind),
            }
        })
        .collect();

    serde_json::to_string(&results).unwrap_or("[]".to_string())
}

#[cfg(not(target_arch = "wasm32"))]
#[no_mangle]
pub extern "system" fn Java_com_sakethh_limae_HarperJVMEngine_lintNative(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    let input_text: String = env
        .get_string(&input)
        .expect("Couldn't get java string!")
        .into();

    let json_output = lint_text(&input_text);

    let output = env
        .new_string(json_output)
        .expect("Couldn't create java string!");

    output.into_raw()
}

#[cfg(target_arch = "wasm32")]
#[wasm_bindgen]
pub fn lint(input: &str) -> String {
    lint_text(input)
}

#[cfg(target_arch = "wasm32")]
#[wasm_bindgen(start)]
pub fn setup() {
    console_error_panic_hook::set_once();
}