package br.com.bossini.previsodotempofatecipitarde;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText cidadeEditText;
    private ImageView iconeImageView;
    private TextView descricaoTextView,
            minTextView, maxTextView, humidityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cidadeEditText = findViewById(R.id.cidadeEditText);
        iconeImageView = findViewById(R.id.iconeImageView);
        descricaoTextView = findViewById(R.id.descricaoTextView);
        minTextView = findViewById(R.id.minTextView);
        maxTextView = findViewById(R.id.maxTextView);
        humidityTextView = findViewById(R.id.humidityTextView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cidade = cidadeEditText.getEditableText().toString();
                if (cidade != null && cidade.length() > 0){
                    StringBuilder sb = new StringBuilder("");
                    sb.append(getString(R.string.url));
                    sb.append(cidade);
                    sb.append("&appid=");
                    sb.append(getString(R.string.chave));
                    sb.append("&cnt=1&units=metric&lang=de");
                    final String url = sb.toString();
                    new ConsomeWSPrevisaoTempo().execute(url);
                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(url)
                                    .build();

                            try {
                                final Response response = client.newCall(request).execute();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Toast.makeText(MainActivity.this,
                                                    response.body().string(),
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();*/

                }
            }
        });
    }

    private class ConsomeWSPrevisaoTempo extends AsyncTask <String, Void, Previsao>{

        @Override
        protected Previsao doInBackground(String... url) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url[0])
                    .build();
            Response response = null;
            try{
                response = client.newCall(request).execute();
                String resultado = response.body().string();
                JSONObject previsao = new JSONObject(resultado);
                JSONArray list = previsao.getJSONArray("list");
                JSONObject dia = list.getJSONObject(0);
                long dt = dia.getLong("dt");
                JSONObject temp = dia.getJSONObject("temp");
                double min = temp.getDouble("min");
                double max = temp.getDouble("max");
                double humidity = dia.getDouble("humidity");
                JSONArray weather = dia.getJSONArray("weather");
                String descricao = weather.getJSONObject(0).getString ("description");
                String icone = weather.getJSONObject(0).getString ("icon");
                Previsao p = new Previsao (dt, min, max, humidity, descricao, icone);
                return p;
            }catch (IOException | JSONException e){
                e.printStackTrace();
                return new Previsao();
            }

        }

        @Override
        protected void onPostExecute(Previsao previsao) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            Date date = new Date();
            date.setTime(previsao.getDt() * 1000);
            String diaDaSemana = sdf.format(date);
            descricaoTextView.setText(diaDaSemana);
        }
    }




}
