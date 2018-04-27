package br.com.bossini.previsodotempofatecipitarde;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText cidadeEditText;
    private ImageView iconeImageView;
    private TextView descricaoTextView,
            minTextView, maxTextView, humidityTextView;
    private ListView previsoesListView;
    private ArrayAdapter <Previsao> adapter;
    private List<Previsao> previsoes;

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
        previsoesListView = findViewById(R.id.previsoesListView);
        previsoes = new ArrayList <>();
        adapter = new MeuAdapter(this);
        previsoesListView.setAdapter(adapter);

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
                    sb.append("&cnt=16&units=");
                    sb.append(getString(R.string.unidade_temperatura));
                    sb.append("&lang=pt");


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

    private class ConsomeWSPrevisaoTempo extends AsyncTask <String, Void, List<Previsao> >{

        @Override
        protected List <Previsao> doInBackground(String... url) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url[0])
                    .build();
            Response response = null;
            previsoes.clear();
            try{
                response = client.newCall(request).execute();
                String resultado = response.body().string();
                JSONObject previsao = new JSONObject(resultado);
                JSONArray list = previsao.getJSONArray("list");
                for (int i = 0; i < list.length(); i++){
                    JSONObject dia = list.getJSONObject(i);
                    long dt = dia.getLong("dt");
                    JSONObject temp = dia.getJSONObject("temp");
                    double min = temp.getDouble("min");
                    double max = temp.getDouble("max");
                    int humidity = dia.getInt("humidity");
                    JSONArray weather = dia.getJSONArray("weather");
                    String descricao = weather.getJSONObject(0).getString ("description");
                    String icone = weather.getJSONObject(0).getString ("icon");
                    Previsao p = new Previsao (dt, min, max, humidity, descricao, icone);
                    previsoes.add(p);
                }

                return previsoes;
            }catch (IOException | JSONException e){
                e.printStackTrace();
                return new ArrayList <Previsao>();
            }

        }

        @Override
        protected void onPostExecute(List <Previsao> previsoes) {

            //new BaixaImagem().execute(previsao.getIcone());
            adapter.notifyDataSetChanged();
        }


    }
    private class BaixaImagem extends AsyncTask <String, Void, Bitmap>{

        private ImageView iconeImageView;
        public BaixaImagem (ImageView iconeImageView){
            this.iconeImageView = iconeImageView;
        }

        @Override
        protected Bitmap doInBackground(String... icone){
            try{
                String url =
                        "http://openweathermap.org/img/w/" + icone[0] + ".png";
                Log.i("meu_app", "tentando baixar...");
                URL urlDownload = new URL (url);
                HttpURLConnection connection =
                        (HttpURLConnection) urlDownload.openConnection();
                InputStream inputStream =
                        connection.getInputStream();
                Bitmap resultado = BitmapFactory.decodeStream(inputStream);
                Log.i("meu_app", "baixou...");
                return resultado;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //iconeImageView.setImageBitmap(bitmap);
            this.iconeImageView.setImageBitmap(bitmap);
            Toast.makeText(MainActivity.this, "sim, baixou", Toast.LENGTH_SHORT).show();
        }
    }

    private class MeuAdapter extends ArrayAdapter <Previsao>{

        public MeuAdapter (Context context){
            super (context, -1, previsoes);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater =
                    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.item_na_lista, parent, false);
            Previsao p = previsoes.get(position);
            TextView descricaoTextView = v.findViewById(R.id.descricaoTextView);
            TextView minTextView = v.findViewById(R.id.minTextView);
            TextView maxTextView = v.findViewById(R.id.maxTextView);
            TextView humidityTextView = v.findViewById(R.id.humidityTextView);
            ImageView iconeImageView = v.findViewById(R.id.iconeImageView);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            Date date = new Date();
            date.setTime(p.getDt() * 1000);
            String diaDaSemana = sdf.format(date);
            descricaoTextView.setText(
                    String.format("%s:%s",
                            diaDaSemana, p.getDescription()));
            minTextView.setText(getString(R.string.min, p.getMin(),
                    getString(R.string.unidade_temperatura).
                            equals("metric") ? "\u00B0C" : "\u00B0F"));
            String m = getString(R.string.max, p.getMax(),
                    getString(R.string.unidade_temperatura).
                            equals("metric") ? "\u00B0C" : "\u00B0F");
            maxTextView.setText(m);
            NumberFormat percentFormat =
                    NumberFormat.getPercentInstance();
            double humidity = p.getHumidity() / 100d;
            humidityTextView.setText(
                    getString(
                            R.string.humidity, percentFormat.format(humidity)));
            new BaixaImagem(iconeImageView).execute(p.getIcone());
            return v;
        }

        @Override
        public int getCount() {
            return previsoes.size();
        }
    }




}
