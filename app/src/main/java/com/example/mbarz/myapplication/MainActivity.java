package com.example.mbarz.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.SubscriptSpan;
import android.view.*;
import android.widget.*;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;
    String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    private String dateForDB = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    private final String COLOR_WHITE = "white";
    private final String COLOR_GREEN = "green";
    private final int ADD_TABLEROW = 0;
    private final int SET_FOCUS = 1;
    private final int AUTO_FILL = 2;
    private final int FOCUS = 3;
    private final int SET_CONTENT = 4;
    private final int SET_DATE_LISTENER = 5;
    private final int TOAST = 6;
    private final int ADD_VIEW = 7;
    private final int SET_TEXT = 8;
    private final int CLEAR_TABLE = 9;
    TableRow.LayoutParams tableParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT, 1.0f);
    TableRow createdRow;

    List<String> fields = new ArrayList<>(Arrays.asList(
            "Рбуф",
            "Рзатр",
            "Ркільц",
            "Рлін"
    ));

    Map<String, Integer> hourMarkedFields = new HashMap<>();
    Handler handler;
    Message msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        dateForDB = format.format(new Date());
        ((Button) findViewById(R.id.date)).setText(date);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case ADD_TABLEROW:
                        TableLayout table = findViewById(R.id.main_table);
                        table.addView((TableRow)msg.obj);
                        break;
                    case SET_FOCUS:
                        ((EditText)msg.obj).requestFocus();
                        break;
                    case AUTO_FILL:
                        ((EditText) findViewById(msg.arg1 * 10 + msg.arg2)).setText((String)msg.obj);
                        break;
                    case FOCUS:
                        findViewById(msg.arg1 * 10 + msg.arg2).requestFocus();
                        break;
                    case SET_CONTENT:
                        setContentView(R.layout.calendar);
                        break;
                    case SET_DATE_LISTENER:
                        CalendarView calendarView = findViewById(R.id.simpleCalendarView);
                        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

                            @Override
                            public void onSelectedDayChange(CalendarView view, int year,
                                                            int month, int dayOfMonth) {
                                int mYear = year;
                                String mMonth = month > 8 ? String.valueOf(month + 1) : 0 + String.valueOf(month + 1);
                                String mDay = dayOfMonth > 9 ? String.valueOf(dayOfMonth) : 0 + String.valueOf(dayOfMonth);
                                String selectedDate = new StringBuilder().append(mYear)
                                        .append("-").append(mMonth).append("-").append(mDay).toString();
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                                String today = format.format(new Date());
                                if (selectedDate.compareTo(today) > 0) {
                                    Toast.makeText(getApplicationContext(), "Обирайте дату не пізнішу сьогоднішньої!",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    date = new StringBuilder().append(mDay)
                                            .append("-").append(mMonth).append("-").append(mYear).toString();
                                    Toast.makeText(getApplicationContext(), date, Toast.LENGTH_LONG).show();
                                    setContentView(R.layout.activity_main);
                                    (
                                            (Button) findViewById(R.id.date)).setText(date);
                                    updateMainTable(selectedDate);
                                }
                            }
                        });
                        break;
                    case TOAST:
                        Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
                        break;
                    case ADD_VIEW:
                        ((LinearLayout) findViewById(R.id.fields)).addView((TextView)msg.obj);
                        break;
                    case SET_TEXT:
                        ((EditText) findViewById(msg.arg1)).setText((String)msg.obj);
                        break;
                    case CLEAR_TABLE:
                        ((TableLayout) findViewById(R.id.main_table)).removeAllViews();
                }
            }
        };

                updateMainTable(dateForDB);
    }

    public void createFieldNames() {
        TextView field;
        for(int i = 0; i < fields.size() + 1; i++) {
            String nameField;
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
            sendToHandler(ADD_VIEW, field);
        }
    }

    public void updateMainTable(final String dateForDB) {
        Thread update = new Thread(new Runnable() {
            @Override
            public void run() {
                createFieldNames();
                Map<String, List<String>> data = getStringListMap(new String(dateForDB));
                createTableInUI(data.isEmpty() ? null : data);
            }
        });
        update.start();
    }

    private void sendToHandler(int set_date, Object object) {
        msg = handler.obtainMessage(set_date, object);
        handler.sendMessage(msg);
    }

    private void sendToHandler(int set_date, int arg1, int arg2, Object object) {
        msg = handler.obtainMessage(set_date, arg1, arg2, object);
        handler.sendMessage(msg);
    }

    private Map<String, List<String>> getStringListMap(String dateForDB) {
        Map<String, List<String>> data = new HashMap<>();
        database = openOrCreateDatabase("data", MODE_PRIVATE, null);
//        database.execSQL("drop table DataValues");
//        database.execSQL("drop table Notes");
        createTablesInDB();
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
        return data;
    }

    private void createTablesInDB() {
        database.execSQL("CREATE TABLE IF NOT EXISTS DataValues(date DATE, hour INT, Pbuf FLOAT, " +
                "Pzatr FLOAT, Pkil FLOAT, Plin FLOAT, color VARCHAR);");
        database.execSQL("CREATE TABLE IF NOT EXISTS Notes(date DATE, note TEXT, recommend TEXT);");
    }

    public void createTableInUI(Map<String, List<String>> data) {
        for(int hour = 1; hour < 25; hour++) {
            createdRow = getTableRow();
            for (int column = 0; column < fields.size() + 1; column++) {
                EditText field = getEditText(tableParams, hour, column);
                field.setBackgroundResource(getBackgroundResid(data, hour));
                field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                field.setOnCreateContextMenuListener(this);
                field.setText(getText(data, hour, column, field));
                createdRow.addView(field);
            }
            sendToHandler(ADD_TABLEROW, createdRow);
        }
        fillNotateAndRecommend(data, 5, 25);
        fillNotateAndRecommend(data, 6, 26);
        sendToHandler(FOCUS, 1, 1, null);
    }

    private void fillNotateAndRecommend(Map<String, List<String>> data, int positionInResultSet, int hour) {
        if (data != null && data.get("1").get(positionInResultSet) != null) {
            EditText text = getEditText(tableParams, hour, 0);
            text.setText(getText(data, 1, positionInResultSet + 1, text));
            createdRow = getTableRow();
            createdRow.addView(text);
            sendToHandler(ADD_TABLEROW, createdRow);
        }
    }

    private TableRow getTableRow() {
        TableRow row = new TableRow(this);
        row.setLayoutParams(tableParams);
        row.setOrientation(TableRow.HORIZONTAL);
        return row;
    }

    private Integer getBackgroundResid(Map<String, List<String>> data, int hour) {
        if (data == null || data.get(String.valueOf(hour)).get(4).equals(COLOR_WHITE)) {
            return R.drawable.back;
        }
        else if (data.get(String.valueOf(hour)).get(4).equals(COLOR_GREEN)) {
            hourMarkedFields.put(String.valueOf(hour), 1);
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
        return field;
    }

    private String getText(Map<String, List<String>> data, int hour, int column, EditText field) {
        if (column == 0) {
            field.setEnabled(false);
            return String.valueOf(hour / 18 * (-24)+ hour + 7);
        }
        if (data == null) {
            return "0";
        }
        return data.get(String.valueOf(hour)).get(column - 1);
    }

    public void onClickCalendar(View v) {
        sendToHandler(SET_CONTENT, null);
        sendToHandler(SET_DATE_LISTENER, null);
    }

    public void onClickSaveData(View v) {
        final View view = v;
        Thread save = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] dateElements = ((Button) findViewById(R.id.date)).getText().toString().split("-");
                String dateForDB = dateElements[2] + "-" + dateElements[1] + "-" + dateElements[0];
                database = openOrCreateDatabase("data", MODE_PRIVATE, null);
                createTablesInDB();
                database.execSQL("DELETE FROM DataValues WHERE date=" + dateForDB);
                database.execSQL("DELETE FROM Notes WHERE date=" + dateForDB);
                List<String> row = new ArrayList<>();
                EditText field;
                for (int hour = 1; hour < 25; hour++) {
                    for (int column = 0; column < fields.size() + 1; column++) {
                        field = findViewById((hour) * 10 + column);
                        row.add(field.getText().toString());
                    }
                    String backColor = hourMarkedFields.get(String.valueOf(hour)) == null ? COLOR_WHITE : COLOR_GREEN;
                    row.add(backColor);
                    String query = "INSERT INTO DataValues VALUES(" + dateForDB + "," + hour + "," + row.get(1) + "," +
                            row.get(2) + "," + row.get(3) + "," + row.get(4) + ",'" + row.get(5) + "');";
                    database.execSQL(query);
                    row.clear();
                }
                EditText note = findViewById(25 * 10);
                EditText recommendation = findViewById(26 * 10);
                if (note != null || recommendation != null) {
                    String stringNote = note != null ? note.getText().toString() : "Примітка:\n";
                    String stringRecommend = recommendation != null ? recommendation.getText().toString() : "Пропозиції щодо раціоналізації:\n";
                    String query2 = "INSERT INTO Notes VALUES(" + dateForDB + ",'" + stringNote +
                            "','" + stringRecommend + "');";
                    database.execSQL(query2);
                }

                database.close();
                hourMarkedFields.clear();
                onClickCalendar(view);
                sendToHandler(TOAST, "Дані збережено");
            }
        });
        save.start();
    }

    //options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_autoFill:
                Thread fillData = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        autoFillData();
                        sendToHandler(TOAST, "Дані заповнено");
                    }
                });
                fillData.start();
                break;
            case R.id.menu_note:
                if (findViewById(25 * 10) != null) {
                    findViewById(25 * 10).requestFocus();
                    break;
                } else {
                    EditText note = getEditText(tableParams, 25, 0);
                    note.setBackgroundResource(R.drawable.back);
                    note.setText("Примітка:\n");
                    createdRow = getTableRow();
                    createdRow.addView(note);
                    sendToHandler(ADD_TABLEROW, createdRow);
                    sendToHandler(SET_FOCUS, note);
                    Toast.makeText(getApplicationContext(), "Додано примітку", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_recommendation:
                if (findViewById(26 * 10) != null) {
                    sendToHandler(SET_FOCUS, findViewById(26 * 10));
                    break;
                } else {
                    EditText recommendation = getEditText(tableParams, 26, 0);
                    recommendation.setBackgroundResource(R.drawable.back);
                    recommendation.setText("Пропозиції щодо раціоналізації:\n");
                    createdRow = getTableRow();
                    createdRow.addView(recommendation);
                    sendToHandler(ADD_TABLEROW, createdRow);
                    sendToHandler(SET_FOCUS, recommendation);
                    Toast.makeText(getApplicationContext(), "Додано пропозицію з раціоналізації", Toast.LENGTH_LONG).show();
                    break;
                }
            case R.id.menu_deleteAll:
                Thread clearAll = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendToHandler(CLEAR_TABLE, null);
                        createTableInUI(null);
                        hourMarkedFields.clear();
                        sendToHandler(TOAST, "Всі записи видалено");
                    }
                });
                clearAll.start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void autoFillData() {
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
                    sendToHandler(AUTO_FILL, hour, column, row.get(column - 1));
                }
            }
        }
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
                markRecord(1);
                Toast.makeText(getApplicationContext(), "Відмічено запис", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_deleteOne:
                clearOneRecord();
                markRecord(null);
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
                sendToHandler(AUTO_FILL, hourFocus, column, "0");
            }
        }
    }

    private boolean checkFocus() {
        return getCurrentFocus() != null || getCurrentFocus().getClass().equals(EditText.class);
    }

    private void markRecord(Integer color) {
        if(checkFocus()) {
            EditText field = (EditText) getCurrentFocus();
            int hourFocus = (field.getId()) / 10;
            for (int column = 0; column < fields.size() + 1; column++) {
                findViewById(hourFocus * 10 + column).setBackgroundResource(color == null ?
                        R.drawable.back : R.drawable.mark_field);
            }
            hourMarkedFields.put(String.valueOf(hourFocus), color);
        }
    }
}
