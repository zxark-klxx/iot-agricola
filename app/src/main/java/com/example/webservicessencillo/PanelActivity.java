package com.example.webservicessencillo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class PanelActivity extends AppCompatActivity {

    TextView tvTemperatura, tvHumedad;
    Button btnActualizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        tvTemperatura = findViewById(R.id.tvTemperatura);
        tvHumedad = findViewById(R.id.tvHumedad);
        btnActualizar = findViewById(R.id.btnActualizar);

        // Pedir los datos al servidor apenas se abre la pantalla
        obtenerDatosSensores();

        // Pedir los datos nuevamente si el usuario presiona el botón
        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PanelActivity.this, "Actualizando...", Toast.LENGTH_SHORT).show();
                obtenerDatosSensores();
            }
        });
    }

    private void obtenerDatosSensores() {
        // Apuntamos a la nueva ruta que creaste en Python
        String url = "http://10.0.2.2:5000/api/iot/lectura/actual";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("success")) {

                                // Extraemos el bloque de "data" que mandó Python
                                JSONObject data = response.getJSONObject("data");
                                double temp = data.getDouble("temperatura");
                                double hum = data.getDouble("humedad");

                                // Actualizamos los textos grandes en la pantalla
                                tvTemperatura.setText(temp + " °C");
                                tvHumedad.setText(hum + " %");

                                // ¡ALGORITMO DE HELADAS!
                                if (temp <= 2.0) {
                                    tvTemperatura.setTextColor(Color.RED);
                                    Toast.makeText(PanelActivity.this, "¡ALERTA DE HELADA DETECTADA!", Toast.LENGTH_LONG).show();
                                } else {
                                    tvTemperatura.setTextColor(Color.BLACK);
                                }

                            } else {
                                Toast.makeText(PanelActivity.this, "Error del servidor", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(PanelActivity.this, "Error procesando datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PanelActivity.this, "Error de red: No se pudo conectar a los sensores", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}