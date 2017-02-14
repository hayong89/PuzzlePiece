package bumbums.puzzlepiece.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.BarHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import bumbums.puzzlepiece.R;
import bumbums.puzzlepiece.model.Puzzle;
import bumbums.puzzlepiece.util.CustomMarkerView;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by han sb on 2017-02-14.
 */

public class GraphFragment extends android.support.v4.app.Fragment {
    private LineChart mChart;
    private TextView mYearMonth;
    private Calendar mToday;

    public static final int X_COUNT = 8;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        mChart = (LineChart) view.findViewById(R.id.chart);
        mYearMonth = (TextView)view.findViewById(R.id.tv_graph_year_month);


        initDate();
        String yearMonth = String.format("%4d. %02d",mToday.get(Calendar.YEAR),mToday.get(Calendar.MONTH)+1);
        mYearMonth.setText(yearMonth);
        //getDataFromDB();

        //getXDays();
        initGraph();
        return view;


    }

    public void initDate() {
        mToday = Calendar.getInstance();
        mToday.set(Calendar.HOUR_OF_DAY,0);
        mToday.set(Calendar.MINUTE,0);
        mToday.set(Calendar.SECOND,0);
        mToday.set(Calendar.MILLISECOND,0);
       // Log.d("###",cal.toString());
    }


//highlight

    public void initGraph() {

        LineDataSet dataSet = new LineDataSet(getDataFromDB(), "퍼즐수");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);



        //dataSet.setColor();
        //dataSet.setValueTextColor();
        LineData lineData = new LineData(dataSet);
        lineData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return Math.round(value)+"";
            }
        });
        mChart.setData(lineData);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);

        mChart.invalidate();
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


         final String[] strs = getXDays();
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return strs[(int) value];
            }

        };
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);


        YAxis leftyAxis = mChart.getAxisLeft();
        leftyAxis.setEnabled(false);



        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(true);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setEnabled(true);
        mChart.setPadding(30, 10, 10, 30);
        mChart.setDescription(null);
        mChart.setNoDataText("데이터가 없어요~!");
        mChart.animateY(1000, Easing.EasingOption.EaseInCubic);
        IMarker marker = new CustomMarkerView(getContext(),R.layout.custom_marker_view_layout);
        mChart.setMarker(marker);
    }

    public List<Entry> getDataFromDB() {
        Realm realm = Realm.getDefaultInstance();
        List<Entry> entries = new ArrayList<Entry>();
        //entries.add(new Entry(i, Math.abs(5 - i)));


        for(int i=0;i<X_COUNT;i++){
            //오늘부터 8일전까지의 puzzle 개수를 받아온다.

            //오늘꺼
            Calendar from = (Calendar)mToday.clone();
            from.add(Calendar.DAY_OF_MONTH,-i);
            Calendar to = (Calendar)mToday.clone();
            to.add(Calendar.DAY_OF_MONTH,1-i);

            int count = realm.where(Puzzle.class)
                    .greaterThanOrEqualTo(Puzzle.DATE_TO_MILLISECONDS,from.getTimeInMillis())
                    .lessThan(Puzzle.DATE_TO_MILLISECONDS,to.getTimeInMillis())
                    .findAll().size();

            entries.add(new Entry((X_COUNT-1)-i,count));
            //Log.d("###","count="+count);
        }

        for(int i=0;i< (int)(X_COUNT/(double)2);i++){
            Entry t1 = entries.get(i);
            Entry t2 = entries.get(X_COUNT-1-i);
            Entry temp =t1;
            entries.set(i,t2);
            entries.set(X_COUNT-1-i,temp);
        }

     /*   for(int i=0;i<X_COUNT;i++){
            Log.d("###",entries.get(i).toString());
        }*/

        return entries;
    }
    public String[] getXDays(){
        String[] xValues = new String[X_COUNT];
        Calendar today = (Calendar)mToday.clone();
        for(int i=0;i<X_COUNT;i++){
            xValues[X_COUNT-1-i] = String.valueOf(today.get(Calendar.DAY_OF_MONTH))+" ";
            today.add(Calendar.DAY_OF_MONTH,-1);
        }
        return xValues;
    }


}
