package ug.go.agriculture.MAAIF_Extension.daes.dairy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import ug.go.agriculture.MAAIF_Extension.R;

import java.util.List;

/**
 * Created by Belal on 1/27/2017.
 */

public class  NameAdapter extends ArrayAdapter<Name> {

    //storing all the names in the list
    private List<Name> names;

    //context object
    private Context context;
    private Button btnPhotoUpload;

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
        View listViewItem = inflater.inflate(R.layout.names_photoupload, null, true);
        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        TextView topic = (TextView) listViewItem.findViewById(R.id.topic);
        TextView entreprise = (TextView) listViewItem.findViewById(R.id.entreprise);
        //TextView created = (TextView) listViewItem.findViewById(R.id.created);
        TextView beneficiaries = (TextView) listViewItem.findViewById(R.id.beneficiaries);
        TextView ben_group = (TextView) listViewItem.findViewById(R.id.group);
        TextView ben_ref = (TextView) listViewItem.findViewById(R.id.ref);
        TextView ref_contact = (TextView) listViewItem.findViewById(R.id.ref_contact);
        TextView village = (TextView) listViewItem.findViewById(R.id.village);
        TextView district = (TextView) listViewItem.findViewById(R.id.district);
        TextView activitytype = (TextView) listViewItem.findViewById(R.id.activitytype);
        ImageView imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);
        btnPhotoUpload = (Button) listViewItem.findViewById(R.id.btnPhotoUpload);

        //getting the current name
        Name name = names.get(position);

        String entreprises = "Entreprize: "+name.getA3();
        String createdes = "Created: "+name.getA17();
        String topics = "Topic: "+name.getA1();
        String ben_groupss = "Group: "+name.getA8();
        String ben_Refss = "Reference: "+name.getA9();
        String ben_Ref_phones = "Contact: "+name.getA10();
        String subcountys = "Subcounty/Village: "+name.getA6() +  " / "+name.getA7();;
        Integer bene = Integer.valueOf(name.getA11())+ Integer.valueOf(name.getA12());
        String beneficiariess = "Total Beneficiaries:  "+ bene ;
        String types =  name.getA20();
        String hh = "District: "+ name.getA5();

        if(types.startsWith("0"))
            activitytype.setText("Activity Type: Planned");
        else
            activitytype.setText("Activity Type: Unplanned");


        //setting the name to textview
        textViewName.setText(name.getA2());
        entreprise.setText(entreprises);
        topic.setText(topics);
        //created.setText(createdes);
        beneficiaries.setText(beneficiariess);
        ben_group.setText(ben_groupss);
        ben_ref.setText(ben_Refss);
        ref_contact.setText(ben_Ref_phones);
        village.setText(subcountys);
        district.setText(hh);

        if (name.getSynced() == 1) {
            btnPhotoUpload.setVisibility(View.VISIBLE);
        } else {
            btnPhotoUpload.setVisibility(View.GONE);
        }

            //Photo upload
        btnPhotoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


             //   Integer bene = Integer.valueOf(name.getA11())+ Integer.valueOf(name.getA12());
                String types =  name.getA20();

                Intent intent= new Intent(context, PhotoUpload.class);
                intent.putExtra("id",name.getA19());
                intent.putExtra("entreprize",name.getA3());
                intent.putExtra("activity",name.getA2());
                intent.putExtra("created",name.getA17());
                intent.putExtra("topic",name.getA1());
                intent.putExtra("group",name.getA8());
                intent.putExtra("ref",name.getA9());
                intent.putExtra("contact",name.getA10());
                intent.putExtra("subcounty",name.getA6() +  " / "+name.getA7());
                intent.putExtra("ben",bene.toString());
                intent.putExtra("district",name.getA5());
                 if(types.startsWith("0")) {
                    intent.putExtra("type","Activity Type: Planned");
                }
                else {
                    intent.putExtra("type","Activity Type: Unplanned");
                }
                context.startActivity(intent);

            }

        });



        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        if (name.getStatus() == 0)
            imageViewStatus.setBackgroundResource(R.drawable.stopwatch);
        else
            imageViewStatus.setBackgroundResource(R.drawable.success);

        return listViewItem;
    }
}
