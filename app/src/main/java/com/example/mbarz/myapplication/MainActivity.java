package com.example.mbarz.myapplication;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.SubscriptSpan;
import android.view.*;
import android.widget.*;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

//    CheckBox chb;
    SQLiteDatabase database;
    String date = null;
    String dateForDB = null;
    final String COLOR_WHITE = "white";
    final String COLOR_GREEN = "green";
    TableRow.LayoutParams tableParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.MATCH_PARENT, 1.0f);

    List<String> fields = new ArrayList<>(Arrays.asList(
            "Рбуф",
            "Рзатр",
            "Ркільц",
            "Рлін"
    ));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDate();
        updateMainTable(dateForDB);

    }

    public void createFieldNames() {
        LinearLayout layout = findViewById(R.id.fields);
//        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        TextView field;
        for(int i = 0; i < fields.size() + 1; i++) {
            String nameField = null;
            SpannableStringBuilder sb = null;
            if (i != 0) {
                nameField = fields.get(i - 1);
                sb = new SpannableStringBuilder(nameField);
                sb.setSpan(new SubscriptSpan(), 1, nameField.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            field = new TextView(this);
            field.setLayoutParams(tableParams);
            field.setText(i == 0 ? "Години" : sb, TextView.BufferType.SPANNABLE);
            field.setEms(10);
            field.setGravity(Gravity.CENTER);
            field.setBackgroundResource(R.drawable.back);
            layout.addView(field);
        }
    }

    public void updateMainTable(String dateForDB) {
        setContentView(R.layout.activity_main);

        createFieldNames();
        Map<String, List<String>> data = new HashMap<>();
        database = openOrCreateDatabase("data", MODE_PRIVATE, null);
//        database.execSQL("drop table DataValues");
//        database.execSQL("drop table Notes");
        database.execSQL("CREATE TABLE IF NOT EXISTS DataValues(date DATE, hour INT, Pbuf FLOAT, " +
                "Pzatr FLOAT, Pkil FLOAT, Plin FLOAT, Color VARCHAR);");
        database.execSQL("CREATE TABLE IF NOT EXISTS Notes(date DATE, note TEXT, recommend TEXT);");
        Cursor resultSet = database.rawQuery("Select * from DataValues left join Notes on " +
                "DataValues.date=Notes.date where DataValues.date=" + dateForDB,null);
        resultSet.moveToFirst();
        while (resultSet.isAfterLast() == false) {
            data.put(String.valueOf(resultSet.getInt(1)),
                    Arrays.asList(resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getString(5),
                            resultSet.getString(6),
                            resultSet.getString(8),
                            resultSet.getString(9)));
            resultSet.moveToNext();
        }
        resultSet.close();
        database.close();
        ((Button) findViewById(R.id.date)).setText(date);
            createTable(data.isEmpty() ? null : data);
            return;
    }

    public void getDate() {
        if (date != null) {
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        date = format.format(new Date());
        format = new SimpleDateFormat("yyyy-MM-dd");
        dateForDB = format.format(new Date());
        ((Button) findViewById(R.id.date)).setText(date);
    }

    public void createTable(Map<String, List<String>> data) {
        TableLayout table = findViewById(R.id.main_table);
//        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
//                TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        for(int hour = 1; hour < 25; hour++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(tableParams);
            row.setOrientation(TableRow.HORIZONTAL);
            for (int column = 0; column < fields.size() + 1; column++) {
                EditText field = getEditText(tableParams, hour, column);
                field.setBackgroundResource(getBackgroundResid(data, hour));
                field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                field.setOnCreateContextMenuListener(this);
                field.setText(getText(data, hour, column, field));
                row.addView(field);
            }
            table.addView(row);
        }
        if (data != null && data.get("1").get(5) != null) {
            EditText note = getEditText(tableParams, 25, 0);
            note.setText(getText(data, 1, 6, note));
            table.addView(note);
        }
        if (data != null && data.get("1").get(6) != null) {
            EditText recommendation = getEditText(tableParams, 26, 0);
            recommendation.setText(getText(data, 1, 7, recommendation));
            table.addView(recommendation);
        }
    }

    private Integer getBackgroundResid(Map<String, List<String>> data, int hour) {
        if (data == null || data.get(String.valueOf(hour)).get(4).equals(COLOR_WHITE)) {
            return R.drawable.back;
        }
        else if (data.get(String.valueOf(hour)).get(4).equals(COLOR_GREEN)) {
            return R.drawable.mark_field;
        } else {
            return R.drawable.back;
        }
    }

    private EditText getEditText(TableRow.LayoutParams rowParams, int hour, int column) {
        EditText field = new EditText(this);
        field.setLayoutParams(rowParams);
        field.setId(hour * 10 + column);
        field.setEms(10);
        field.setGravity(Gravity.CENTER);
//        field.setBackgroundResource(R.drawable.back);
        return field;
    }

    private String getText(Map<String, List<String>> data, int hour, int column, EditText field) {
//        if (hour == 25 || hour == 26) {
//            return "";
//        }
        if (column == 0) {
            field.setEnabled(false);
            return String.valueOf(hour);
        }
        if (data == null) {
            return "0";
        }
        return data.get(String.valueOf(hour)).get(column - 1);
    }

    public void onClickCalendar(View v) {
        setContentView(R.layout.calendar);
        CalendarView calendarView = findViewById(R.id.simpleCalendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year,
                                            int month, int dayOfMonth) {
                int mYear = year;
                String mMonth = month > 8 ? String.valueOf(month + 1) : 0 + String.valueOf(month + 1);
                String mDay = dayOfMonth > 9 ? String.valueOf(dayOfMonth) : 0 + String.valueOf(dayOfMonth);
                String selectedDate = new StringBuilder().append(mDay)
                        .append("-").append(mMonth).append("-").append(mYear).toString();
                Toast.makeText(getApplicationContext(), selectedDate, Toast.LENGTH_LONG).show();
                setContentView(R.layout.activity_main);
                date = selectedDate;
                String dateForDB = new StringBuilder().append(mYear)
                        .append("-").append(mMonth).append("-").append(mDay).toString();
                updateMainTable(dateForDB);
            }
        });
    }

    public void onClickSaveData(View v) {
        String[] dateElements = ((Button)findViewById(R.id.date)).getText().toString().split("-");
        String dateForDB = dateElements[2] + "-" + dateElements[1] + "-" + dateElements[0];
        database = openOrCreateDatabase("data", MODE_PRIVATE, null);
//        database.execSQL("drop table DataValues");
        database.execSQL("CREATE TABLE IF NOT EXISTS DataValues(date DATE, hour INT, Pbuf FLOAT, " +
                "Pzatr FLOAT, Pkil FLOAT, Plin FLOAT, Color VARCHAR);");
        database.execSQL("CREATE TABLE IF NOT EXISTS Notes(date DATE, note TEXT, recommend TEXT);");
        database.execSQL("DELETE FROM DataValues WHERE date=" + dateForDB);
        database.execSQL("DELETE FROM Notes WHERE date=" + dateForDB);
        List<String> row = new ArrayList<>();
        EditText field = null;
        for(int hour = 1; hour < 25; hour++) {
            for(int column = 0; column < fields.size() + 1; column++) {
                field = findViewById((hour) * 10 + column);
                row.add(field.getText().toString());
            }
            row.add(field.getBackground().getConstantState().equals(
                    getResources().getDrawable(R.drawable.back).getConstantState()) ? COLOR_WHITE : COLOR_GREEN);
            String query = "INSERT INTO DataValues VALUES(" + dateForDB + "," + hour + "," + row.get(1) + "," +
                    row.get(2) + "," + row.get(3) + "," + row.get(4) + ",'" + row.get(5) + "');";
            database.execSQL(query);
            row.clear();
        }
        EditText note = findViewById(25 * 10);
        EditText recommendation = findViewById(26 * 10);
        String stringNote = note != null ? note.getText().toString() : "";
        String stringRecommend = recommendation != null ? recommendation.getText().toString() : "";
        String query2 = "INSERT INTO Notes VALUES(" + dateForDB + ",'" + stringNote +
                "','" + stringRecommend + "');";
        database.execSQL(query2);
        database.close();
        Toast.makeText(getApplicationContext(), "Дані збережено", Toast.LENGTH_LONG).show();
        onClickCalendar(v);
    }

    //options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.setGroupVisible(R.id.group1, chb.isChecked());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //todo switch
        TableLayout table = findViewById(R.id.main_table);
        switch (item.getItemId()) {
            case R.id.menu_autoFill:
                fillData();
                Toast.makeText(getApplicationContext(), "Дані заповнено", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_note:
                if (findViewById(25 * 10) != null) {
                    findViewById(25 * 10).requestFocus();
                    break;
                } else {
//                    TableRow.LayoutParams tableParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
//                            TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
                    EditText note = getEditText(tableParams, 25, 0);
                    note.setText("Примітка:\n");
                    table.addView(note);
                    note.requestFocus();
                    Toast.makeText(getApplicationContext(), "Додано примітку", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_recommendation:
                if (findViewById(26 * 10) != null) {
                    findViewById(26 * 10).requestFocus();
                    break;
                } else {
//                TableRow.LayoutParams tableParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
//                        TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
                    EditText recommendation = getEditText(tableParams, 26, 0);
                    recommendation.setText("Пропозиції щодо раціоналізації:\n");
                    table.addView(recommendation);
                    recommendation.requestFocus();
                    Toast.makeText(getApplicationContext(), "Додано пропозицію з раціоналізації", Toast.LENGTH_LONG).show();
                    break;
                }
            case R.id.menu_deleteAll:
                table.removeAllViews();
                createTable(null);
                Toast.makeText(getApplicationContext(), "Всі записи видалено", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillData() {
        if(checkFocus()) {
            EditText field = (EditText) getCurrentFocus();
            int hourFocus = (field.getId()) / 10;
            if (hourFocus < 1 || hourFocus > 24) {
                return;
            }
            List<String> row = new ArrayList<>();
            for (int column = 1; column < fields.size() + 1; column++) {
                row.add(((EditText) findViewById(hourFocus * 10 + column)).getText().toString());
            }
            for(int hour = hourFocus + 1; hour < 25; hour++) {
                for(int column = 1; column < fields.size() + 1; column++) {
                    ((EditText) findViewById(hour * 10 + column)).setText(row.get(column - 1));
                }
            }
        }
        return;
    }

    //context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_mark:
                markRecord();
                Toast.makeText(getApplicationContext(), "Відмічено запис", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_deleteOne:
                clearOneRecord();
                Toast.makeText(getApplicationContext(), "Запис видалено", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void clearOneRecord() {
        if(checkFocus()) {
            EditText field = (EditText) getCurrentFocus();
            int hourFocus = (field.getId()) / 10;
            for (int column = 1; column < fields.size() + 1; column++) {
                ((EditText) findViewById(hourFocus * 10 + column)).setText("0");
            }
        }
    }

    private boolean checkFocus() {
        return getCurrentFocus() != null || getCurrentFocus().getClass().equals(EditText.class);
    }

    private void markRecord() {
        if(checkFocus()) {
            EditText field = (EditText) getCurrentFocus();
            int hourFocus = (field.getId()) / 10;
            for (int column = 1; column < fields.size() + 1; column++) {
                findViewById(hourFocus * 10 + column).setBackgroundResource(R.drawable.mark_field);
            }
        }
    }
}
