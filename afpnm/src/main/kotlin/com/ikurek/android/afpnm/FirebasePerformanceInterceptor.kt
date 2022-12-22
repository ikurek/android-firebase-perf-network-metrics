package com.ikurek.android.afpnm

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.ktx.trace
import com.google.firebase.perf.util.Constants.MAX_ATTRIBUTE_KEY_LENGTH
import com.google.firebase.perf.util.Constants.MAX_ATTRIBUTE_VALUE_LENGTH
import com.google.firebase.perf.util.Constants.MAX_TRACE_CUSTOM_ATTRIBUTES
import com.ikurek.android.afpnm.model.TraceAttribute
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class FirebasePerformanceInterceptor private constructor(
    private val customAttributes: List<TraceAttribute>,
    private val performanceInstance: FirebasePerformance,
    private val setRequestPayloadSize: Boolean,
    private val setResponseContentType: Boolean,
    private val setResponseHttpCode: Boolean,
    private val setResponsePayloadSize: Boolean
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        lateinit var response: Response
        val request: Request = chain.request()

        val metric = performanceInstance.newHttpMetric(
            request.url.toUrl(),
            request.method
        )

        metric.trace {
            response = chain.proceed(request)

            // Metrics for request
            if (setRequestPayloadSize) {
                setRequestPayloadSize(request.body?.contentLength() ?: -1)
            }

            // Metrics for response
            if (setResponseHttpCode) {
                setHttpResponseCode(response.code)
            }
            if (setResponseContentType) {
                setResponseContentType(response.body?.contentType().toString())
            }
            if (setResponsePayloadSize) {
                setResponsePayloadSize(response.body?.contentLength() ?: -1)
            }

            // Custom attributes
            customAttributes.forEach { attribute ->
                when (attribute) {
                    is TraceAttribute.Custom -> putAttribute(attribute.key, attribute.value)
                    else -> { /* Do nothing */ }
                }
            }
        }

        return response.newBuilder().build()
    }


    data class Builder(
        /**
         * A list of custom attributes that can be optionally attached to the trace. The overall number
         * of attributes can't exceed [MAX_TRACE_CUSTOM_ATTRIBUTES]. Bu default, the
         * [TraceAttribute.OperationName] attribute is attached.
         *
         * This may be useful to attach some additional request logging parameters to the traces,
         * like language, build variant, etc
         */
        var customAttributes: List<TraceAttribute> = listOf(
            TraceAttribute.OperationName()
        ),

        /**
         * Sets the [FirebasePerformance] instance used to collect metrics.
         */
        var performanceInstance: FirebasePerformance = FirebasePerformance.getInstance(),

        /**
         * Defines if request payload size should be traced. The registered value is the content
         * length of request body, not including headers and other parts of the response
         */
        var setRequestPayloadSize: Boolean = true,

        /**
         * Defines if response MIME-type should be traced
         *
         * Since Apollo uses JSON as an underlying transport protocol the value of this field will
         * always be set to `application/json` for GraphQL communication, but it can be set to a
         * different value in case of server-side error
         */
        var setResponseContentType: Boolean = true,

        /**
         * Defines if response HTTP code should be traced.
         *
         * For most HTTP-based GraphQL implementations the response code will always be 200, but
         * this can also help tracing some server-side errors (f.e. Internal Server Errors with
         * HTTP code 500)
         */
        var setResponseHttpCode: Boolean = true,

        /**
         * Defines if response payload size should be traced. The registered value is the content
         * length of response body, not including headers and other parts of the response
         */
        var setResponsePayloadSize: Boolean = true,
    ) {
        /**
         * Returns an instance of [FirebasePerformanceInterceptor] with parameters set to this
         * builder instance
         */
        fun build(): FirebasePerformanceInterceptor {

            /**
             * Validate if max limit of trace parameters was not defined
             */
            if (customAttributes.size > MAX_TRACE_CUSTOM_ATTRIBUTES) {
                error(
                    "Number of custom trace attributes can't exceed $MAX_TRACE_CUSTOM_ATTRIBUTES. " +
                            "Defined attributes: ${customAttributes.joinToString { it.key }}"
                )
            }

            /**
             * Validate the length of attribute keys and values
             */
            customAttributes.forEach { attribute ->
                if (attribute.key.length > MAX_ATTRIBUTE_KEY_LENGTH) {
                    error("Custom trace attribute ${attribute.key} key can't " +
                            "exceed $MAX_ATTRIBUTE_KEY_LENGTH characters")
                }

                if (attribute is TraceAttribute.Custom && attribute.value.length > MAX_ATTRIBUTE_VALUE_LENGTH) {
                    error("Custom trace attribute ${attribute.key} value can't " +
                            "exceed $MAX_ATTRIBUTE_VALUE_LENGTH characters")
                }
            }

            return FirebasePerformanceInterceptor(
                customAttributes = customAttributes,
                performanceInstance = performanceInstance,
                setRequestPayloadSize = setRequestPayloadSize,
                setResponseContentType = setResponseContentType,
                setResponseHttpCode = setResponseHttpCode,
                setResponsePayloadSize = setResponsePayloadSize
            )
        }
    }
}