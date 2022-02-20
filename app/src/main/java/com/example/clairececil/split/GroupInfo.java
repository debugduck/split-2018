package com.example.clairececil.split;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
import static android.widget.LinearLayout.VERTICAL;

public class GroupInfo extends AppCompatActivity {

    ArrayList<Double> prices;
    ArrayList<String> items;
    LinearLayout linearLayout;


    int numPeople = 0;


    private List<EditText> editTextList = new ArrayList<EditText>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        linearLayout = new LinearLayout(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(FILL_PARENT, WRAP_CONTENT);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(VERTICAL);

        linearLayout.addView(addButton());
        linearLayout.addView(submitButton());
        linearLayout.setBackgroundColor(Color.parseColor("#272822"));
        setContentView(linearLayout);

        if(getIntent().hasExtra("prices")) {
            prices = (ArrayList<Double>) getIntent().getSerializableExtra("prices");
        }

        if(getIntent().hasExtra("items")) {
            items = getIntent().getStringArrayListExtra("items");
        }
    }

    private Button addButton() {
        Button button = new Button(this);
        button.setHeight(WRAP_CONTENT);
        button.setText("Add Person");
        button.setTextColor(Color.parseColor("#272822"));
        button.setBackgroundColor(Color.parseColor("#66D9EE"));
        button.setOnClickListener(addListener);
        return button;
    }

    private Button submitButton() {
        Button button = new Button(this);
        button.setHeight(WRAP_CONTENT);
        button.setText("Next");
        button.setTextColor(Color.parseColor("#272822"));
        button.setBackgroundColor(Color.parseColor("#A7EC21"));
        button.setOnClickListener(submitListener);
        return button;
    }

    // Access the value of the EditText

    private View.OnClickListener submitListener = new View.OnClickListener() {
        public void onClick(View view) {
            StringBuilder stringBuilder = new StringBuilder();
            for (EditText editText : editTextList) {
                if(editText.getText().toString().equals("")) {
                    numPeople--;
                } else {
                    stringBuilder.append(editText.getText().toString() + " ");
                }
            }

            if(numPeople < 2 || numPeople > 6) {
                Toast.makeText(getBaseContext(), "Please enter at least 2 and at most 6 people", Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getBaseContext(), SplitBill.class);
                i.putStringArrayListExtra("items", items);
                i.putExtra("prices", prices);
                i.putExtra("initials", stringBuilder.toString());
                startActivity(i);
            }
        }
    };

    private View.OnClickListener addListener = new View.OnClickListener() {
        public void onClick(View view) {
            if(numPeople < 6) {
                numPeople++;
                linearLayout.addView(tableLayout());
            } else {
                Toast.makeText(getBaseContext(), "The maximum group size is 6 people.", Toast.LENGTH_LONG).show();
            }
        }
    };

    private TableLayout tableLayout() {
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(true);
        TableRow tableRow = new TableRow(this);
        tableRow.setPadding(0, 10, 0, 0);
        tableRow.addView(editText(String.valueOf(numPeople)));
        tableLayout.addView(tableRow);
        return tableLayout;
    }

    private EditText editText(String hint) {
        EditText editText = new EditText(this);
        editText.setId(Integer.valueOf(hint));
        editText.setHint(hint);
        editText.setHintTextColor(Color.parseColor("#75715E"));
        editText.setTextColor(Color.parseColor("#75715E"));
        editText.setBackgroundColor(Color.parseColor("#272822"));
        editTextList.add(editText);
        return editText;
    }
}
