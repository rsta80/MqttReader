package es.ugr.mqttreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class NewChannel extends AppCompatActivity {

    Button btnUpdate;
    EditText txtChnUpd, txtFldUpd, txtApiUpd;
    Switch swcApiUpd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_channel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitle("Parametros");

        btnUpdate = (Button) findViewById(R.id.btn_update);
        txtChnUpd = (EditText) findViewById(R.id.txt_chn_upd);
        txtFldUpd = (EditText) findViewById(R.id.txt_fld_upd);
        txtApiUpd = (EditText) findViewById(R.id.txt_api_upd);

        swcApiUpd = (Switch) findViewById(R.id.swc_api);

        swcApiUpd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (swcApiUpd.isChecked())
                    txtApiUpd.setVisibility(View.VISIBLE);
                else
                    txtApiUpd.setVisibility(View.INVISIBLE);
            }
        });



        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String strCh = txtChnUpd.getText().toString();
                String strFl = txtFldUpd.getText().toString();
                String strAp = txtApiUpd.getText().toString();

                SharedPreferences.Editor editor = getSharedPreferences("Prefs", MODE_PRIVATE).edit();
                if (!strCh.equals("") ||
                        !strFl.equals("")) {
                    if (swcApiUpd.isChecked() && !strAp.equals("")) {
                        editor.putString("chn", txtChnUpd.getText().toString());
                        editor.putString("fld", txtFldUpd.getText().toString());
                        editor.putString("apik", txtApiUpd.getText().toString());
                        editor.apply();
                        finish();
                    } else if (!swcApiUpd.isChecked()) {
                        editor.putString("chn", txtChnUpd.getText().toString());
                        editor.putString("fld", txtFldUpd.getText().toString());
                        editor.putString("apik", "");
                        editor.apply();
                        finish();
                    } else {
                        Toast.makeText(NewChannel.this,
                                "Llena todos los campos requeridos",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(NewChannel.this,
                            "Llena todos los campos requeridos",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
