package com.example.mbarz.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.SubscriptSpan;
import android.view.*;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;
    String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    private String dateForDB = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    private int number_table;
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

    List<String> fields1 = new ArrayList<>(Arrays.asList(
            "Рбуф",
            "Рзатр",
            "Ркільц",
            "Рлін"
    ));

    List<String> fields2 = new ArrayList<>(Arrays.asList(
            "Глибина спуску/підйому",
            "Виконані роботи"
    ));

    List<String> fields3 = new ArrayList<>(Arrays.asList(
            "Тиск на трапі",
            "Тиск на лінії",
            "Продувка сепаратора",
            "№ Збірника",
            "Стан у збірнику",
            "Відкачано рідини",
            "Початок відкачки",
            "Кінець відкачки",
            "Поступило за зміну",
            "Звідки відбулось поступлення"
    ));

    List<String> currentFields = new ArrayList<>();
    private int idDate;
    private int idFields;
    private int idTable;
    private int currentLayout;

    Map<String, String> hourMarkedFields = new HashMap<>();
    Handler handler;
    Message msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        dateForDB = format.format(new Date());
//        ((Button) findViewById(idDate)).setText(date);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case ADD_TABLEROW:
                        TableLayout table = findViewById(idTable);
                        table.addView((TableRow)msg.obj);
                        break;
                    case SET_FOCUS:
                        ((EditText)msg.obj).requestFocus();
                        break;
                    case AUTO_FILL:
                        ((EditText) findViewById(generateId( msg.arg1, msg.arg2))).setText((String)msg.obj);
                        break;
                    case FOCUS:
                        findViewById(generateId( msg.arg1, msg.arg2)).requestFocus();
                        break;
                    case SET_CONTENT:
                        setContentView(currentLayout);
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
                                    currentLayout = getLayoutByNumberTable(number_table);
                                    setContentView(currentLayout);
                                    ((Button) findViewById(idDate)).setText(date);
                                    updateMainTable(selectedDate);
                                }
                            }
                        });
                        break;
                    case TOAST:
                        Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
                        break;
                    case ADD_VIEW:
                        ((LinearLayout) findViewById(idFields)).addView((TextView)msg.obj);
                        break;
                    case SET_TEXT:
                        ((Button) findViewById(msg.arg1)).setText((String)msg.obj);
                        break;
                    case CLEAR_TABLE:
                        ((TableLayout) findViewById(idTable)).removeAllViews();
                }
            }
        };

    }

    private int getLayoutByNumberTable(int number) {
        switch (number) {
            case 1:
                return R.layout.table1;
            case 2:
                return R.layout.table2;
            case 3:
                return R.layout.table3;
        }
        return 0;
    }

    private void sendToHandler(int set_data, Object object) {
        msg = handler.obtainMessage(set_data, object);
        handler.sendMessage(msg);
    }

    private void sendToHandler(int set_data, int arg1, int arg2, Object object) {
        msg = handler.obtainMessage(set_data, arg1, arg2, object);
        handler.sendMessage(msg);
    }

    public void updateMainTable(final String dateForDB) {
        Thread update = new Thread(new Runnable() {
            @Override
            public void run() {
                sendToHandler(SET_TEXT, idDate, 0, date);
                createFieldNames();
                Map<String, List<String>> data = getStringListMap(new String(dateForDB));
                createTableInUI(data.isEmpty() ? null : data);
            }
        });
        update.start();
    }

    public void createFieldNames() {
        TextView field;
        createdRow = getTableRow();
        for(int i = 0; i < currentFields.size() + 1; i++) {
            String nameField;
            SpannableStringBuilder sb = null;
            if (number_table == 1 && i != 0) {
                nameField = currentFields.get(i - 1);
                sb = new SpannableStringBuilder(nameField);
                sb.setSpan(new SubscriptSpan(), 1, nameField.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            field = new TextView(this);
            field.setLayoutParams(tableParams);
            if (i != 0) {
                if (number_table == 1) {
                    field.setText(sb, TextView.BufferType.SPANNABLE);
                } else {
                    field.setText(currentFields.get(i - 1));
                }
            } else {
                field.setText("Години");
            }
            field.setEms(10);
            field.setGravity(Gravity.CENTER);
            field.setBackgroundResource(R.drawable.back);
            if (number_table != 3) {
                sendToHandler(ADD_VIEW, field);
            } else {
                createdRow.addView(field);
            }
        }
        if (number_table == 3) {
            sendToHandler(ADD_TABLEROW, createdRow);
        }
    }

    private Map<String, List<String>> getStringListMap(String dateForDB) {
        Map<String, List<String>> data = new HashMap<>();
        database = openOrCreateDatabase("data", MODE_PRIVATE, null);
//        database.execSQL("drop table DataValues1");
//        database.execSQL("drop table Notes1");
        createTablesInDB();
        Cursor resultSet = database.rawQuery("Select * from DataValues" + number_table + " as dv left join Notes" +
                number_table + " as n on dv.date=n.date where dv.date=" + dateForDB,null);
        if (resultSet.getCount() == 0) {
            return data;
        }
        resultSet.moveToFirst();
        int size = currentFields.size();
        List<String> valuesFromDB = new ArrayList<>();
        while (resultSet.isAfterLast() == false) {
            for (int i = 1; i < size + 7; i++) {
                if (i == size + 3) {
                    continue;
                } else {
                    valuesFromDB.add(resultSet.getString(i));
                }
            }
            data.put(valuesFromDB.get(0), new ArrayList(valuesFromDB));
            valuesFromDB.clear();
            resultSet.moveToNext();
        }
        resultSet.close();
        database.close();
        return data;
    }

    private void createTablesInDB() {
        switch (number_table) {
            case 1:
            database.execSQL("CREATE TABLE IF NOT EXISTS DataValues1(date DATE, hour INT, Pbuf FLOAT, " +
                    "Pzatr FLOAT, Pkil FLOAT, Plin FLOAT, color VARCHAR);");
            database.execSQL("CREATE TABLE IF NOT EXISTS Notes1(date DATE, note TEXT, recommend TEXT, author TEXT);");
            break;
            case 2:
            database.execSQL("CREATE TABLE IF NOT EXISTS DataValues2(date DATE, hour INT, " +
                    "Deep FLOAT, Tasks TEXT, color VARCHAR);");
            database.execSQL("CREATE TABLE IF NOT EXISTS Notes2(date DATE, note TEXT, recommend TEXT, author TEXT);");
            break;
            case 3:
            database.execSQL("CREATE TABLE IF NOT EXISTS DataValues3(date DATE, hour INT, Ptrap FLOAT, " +
                    "Plin FLOAT, blowing TEXT, number INT, state TEXT, pumped FLOAT, start TEXT, finish TEXT, " +
                    "arrived FLOAT, arrivedFrom TEXT, color VARCHAR);");
            database.execSQL("CREATE TABLE IF NOT EXISTS Notes3(date DATE, note TEXT, recommend TEXT, author TEXT);");
            break;
        }
    }

    public void createTableInUI(Map<String, List<String>> data) {
        for(int hour = 1; hour < 25; hour++) {
            createdRow = getTableRow();
            for (int column = 0; column < currentFields.size() + 1; column++) {
                EditText field = getObjectEditText(tableParams, hour, column);
                field.setBackgroundResource(getBackgroundResId(data, hour));
//                field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                field.setOnCreateContextMenuListener(this);
                field.setText(getText(data, hour, column, field));
                createdRow.addView(field);
            }
            sendToHandler(ADD_TABLEROW, createdRow);
        }
        int positionNote = currentFields.size() + 1;
        fillSummaryNotes(data, positionNote, positionNote, 25);
        fillSummaryNotes(data, positionNote, positionNote + 1, 26);
        fillSummaryNotes(data, positionNote, positionNote + 2, 27);

        sendToHandler(FOCUS, 1, 1, null);
    }

    private void fillSummaryNotes(Map<String, List<String>> data, int positionNote, int positionInResultSet, int hour) {
        EditText text = getObjectEditText(tableParams, hour, 0);
        if (data != null) {
            text.setText(getText(data, 1, positionInResultSet + 1, text));
        } else {
            if (positionInResultSet == positionNote + 2) {
                text.setText("Виконавець:\n");
            } else {
                text.setText(positionInResultSet == positionNote ? "Примітка:\n" : "Пропозиції щодо раціоналізації:\n");
            }
        }
        createdRow = getTableRow();
        createdRow.addView(text);
        sendToHandler(ADD_TABLEROW, createdRow);
    }

    private TableRow getTableRow() {
        TableRow row = new TableRow(this);
        row.setLayoutParams(tableParams);
        row.setOrientation(TableRow.HORIZONTAL);
        return row;
    }

    private Integer getBackgroundResId(Map<String, List<String>> data, int hour) {
        int positionColor = currentFields.size() + 1;
        if (data == null || data.get(String.valueOf(hour)).get(positionColor).equals(COLOR_WHITE)) {
            return R.drawable.back;
        }
        else if (data.get(String.valueOf(hour)).get(positionColor).equals(COLOR_GREEN)) {
            hourMarkedFields.put(String.valueOf(hour), COLOR_GREEN);
            return R.drawable.mark_field;
        } else {
            return R.drawable.back;
        }
    }

    private EditText getObjectEditText(TableRow.LayoutParams rowParams, int hour, int column) {
        EditText field = new EditText(this);
        field.setLayoutParams(rowParams);
        field.setId(number_table * 1000 + hour * 10 + column);
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
        return data.get(String.valueOf(hour)).get(column);
    }

    public void onClickCalendar(View v) {
        currentLayout = R.layout.calendar;
        sendToHandler(SET_CONTENT, null);
        sendToHandler(SET_DATE_LISTENER, null);
    }

    public void onClickTable(View v) {
        switch (v.getId()) {
            case R.id.button_table1:
                number_table = 1;
                idDate = R.id.date1;
                idFields = R.id.fields1;
                idTable = R.id.main_table1;
                currentLayout = R.layout.table1;
                currentFields = fields1;
                break;
            case R.id.button_table2:
                number_table = 2;
                idDate = R.id.date2;
                idFields = R.id.fields2;
                idTable = R.id.main_table2;
                currentLayout = R.layout.table2;
                currentFields = fields2;
                break;
            case R.id.button_table3:
                number_table = 3;
                idDate = R.id.date3;
                idFields = R.id.fields3;
                idTable = R.id.main_table3;
                currentLayout = R.layout.table3;
                currentFields = fields3;
                break;
        }
        sendToHandler(SET_CONTENT, null);
        updateMainTable(dateForDB);
    }

    public void onClickSaveData(View v) {
        final View view = v;
        Thread save = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] dateElements = ((Button) findViewById(idDate)).getText().toString().split("-");
                String dateForDB = dateElements[2] + "-" + dateElements[1] + "-" + dateElements[0];
                database = openOrCreateDatabase("data", MODE_PRIVATE, null);
                createTablesInDB();
                database.execSQL("DELETE FROM DataValues" + number_table + " WHERE date=" + dateForDB);
                database.execSQL("DELETE FROM Notes" + number_table + " WHERE date=" + dateForDB);
                List<String> row = new ArrayList<>();
                EditText field;
                for (int hour = 1; hour < 25; hour++) {
                    for (int column = 1; column < currentFields.size() + 1; column++) {
                        field = findViewById(generateId(hour, column));
                        row.add(field.getText().toString());
                    }
                    String backColor = hourMarkedFields.get(String.valueOf(hour));
                    row.add(backColor == null ? COLOR_WHITE : backColor);
                    String query = "INSERT INTO DataValues" + number_table + " VALUES(" + dateForDB + "," + hour + ",'";
                    for (int i = 0; i < currentFields.size(); i++) {
                        query += row.get(i) + "','";
                    }
                    query += row.get(currentFields.size()) + "');";
                    database.execSQL(query);
                    row.clear();
                }
                EditText note = findViewById(generateId(25, 0));
                EditText recommendation = findViewById(generateId(26, 0));
                EditText author = findViewById(generateId(27, 0));
                    String stringNote = note.getText().toString();
                    String stringRecommend = recommendation.getText().toString();
                    String stringAuthor = author.getText().toString();
                    String query2 = "INSERT INTO Notes" + number_table + " VALUES(" + dateForDB + ",'" + stringNote +
                            "','" + stringRecommend + "','" + stringAuthor + "');";
                    database.execSQL(query2);

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
        EditText text;
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
                text = findViewById(generateId(25, 0));
                text.requestFocus();
                text.setSelection(text.getText().length());
                break;
            case R.id.menu_recommendation:
                text = findViewById(generateId(26, 0));
                text.requestFocus();
                text.setSelection(text.getText().length());
                break;
            case R.id.menu_author:
                text = findViewById(generateId(27, 0));
                text.requestFocus();
                text.setSelection(text.getText().length());
                break;
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
            case R.id.menu_tables:
                currentLayout = R.layout.activity_main;
                sendToHandler(SET_CONTENT, null);
        }
        return super.onOptionsItemSelected(item);
    }

    private void autoFillData() {
        if(checkFocus()) {
            EditText field = (EditText) getCurrentFocus();
            int hourFocus = ((field.getId()) % 1000) / 10;
            if (hourFocus < 1 || hourFocus > 24) {
                return;
            }
            List<String> row = new ArrayList<>();
            for (int column = 1; column < currentFields.size() + 1; column++) {
                row.add(((EditText) findViewById(generateId(hourFocus, column))).getText().toString());
            }
            for(int hour = hourFocus + 1; hour < 25; hour++) {
                for(int column = 1; column < currentFields.size() + 1; column++) {
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
                markRecord(COLOR_GREEN);
                Toast.makeText(getApplicationContext(), "Відмічено запис", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_deleteOne:
                clearOneRecord();
                markRecord(COLOR_WHITE);
                Toast.makeText(getApplicationContext(), "Запис видалено", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void clearOneRecord() {
        if(checkFocus()) {
            EditText field = (EditText) getCurrentFocus();
            int hourFocus = ((field.getId()) % 1000) / 10;
            for (int column = 1; column < currentFields.size() + 1; column++) {
                sendToHandler(AUTO_FILL, hourFocus, column, "0");
            }
        }
    }

    private boolean checkFocus() {
        return getCurrentFocus() != null || getCurrentFocus().getClass().equals(EditText.class);
    }

    private void markRecord(String color) {
        if(checkFocus()) {
            EditText field = (EditText) getCurrentFocus();
            int hourFocus = ((field.getId()) % 1000) / 10;
            for (int column = 0; column < currentFields.size() + 1; column++) {
                findViewById(generateId(hourFocus, column)).setBackgroundResource(color == null || color == "white" ?
                        R.drawable.back : R.drawable.mark_field);
            }
            hourMarkedFields.put(String.valueOf(hourFocus), color);
        }
    }

    private int generateId(int x, int y) {
        return number_table * 1000 + x * 10 + y;
    }
}
