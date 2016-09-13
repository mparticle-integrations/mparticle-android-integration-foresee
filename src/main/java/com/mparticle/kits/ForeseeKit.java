package com.mparticle.kits;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This is not really a true embedded kit - it only supports getSurveyUrl, which is Foresee's
 * primary use-case on the client-side. Everything else is forwarded server-side.
 *
 */
public class ForeseeKit extends KitIntegration {

    public static final String ROOT_URL = "rootUrl";
    public static final String CLIENT_ID = "clientId";
    public static final String SURVEY_ID = "surveyId";
    public static final String SEND_APP_VERSION = "sendAppVersion";


    @Override
    public String getName() {
        return "Foresee";
    }

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) throws IllegalArgumentException {
        if (KitUtils.isEmpty(settings.get(CLIENT_ID)) ||
                KitUtils.isEmpty(settings.get(ROOT_URL)) ||
                KitUtils.isEmpty(settings.get(SURVEY_ID))) {
            throw new IllegalArgumentException("Foresee missing required settings.");
        }
        return null;
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean optedOut) {
        return null;
    }


    /**
     * example: http://survey.foreseeresults.com/survey/display?cid=8NNxB5BIVJdMBEBUBJ1Fpg==&sid=link&cpp[custid]=1234
     *
     * @param userAttributes
     * @param userAttributeLists
     * @return
     */
    @Override
    public Uri getSurveyUrl(Map<String, String> userAttributes, Map<String, List<String>> userAttributeLists) {
        String baseUrl = getSettings().get(ROOT_URL);
        if (baseUrl == null){
            return null;
        }

        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder = builder
                .appendQueryParameter("cid",getSettings().get(CLIENT_ID))
                .appendQueryParameter("sid", getSettings().get(SURVEY_ID))
                .appendQueryParameter("rid", UUID.randomUUID().toString());

        StringBuilder cpps = new StringBuilder();
        if (Boolean.parseBoolean(getSettings().get(SEND_APP_VERSION))){
            try {
                PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
                String version = pInfo.versionName;
                cpps.append("cpp[appversion]=").append(version).append("&");
            }catch (PackageManager.NameNotFoundException nnfe){

            }
        }

        Set<String> keys = userAttributes.keySet();
        for (String key : keys) {
            try {
                Object value = userAttributes.get(key);
                String strValue = "";

                if (value instanceof String) {
                    strValue = (String) value;
                }else if (value instanceof Integer){
                    strValue = Integer.toString((Integer)value);
                }else if (value instanceof Boolean){
                    strValue = Boolean.toString((Boolean)value);
                }else if (value instanceof Double){
                    strValue = Double.toString((Double)value);
                }else if (value != null){
                    strValue = value.toString();
                }
                cpps.append("cpp[").append(key).append("]=").append(strValue).append("&");
            }catch (Exception e){

            }
        }

        //remove the extra &
        if (cpps.length() > 0){
            cpps.delete(cpps.length()-1, cpps.length());
        }

        builder.appendQueryParameter("cpps", cpps.toString());

        return builder.build();
    }
}