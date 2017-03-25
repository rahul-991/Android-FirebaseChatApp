package com.menon.rahul.uberapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentEmotion;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Rahul on 2/13/2017.
 */

class BackgroundAlchemyService extends AsyncTask<StringBuilder, Integer, DocumentEmotion>
{
    private Context mContext;
    private AlchemyLanguage AlchemyService;
    private UberApp state;
    private ProgressDialog progressDialog;

    public BackgroundAlchemyService(Context c)
    {
        this.mContext = c;
        AlchemyService = new AlchemyLanguage();
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        state = (UberApp) mContext.getApplicationContext();
        AlchemyService = state.getAlchemyService();
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Analyzing..");
        progressDialog.show();
    }

    @Override
    protected DocumentEmotion doInBackground(StringBuilder... str)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AlchemyLanguage.TEXT, str[0].toString());
        DocumentEmotion emotion = AlchemyService.getEmotion(params).execute();
        DocumentSentiment sentiment = AlchemyService.getSentiment(params).execute();
        Log.d("Sentiment", sentiment.getSentiment().toString());
        Log.d("Emotion", emotion.getEmotion().toString());
        return emotion;
    }

    @Override
    protected void onPostExecute(DocumentEmotion emotion)
    {
        super.onPostExecute(emotion);
        if(progressDialog!=null)
            progressDialog.dismiss();

        Map<String, Double> emotionMap = new HashMap<>(5);
        emotionMap.put("Anger", emotion.getEmotion().getAnger());
        emotionMap.put("Disgust", emotion.getEmotion().getDisgust());
        emotionMap.put("Joy", emotion.getEmotion().getJoy());
        emotionMap.put("Fear", emotion.getEmotion().getFear());
        emotionMap.put("Sadness", emotion.getEmotion().getSadness());

        String toastMsg = null;
        double maxEmotion = Double.MIN_VALUE;

        for(String str: emotionMap.keySet())
        {
            if(emotionMap.get(str) > maxEmotion)
            {
                toastMsg = str;
                maxEmotion = emotionMap.get(str);
            }
        }

        Toast.makeText(mContext, "The user's emotion: "+toastMsg, Toast.LENGTH_SHORT).show();
    }
}
