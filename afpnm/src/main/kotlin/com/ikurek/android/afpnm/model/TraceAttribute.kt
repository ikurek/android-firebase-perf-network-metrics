package com.ikurek.android.afpnm.model

import com.google.firebase.perf.util.Constants.MAX_ATTRIBUTE_KEY_LENGTH
import com.google.firebase.perf.util.Constants.MAX_ATTRIBUTE_VALUE_LENGTH
import com.ikurek.android.afpnm.Constants

/**
 * Base interface for predefined custom attributes and user-defined additional attributes
 */
sealed interface TraceAttribute {

    /**
     * Each attribute requires a key, but predefined attributes will fetch the value internally in
     * the interceptor. Max length of key is [MAX_ATTRIBUTE_KEY_LENGTH]
     */
    val key: String


    /**
     * (Apollo GraphQL only) Defines if GraphQL operation name should be attached as a custom
     * attribute key. The value is internally retrieved from
     * [Constants.Headers.APOLLO_HEADER_OPERATION_NAME] header attached by Apollo
     *
     * This may be useful to filter the data for specific types of GraphQL operations
     */
    data class OperationName(
        override val key: String = Constants.Defaults.CUSTOM_ATTRIBUTE_OPERATION_NAME
    ): TraceAttribute

    /**
     * Defines a completely custom attribute. Max length of key is [MAX_ATTRIBUTE_KEY_LENGTH] and
     * max length of value is [MAX_ATTRIBUTE_VALUE_LENGTH]
     */
    data class Custom(
        override val key: String,
        val value: String
    ): TraceAttribute
}