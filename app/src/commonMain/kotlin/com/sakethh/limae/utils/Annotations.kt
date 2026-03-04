package com.sakethh.limae.utils

@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn
/**
`runBlockingNonWeb` works as expected on Android and Desktop, but the
native implementation is unavailable on Web via Wasm, so this fallback
essentially does absolutely nothing there. AVOID USING THIS UNLESS IT
DOES NOT IMPACT WEB USAGE, because a proper solution matching standard
behavior won't likely arrive anytime soon.
 * */
annotation class NonWebRunBlocking
