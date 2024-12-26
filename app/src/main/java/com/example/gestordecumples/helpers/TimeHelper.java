package com.example.gestordecumples.helpers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


public class TimeHelper {

    public static class DatePickerHelper extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private DatePickerDialog.OnDateSetListener onDateSetListener;

        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            Log.w("Fecha", day + " / " + (month+1) + " / " + year);

        }

        public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {

            this.onDateSetListener = onDateSetListener;

        }
    }

    public static class TimePickerHelper extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

        private TimePickerDialog.OnTimeSetListener listener;

        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int hora = c.get(Calendar.HOUR_OF_DAY);
            int minuto = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), listener, hora, minuto,true);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Log.w("Time","Hora: " + hourOfDay + " | Minutos: " + minute);
        }

        public void setOnTimeSetListener(TimePickerDialog.OnTimeSetListener listener){
            this.listener = listener;
        }
    }


}
