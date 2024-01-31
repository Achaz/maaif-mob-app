package ug.go.agriculture.MAAIF_Extension.daes.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ug.go.agriculture.MAAIF_Extension.R;

/**
 * Created by Belal on 1/27/2017.
 */

public class  NameAdapter extends ArrayAdapter<Name> {

    //storing all the names in the list
    private List<Name> names;

    //context object
    private Context context;

    //constructor
    public NameAdapter(Context context, int resource, List<Name> names) {
        super(context, resource, names);
        this.context = context;
        this.names = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview itmes
        View listViewItem = inflater.inflate(R.layout.weather, null, true);
        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        TextView topic = (TextView) listViewItem.findViewById(R.id.topic);
        TextView entreprise = (TextView) listViewItem.findViewById(R.id.entreprise);
        TextView beneficiaries = (TextView) listViewItem.findViewById(R.id.beneficiaries);
        TextView ben_group = (TextView) listViewItem.findViewById(R.id.group);
        TextView ben_ref = (TextView) listViewItem.findViewById(R.id.ref);
        TextView ref_contact = (TextView) listViewItem.findViewById(R.id.ref_contact);
        TextView village = (TextView) listViewItem.findViewById(R.id.village);
        TextView district = (TextView) listViewItem.findViewById(R.id.district);
        TextView activitytype = (TextView) listViewItem.findViewById(R.id.activitytype);
        TextView dayofweek = (Button) listViewItem.findViewById(R.id.btnDayOfWeek);
        ImageView imageIcon = (ImageView) listViewItem.findViewById(R.id.imageIcon);


        //getting the current name
        Name name = names.get(position);

        String entreprises = "Max Temp: "+name.getMaximum_temperature() + " \u00B0" + "C";
        String createdes = "Min Temp: "+name.getMinimum_temperature() + " \u00B0" + "C";
        String topics = "Rainfall Chance: "+name.getRainfall_chance();
        String desc = name.getDesc();
        String ben_groupss = "Average Temp: "+name.getAverage_temperature() + " \u00B0" + "C";
        String ben_Refss = "Windspeed Average: "+name.getWindspeed_average()+name.getWindspeed_units();
        String ben_Ref_phones = "Wind Direction: "+name.getWind_direction();
        String subcountys = "Cloud Cover: "+name.getCloudcover() ;
        String beneficiariess = "Soil Temperature:  "+ name.getSoil_temperature();
        String hh = "Rainfall Amount: "+ name.getRainfall_amount()+"mm";



        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date dt1 = null;
        try {
            dt1 = format1.parse(name.getForecast_date());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat format2 = new SimpleDateFormat("EEEE");
        String finalDay = format2.format(dt1);



        //Fix icons here
        String myStr = name.getIcon();
         if(myStr.startsWith("http://openweathermap.org/img/w/01d.png")) {
             imageIcon.setImageResource(R.drawable.icon_01d);
         }else if(myStr.startsWith("http://openweathermap.org/img/w/02d.png")) {
             imageIcon.setImageResource(R.drawable.icon_02d);
         }else if(myStr.startsWith("http://openweathermap.org/img/w/03d.png")) {
             imageIcon.setImageResource(R.drawable.icon_03d);
         }else if(myStr.startsWith("http://openweathermap.org/img/w/04d.png")) {
             imageIcon.setImageResource(R.drawable.icon_04d);
         }else if(myStr.startsWith("http://openweathermap.org/img/w/10d.png")) {
             imageIcon.setImageResource(R.drawable.icon_10d);
         }


        //setting the name to textview
        textViewName.setText(name.getForecast_date());
        entreprise.setText(createdes);
        topic.setText(desc);
       // created.setText(createdes);
        beneficiaries.setText(entreprises);
        ben_group.setText(ben_groupss);
        ben_ref.setText(ben_Refss);
        ref_contact.setText(ben_Ref_phones);
        village.setText(hh);
        district.setText(topics);
        activitytype.setText(" ");
        dayofweek.setText(finalDay);

        //if the synced status is 0 displaying
        //queued icon

        return listViewItem;
    }
}
