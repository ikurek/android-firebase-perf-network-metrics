package com.ikurek.android.afpnm.processor

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.ktx.trace
import com.ikurek.android.afpnm.Constants.Headers.APOLLO_HEADER_OPERATION_NAME
import com.ikurek.android.afpnm.model.TraceAttribute
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class ApolloRequestProcessor(
    private val customAttributes: List<TraceAttribute>,
    private val performanceInstance: FirebasePerformance,
    override val setRequestPayloadSize: Boolean,
    override val setResponseContentType: Boolean,
    override val setResponseHttpCode: Boolean,
    override val setResponsePayloadSize: Boolean
) : TracingRequestProcessor() {

    override fun process(chain: Interceptor.Chain): Response {
        lateinit var response: Response
        val request: Request = chain.request()

        val apolloOperationName: String = request.headers[APOLLO_HEADER_OPERATION_NAME].orEmpty()

        val metric = performanceInstance.newHttpMetric(
            request.url.toUrl(),
            request.method
        )

        metric.trace {
            response = chain.proceed(request)

            setBaseMetrics(request, response)

            customAttributes.forEach { attribute ->
                when (attribute) {
                    is TraceAttribute.OperationName ->
                        putAttribute(attribute.key, apolloOperationName)

                    is TraceAttribute.Custom ->
                        putAttribute(attribute.key, attribute.value)
                }
            }
        }

        return response
    }
}