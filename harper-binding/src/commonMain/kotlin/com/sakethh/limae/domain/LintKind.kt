package com.sakethh.limae.domain

import kotlinx.serialization.Serializable

// https://docs.rs/harper-core/latest/harper_core/linting/enum.LintKind.html
@Serializable
enum class LintKind {
    Agreement,
    BoundaryError,
    Capitalization,
    Eggcorn,
    Enhancement,
    Formatting,
    Grammar,
    Malapropism,
    Miscellaneous,
    Nonstandard,
    Punctuation,
    Readability,
    Redundancy,
    Regionalism,
    Repetition,
    Spelling,
    Style,
    Typo,
    Usage,
    WordChoice,
    LimaeNull
}