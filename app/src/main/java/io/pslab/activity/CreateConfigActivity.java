package io.pslab.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import io.pslab.R;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;

public class CreateConfigActivity extends AppCompatActivity {

    private ArrayList<String> instrumentsList;
    private ArrayList<String[]> instrumentParamsList;
    private ArrayList<String[]> instrumentParamsListTitles;
    private int selectedItem = 0;
    private String intervalUnit = "sec";
    private EditText intervalEditText;
    private String interval;
    private View rootView;
    private LinearLayout paramsListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        rootView = findViewById(R.id.create_config_root_view);
        paramsListContainer = findViewById(R.id.params_list_container);
        Spinner selectInstrumentSpinner = findViewById(R.id.select_instrument_spinner);
        Spinner intervalUnitSpinner = findViewById(R.id.interval_unit_spinner);
        intervalEditText = findViewById(R.id.interval_edit_text);
        Button createConfigFileBtn = findViewById(R.id.create_config_btn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_config);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        instrumentsList = new ArrayList<>();
        instrumentParamsList = new ArrayList<>();
        instrumentParamsListTitles = new ArrayList<>();
        createArrayLists();
        selectInstrumentSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, instrumentsList));
        selectInstrumentSpinner.setSelection(0, true);
        createCheckboxList();
        selectInstrumentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = position;
                createCheckboxList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedItem = 0;
            }
        });

        intervalUnitSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.interval_units)));
        intervalUnitSpinner.setSelection(0, true);
        intervalUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                intervalUnit = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                intervalUnit = "sec";
            }
        });

        createConfigFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interval = intervalEditText.getText().toString();
                if (interval.length() == 0) {
                    Toast.makeText(CreateConfigActivity.this, getResources().getString(R.string.no_interval_message), Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<String> selectedParamsList = new ArrayList<>();
                    for (int i = 0; i < paramsListContainer.getChildCount(); i ++) {
                        CheckBox checkBox = (CheckBox) paramsListContainer.getChildAt(i);
                        if (checkBox.isChecked()) {
                            selectedParamsList.add(instrumentParamsList.get(selectedItem)[i]);
                        }
                    }
                    createConfigFile(selectedParamsList);
                }
            }
        });
    }

    private void createArrayLists() {

        instrumentParamsList.add(getResources().getStringArray(R.array.oscilloscope_params));
        instrumentParamsList.add(getResources().getStringArray(R.array.multimeter_params));
        instrumentParamsList.add(getResources().getStringArray(R.array.logic_analyzer_params));
        instrumentParamsList.add(getResources().getStringArray(R.array.barometer_params));
        instrumentParamsList.add(getResources().getStringArray(R.array.luxmeter_params));
        instrumentParamsList.add(getResources().getStringArray(R.array.accelerometer_params));

        instrumentParamsListTitles.add(getResources().getStringArray(R.array.oscilloscope_params_title));
        instrumentParamsListTitles.add(getResources().getStringArray(R.array.multimeter_params_title));
        instrumentParamsListTitles.add(getResources().getStringArray(R.array.logic_analyzer_params_title));
        instrumentParamsListTitles.add(getResources().getStringArray(R.array.barometer_params));
        instrumentParamsListTitles.add(getResources().getStringArray(R.array.luxmeter_params));
        instrumentParamsListTitles.add(getResources().getStringArray(R.array.accelerometer_params_title));

        instrumentsList.add(getResources().getString(R.string.oscilloscope));
        instrumentsList.add(getResources().getString(R.string.multimeter));
        instrumentsList.add(getResources().getString(R.string.logical_analyzer));
        instrumentsList.add(getResources().getString(R.string.baro_meter));
        instrumentsList.add(getResources().getString(R.string.lux_meter));
        instrumentsList.add(getResources().getString(R.string.accelerometer));
    }

    private void createCheckboxList() {
        paramsListContainer.removeAllViews();
        String[] params = instrumentParamsListTitles.get(selectedItem);
        for (int i = 0; i < params.length; i++){
            CheckBox checkBox = new CheckBox(CreateConfigActivity.this);
            checkBox.setText(params[i]);
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            checkBoxParams.gravity = Gravity.CENTER_HORIZONTAL;
            checkBoxParams.setMargins(0,(int)getResources().getDimension(R.dimen.create_config_margin1),0,0);
            checkBox.setLayoutParams(checkBoxParams);
            paramsListContainer.addView(checkBox, i);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createConfigFile(ArrayList<String> params) {
        String instrumentName = instrumentsList.get(selectedItem);
        String fileName = "pslab_config.txt";
        String basepath = Environment.getExternalStorageDirectory().getAbsolutePath();

        File baseDirectory = new File(basepath + File.separator + CSVLogger.CSV_DIRECTORY);
        if (!baseDirectory.exists()) {
            try {
                baseDirectory.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File configFile = new File(basepath + File.separator + CSVLogger.CSV_DIRECTORY + File.separator + fileName);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter writer = new FileWriter(configFile);
            writer.write("instrument: " + instrumentName + "\n");
            writer.write("interval: " + interval + " " + intervalUnit + "\n");
            String param = String.join(",", params);
            writer.write("params: " + param);
            writer.flush();
            writer.close();
            CustomSnackBar.showSnackBar(rootView, getString(R.string.file_created_success_message), null, null, Snackbar.LENGTH_SHORT);
        } catch (IOException e) {
            e.printStackTrace();
            CustomSnackBar.showSnackBar(rootView, getString(R.string.file_created_fail_message), null, null, Snackbar.LENGTH_SHORT);
        }

    }
}
