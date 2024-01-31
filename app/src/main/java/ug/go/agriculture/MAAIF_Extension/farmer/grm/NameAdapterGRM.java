package ug.go.agriculture.MAAIF_Extension.farmer.grm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ug.go.agriculture.MAAIF_Extension.R;

/**
 * Created by Herbert on 6/27/2019.
 */

public class NameAdapterGRM extends ArrayAdapter<NameGRM> {

    //storing all the names in the list
    private List<NameGRM> names;

    //context object
    private Context context;

    //constructor
    public NameAdapterGRM(Context context, int resource, List<NameGRM> names) {
        super(context, resource, names);
        this.context = context;
        this.names = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview itmes
        View listViewItem = inflater.inflate(R.layout.names_grivences, null, true);
        TextView namez = (TextView) listViewItem.findViewById(R.id.name);
        TextView age = (TextView) listViewItem.findViewById(R.id.age);
        TextView gender= (TextView) listViewItem.findViewById(R.id.gender);
        TextView phone = (TextView) listViewItem.findViewById(R.id.phone);
        TextView gDate = (TextView) listViewItem.findViewById(R.id.gDate);
        TextView gNature = (TextView) listViewItem.findViewById(R.id.gNature);
        TextView gType = (TextView) listViewItem.findViewById(R.id.gType);
        TextView gReference= (TextView) listViewItem.findViewById(R.id.gReference);
        TextView gDescription= (TextView) listViewItem.findViewById(R.id.gDescription);
        ImageView imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);

        //getting the current name
        NameGRM name = names.get(position);

        String cName = "Name: "+name.getName();
        String cAge = "Age: "+name.getAge();
        String cGender = "Gender: "+name.getGender();
        String cPhone = "Phone: "+name.getPhone();
        String gDatez = "Date: "+name.getDate_of_grievance();
        String gNaturez = "Nature: "+name.getgNature();
        String gTypez = "Type: "+name.getgType();
        String gReferencez = "Reference: "+name.getRef_number();
        String gDescriptionz = "Description: "+name.getDescription();

        //setting the name to textview
        namez.setText(cName);
        age.setText(cAge);
        gender.setText(cGender);
        phone.setText(cPhone);
        gDate.setText(gDatez);
        gNature.setText(gNaturez);
        gType.setText(gTypez);
        gReference.setText(gReferencez);
        gDescription.setText(gDescriptionz);

        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        if (name.getSynced() == 0)
            imageViewStatus.setBackgroundResource(R.drawable.stopwatch);
        else
            imageViewStatus.setBackgroundResource(R.drawable.success);

        return listViewItem;
    }
}
