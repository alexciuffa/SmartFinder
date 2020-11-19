package com.example.camera2_v04;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MatchResultActivity extends AppCompatActivity {

    private float[] new_face_embedding;
    private double person_distance;
    private PersonFile[] personArray;
    private String[] match_names;
    private int index_match;

    TextView loadingTextView;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_result);

        loadingTextView = (TextView) findViewById(R.id.loadingTextView);
        listView = (ListView) findViewById(R.id.resultListView);

        // Get generated embedding from face
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            new_face_embedding = extras.getFloatArray("vector_embedding");
        }

        loadingTextView.setText("Carregando...");

        /* Request */
        Map<String,String> params = new HashMap<String,String>();
        params.put("lat", "3");
        params.put("lon", "3");
        params.put("embedding", Arrays.toString(new_face_embedding));
        postRequest(params);

    }

    private void postRequest(Map<String,String> params) {
        String base_url="http://192.168.5.199:5000/get_wanted_people";
        String url = new CustomURL(base_url, params).get_url();

        Log.d("postRequest", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingTextView.setVisibility(View.GONE);

                try {
                    JSONArray matches = new JSONObject(response).getJSONArray("matches");

                    if(matches != null && matches.length() > 0 ) {
                        match_names = new String[matches.length()];

                        for (int i = 0; i < matches.length(); i++) {
                            JSONObject match_object = matches.getJSONObject(i);
                            match_names[i] = match_object.getString("person_name");
                        }

                        // Display on ListView
                        ArrayAdapter adapter = new ArrayAdapter<String>(MatchResultActivity.this, R.layout.match_list, match_names);
                        listView.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingTextView.setText("Post Data: Response Failed!\n"+error);

                /*if (error instanceof NetworkError) {
                } else if (error instanceof ServerError) {
                } else if (error instanceof AuthFailureError) {
                } else if (error instanceof ParseError) {
                } else if (error instanceof NoConnectionError) {
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(getContext(),
                            "Oops. Timeout error!",
                            Toast.LENGTH_LONG).show();
                }*/
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<String,String>();
                header.put("Content-Type", "application/json; charset=UTF-8");
                header.put("x-api-key", "mySuperSecretKey");
                return header;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.getCache().clear();
        requestQueue.add(stringRequest);

    }


    private double calculateDistance(float[] a, float[] b) {
        double diff_square_sum = 0;
        for (int i = 0; i < a.length; i++) {
            diff_square_sum += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(diff_square_sum);
    }

}
