package com.example.clairececil.split;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Shader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SplitBill extends AppCompatActivity {

    private Shader shader;
    private GradientManager mGradientManager;

    ArrayList<Double> prices;
    ArrayList<String> items;
    ArrayList<String> initials = new ArrayList<>();
    ArrayList<String> itemsWithPrices = new ArrayList<>();

    HashMap<String, SplitItem> splitItems = new HashMap<>(); // A map of each item and its information about being split
    HashMap<String, Double> itemPriceMap; // A map of each item and its original price
    HashMap<String, Double> currentItemPriceMap; // A map of each item and how much balance is currently left
    HashMap<String, String> userColors; // A map of users to their assigned text colors
    HashMap<String, Double> userTotals; // A map of each user and their running totals

    private ItemListCustomAdapter mAdapter;
    ListView itemList;
    TextView totalView;

    static final String[] colors = {"#E5DB74", "#AE81FF", "#66D9EE", "#A7EC21", "#F92772", "#F9FAF4"};
    static final String listSeparator = "...........";

    String currentUser;
    String currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_bill);


        itemList = (ListView) findViewById(R.id.item_list);
        totalView = (TextView) findViewById(R.id.total_view);

        if(getIntent().hasExtra("initials")) {
            String i = getIntent().getStringExtra("initials");
            String[] splitInitials = i.split(" ");
            for(String s : splitInitials) {
                if(!s.equals("")) {
                    initials.add(s);
                }
            }
        }

        if(getIntent().hasExtra("prices")) {
            prices = (ArrayList<Double>) getIntent().getSerializableExtra("prices");
        }

        if(getIntent().hasExtra("items")) {
            items = getIntent().getStringArrayListExtra("items");
        }

        itemPriceMap = mapItemsToPrices();
        currentItemPriceMap = mapItemsToPrices();
        userColors = mapUsersToColors();
        userTotals = mapUsersToTotals();
        itemsWithPrices = createItemizedList();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, initials);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView adapter, View v, int i, long lng) {
                currentUser =  adapter.getItemAtPosition(i).toString();
                ((TextView) adapter.getChildAt(0)).setTextColor(Color.parseColor(userColors.get(currentUser)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView)
            {

            }
        });

        registerForContextMenu(itemList);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                //Toast.makeText(getBaseContext(), "ONITEMCLICK", Toast.LENGTH_SHORT).show();
                String line = itemList.getItemAtPosition(position).toString();
                String[] itemWithPrice = line.split(listSeparator + " ");
                String item = itemWithPrice[0];
                String price = itemWithPrice[1].replace("$", "");
                Double toAdd = round(Double.parseDouble(price));
                currentItem = item;
                // Case 1: Item has already been split by the current user and they'd like to remove it
                if(splitItems.containsKey(item) && splitItems.get(item).partyMembers.contains(currentUser)) {
                    Double takeBack = round(splitItems.get(item).originalPrice / splitItems.get(item).partyMembers.size());
                    splitItems.get(item).partyMembers.remove(currentUser);
                    userTotals.put(currentUser, userTotals.get(currentUser) - takeBack);
                    TextView t = (TextView) arg1.findViewById(R.id.label);
                    updateTextView(t, splitItems.get(item));
                    updateSplitItemOnRemove(splitItems.get(item));
                    updateCurrentTotal();
                // Case 2: Item has already been split but not by the current user and they'd like to add it
                } else if(splitItems.containsKey(item) && !splitItems.get(item).partyMembers.contains(currentUser)) {
                    updateSplitItemOnAdd(splitItems.get(item));
                    splitItems.get(item).partyMembers.add(currentUser);
                    Double addBack = round(splitItems.get(item).originalPrice / splitItems.get(item).partyMembers.size());
                    userTotals.put(currentUser, userTotals.get(currentUser) + addBack);
                    TextView t = (TextView) arg1.findViewById(R.id.label);
                    updateTextView(t, splitItems.get(currentItem));
                    updateCurrentTotal();
                // Case 3: Item hasn't been split yet and the current user is the first to select it
                } else {
                    SplitItem newSplit = new SplitItem(currentItem, itemPriceMap.get(currentItem));
                    newSplit.partyMembers.add(currentUser);
                    splitItems.put(currentItem, newSplit);
                    Double currentUserTotal = userTotals.get(currentUser);
                    userTotals.put(currentUser, currentUserTotal + itemPriceMap.get(currentItem));
                    TextView t = (TextView) arg1.findViewById(R.id.label);
                    int userColorInt = Color.parseColor(userColors.get(currentUser));
                    t.setTextColor(userColorInt);
                    updateCurrentTotal();
                }
            }
        });

        updateCurrentTotal();

        mAdapter = new ItemListCustomAdapter(itemsWithPrices);
        itemList.setAdapter(mAdapter);

        totalView.setTextColor(Color.parseColor("#75715E"));

    }

    public HashMap<String, Double> mapItemsToPrices() {
        HashMap<String, Double> result = new HashMap<>();
        for(int i = 0; i < items.size(); i++) {
            result.put(items.get(i), prices.get(i));
        }
        result.put("Subtotal", round(prices.get(prices.size() - 3)));
        result.put("Tax", round(prices.get(prices.size() - 2)));
        result.put("Total", round(prices.get(prices.size() - 1)));
        return result;
    }

    public HashMap<String, String> mapUsersToColors() {
        HashMap<String, String> result = new HashMap<>();
        for(int i = 0; i < initials.size(); i++) {
            result.put(initials.get(i), colors[i]);
        }
        return result;
    }

    public ArrayList<String> createItemizedList() {
        ArrayList<String> result = new ArrayList<>();
        for (HashMap.Entry<String, Double> item : itemPriceMap.entrySet()) {
            boolean notTotal = !item.getKey().equals("Subtotal") && !item.getKey().equals("Tax") && !item.getKey().equals("Total");
            if(notTotal) {
                result.add(item.getKey() + listSeparator + " $" + item.getValue());
            }
        }
        return result;
    }

    public HashMap<String, Double> mapUsersToTotals() {
        HashMap<String, Double> result = new HashMap<>();
        for(String i : initials) {
            result.put(i, 0.0);
        }
        return result;
    }

    private class ItemListCustomAdapter extends BaseAdapter {

        private ArrayList<String> mData;
        private LayoutInflater mInflater;

        public ItemListCustomAdapter(ArrayList<String> items) {
            mData = items;
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        public int getCount() {
            return mData.size();
        }


        public String getItem(int position) {
            return mData.get(position);
        }


        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.line, null);
                holder = new ViewHolder();
                holder.textView = (TextView)convertView.findViewById(R.id.label);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            String s = mData.get(position);
            holder.textView.setText(s);
            return convertView;
        }

    }

    public static class ViewHolder {
        public TextView textView;
    }

    Double round(Double val) {
        DecimalFormat twoPlaces = new DecimalFormat("###.##");
        return Double.valueOf(twoPlaces.format(val));
    }

    private class SplitItem {
        String item;
        ArrayList<String> partyMembers;
        Double originalPrice;

        public SplitItem(String item, Double originalPrice) {
            this.item = item;
            this.originalPrice = originalPrice;
            this.partyMembers = new ArrayList<>();
        }
    }

    private void setGradient(TextView target, ArrayList<String> chosenColors) {
        int tWidth = target.getWidth();
        int tHeight = target.getHeight();
        Point size = new Point(tWidth, tHeight);
        // Initializing a new instance of GradientManager class
        mGradientManager = new GradientManager(getApplicationContext(), size);
        shader = mGradientManager.getLinearGradient(chosenColors);
        target.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        target.getPaint().setShader(shader);
    }

    private void updateSplitItemOnRemove(SplitItem si) {
        Double oldSplitAmount = si.originalPrice / (si.partyMembers.size() + 1);
        Double difference = oldSplitAmount / si.partyMembers.size();
        Double errorTotal = 0.0;
        for(int i = 0; i < si.partyMembers.size(); i++) {
            Double oldTotal = userTotals.get(si.partyMembers.get(i));
            userTotals.put(si.partyMembers.get(i), oldTotal + difference);
            Double newTotal = oldTotal + difference;
        }
    }

    private void updateSplitItemOnAdd(SplitItem si) {
        Double oldSplitAmount = si.originalPrice / (si.partyMembers.size());
        Double difference = oldSplitAmount / (si.partyMembers.size() + 1);
        for(int i = 0; i < si.partyMembers.size(); i++) {
            Double oldTotal = userTotals.get(si.partyMembers.get(i));
            userTotals.put(si.partyMembers.get(i), oldTotal - difference);
            Double newTotal = oldTotal - difference;
        }
    }

    private void updateTextView(TextView t, SplitItem si) {
        ArrayList<String> chosenColors = getColorList(si);
        setGradient(t, chosenColors);
    }

    private ArrayList<String> getColorList(SplitItem si) {
        ArrayList<String> chosenColors = new ArrayList<>();
        for(int i = 0; i < si.partyMembers.size(); i++) {
            String userColor = userColors.get(si.partyMembers.get(i));
            chosenColors.add(userColor);
        }
        if(si.partyMembers.size() == 1) {
            chosenColors.add(userColors.get(si.partyMembers.get(0)));
        } else if(si.partyMembers.size() == 0) {
            chosenColors.add("#75715E");
            chosenColors.add("#75715E");
        }
        return chosenColors;
    }

    private void updateCurrentTotal() {
        Double total = itemPriceMap.get("Subtotal");
        Double claimedTotal = 0.0;
        for(Double userTotal : userTotals.values()) {
            claimedTotal += round(userTotal);
        }
        if(claimedTotal > total) {
            Random r = new Random();
            SplitItem si = splitItems.get(currentItem);
            int index = r.nextInt(si.partyMembers.size());
            String chosenPerson = si.partyMembers.get(index);
            userTotals.put(chosenPerson, userTotals.get(chosenPerson) - 0.01);
            claimedTotal -= 0.01;
        }
        totalView.setText("Remaining: $" + round(claimedTotal) + "\n Bill total: $" + round(total) + "\n");
    }

    public void onCalculateTotals(View v) {
        Double total = itemPriceMap.get("Subtotal");
        Double claimedTotal = 0.0;
        for(Double userTotal : userTotals.values()) {
            claimedTotal += round(userTotal);
        }
        Double margin = 0.05;
        boolean largeWithinBounds = claimedTotal > total && (claimedTotal - margin) <= total;
        boolean smallWithinBounds = claimedTotal < total && (claimedTotal + margin) >= total;

        Intent i = new Intent(this, ShowTotals.class);
        i.putExtra("userTotals", userTotals);
        i.putExtra("itemPriceMap", itemPriceMap);
        i.putExtra("userColors", userColors);
        startActivity(i);

    }
}
