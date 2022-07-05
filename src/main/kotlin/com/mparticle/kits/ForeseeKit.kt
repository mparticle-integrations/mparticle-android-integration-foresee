package com.mparticle.kits

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import java.util.*

/**
 * This is not really a true embedded kit - it only supports getSurveyUrl, which is Foresee's
 * primary use-case on the client-side. Everything else is forwarded server-side.
 *
 */
class ForeseeKit : KitIntegration() {
    override fun getName(): String = NAME

    @Throws(IllegalArgumentException::class)
    override fun onKitCreate(
        settings: Map<String, String>,
        context: Context
    ): List<ReportingMessage> {
        require(
            !(KitUtils.isEmpty(settings[CLIENT_ID]) ||
                    KitUtils.isEmpty(settings[ROOT_URL]) ||
                    KitUtils.isEmpty(settings[SURVEY_ID]))
        ) { "Foresee missing required settings." }
        return emptyList()
    }

    override fun setOptOut(optedOut: Boolean): List<ReportingMessage> = emptyList()

    /**
     * example: http://survey.foreseeresults.com/survey/display?cid=8NNxB5BIVJdMBEBUBJ1Fpg==&sid=link&cpp[custid]=1234
     *
     * @param userAttributes
     * @param userAttributeLists
     * @return
     */
    override fun getSurveyUrl(
        userAttributes: Map<String, String>,
        userAttributeLists: Map<String, List<String>>
    ): Uri? {
        val baseUrl = settings[ROOT_URL] ?: return null
        var builder = Uri.parse(baseUrl).buildUpon()
        builder = builder
            .appendQueryParameter("cid", settings[CLIENT_ID])
            .appendQueryParameter("sid", settings[SURVEY_ID])
            .appendQueryParameter("rid", UUID.randomUUID().toString())
        val cpps = StringBuilder()
        if (settings[SEND_APP_VERSION].toBoolean()) {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val version = pInfo.versionName
                cpps.append("cpp[appversion]=").append(version).append("&")
            } catch (nnfe: NameNotFoundException) {
            }
        }
        val keys = userAttributes.keys
        for (key in keys) {
            try {
                val value: Any? = userAttributes[key]
                var strValue: String? = ""
                when {
                    value is String -> strValue = value
                    value is Int -> strValue = value.toString()
                    value is Boolean -> strValue = value.toString()
                    value is Double -> strValue = value.toString()
                    value != null -> strValue = value.toString()
                }
                cpps.append("cpp[").append(key).append("]=").append(strValue).append("&")
            } catch (e: Exception) {
            }
        }

        //remove the extra &
        if (cpps.isNotEmpty()) {
            cpps.delete(cpps.length - 1, cpps.length)
        }
        builder.appendQueryParameter("cpps", cpps.toString())
        return builder.build()
    }

    companion object {
        const val ROOT_URL = "rootUrl"
        const val CLIENT_ID = "clientId"
        const val SURVEY_ID = "surveyId"
        const val SEND_APP_VERSION = "sendAppVersion"
        const val NAME = "ForeSee"
    }
}
