package com.example.clairececil.split;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;

public class ShowTotals extends AppCompatActivity {

    HashMap<String, Double> userTotals = new HashMap<>();
    HashMap<String, Double> itemPriceMap = new HashMap<>();
    HashMap<String, String> userColors = new HashMap<>();
    TextView totals;
    Double total;
    Double subtotal;
    Double tax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_totals);

        if(getIntent().hasExtra("userTotals")) {
            userTotals = (HashMap<String, Double>) getIntent().getSerializableExtra("userTotals");
        }

        if(getIntent().hasExtra("itemPriceMap")) {
            itemPriceMap = (HashMap<String, Double>) getIntent().getSerializableExtra("itemPriceMap");
        }

        if(getIntent().hasExtra("userColors")) {
            userColors = (HashMap<String, String>) getIntent().getSerializableExtra("userColors");
        }

        total = itemPriceMap.get("Total");
        subtotal = itemPriceMap.get("Subtotal");
        tax = itemPriceMap.get("Tax");

        int partySize = userTotals.size();
        Double individualTax = round((tax / partySize));

        int count = 1;
        for(String key : userTotals.keySet()) {
            String name = "totals"+ count;
            int id = getResources().getIdentifier(name, "id", getPackageName());
            totals = (TextView) findViewById(id);
            Double iTotal = round(individualTax + userTotals.get(key));
            totals.append(key + "  |  $" + round(userTotals.get(key)));
            totals.append(" | with tax: $" + round(iTotal) + "\n");
            totals.append("25% tip: $" + round((iTotal * 0.25) + iTotal));
            totals.append(" | 20% tip: $" + round((iTotal * 0.2) + iTotal));
            totals.append(" | 15% tip  $" + round((iTotal * 0.15) + iTotal));
            totals.setTextColor(Color.parseColor(userColors.get(key)));
            count++;
        }
    }

    double round(double val) {
        DecimalFormat twoPlaces = new DecimalFormat("###.##");
        return Double.valueOf(twoPlaces.format(val));
    }
}
