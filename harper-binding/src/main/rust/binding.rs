use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use serde::Serialize;
use lazy_static::lazy_static;
use std::sync::Arc;

use harper_core::linting::{LintGroup, Linter};
use harper_core::parsers::PlainEnglish;
use harper_core::spell::FstDictionary;
use harper_core::{Dialect, Document};

lazy_static! {
    static ref DICTIONARY: Arc<FstDictionary> = {
        FstDictionary::curated()
    };
}

#[derive(Serialize)]
struct LimaeError {
    startIndex: usize,
    endIndex: usize,
    message: String,
    suggestions: Vec<String>,
    kind: String,
}

#[no_mangle]
pub extern "system" fn Java_com_sakethh_limae_HarperEngine_lintNative(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    let input_text: String = env
        .get_string(&input)
        .expect("Couldn't get java string!")
        .into();

    let parser = PlainEnglish;

    let document = Document::new(&input_text, &parser, &*DICTIONARY);

    let mut linter = LintGroup::new_curated(DICTIONARY.clone(), Dialect::American);

    let lints = linter.lint(&document);

    let results: Vec<LimaeError> = lints.iter().map(|lint| {
        let suggestions: Vec<String> = lint
            .suggestions
            .iter()
            .map(|s| s.to_string())
            .collect();

        LimaeError {
            startIndex: lint.span.start,
            endIndex: lint.span.end,
            message: lint.message.to_string(),
            suggestions,
            kind: format!("{:?}", lint.lint_kind),
        }
    }).collect();

    let json_output = serde_json::to_string(&results).unwrap_or("[]".to_string());

    let output = env
        .new_string(json_output)
        .expect("Couldn't create java string!");

    output.into_raw()
}