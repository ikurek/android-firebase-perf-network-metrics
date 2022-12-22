package com.ikurek.android.afpnm.processor

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.ktx.trace
import com.ikurek.android.afpnm.model.TraceAttribute
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class RestRequestProcessor(
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

        val metric = performanceInstance.newHttpMetric(
            request.url.toUrl(),
            request.method
        )

        metric.trace {
            response = chain.proceed(request)

            setBaseMetrics(request, response)

            // In REST API only custom attributes are supported for now
            customAttributes.filterIsInstance<TraceAttribute.Custom>().forEach { attribute ->
                putAttribute(attribute.key, attribute.value)
            }
        }

        return response
    }
}