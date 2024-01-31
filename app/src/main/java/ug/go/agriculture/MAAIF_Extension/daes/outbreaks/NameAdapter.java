package ug.go.agriculture.MAAIF_Extension.daes.outbreaks;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
        TextView created = (TextView) listViewItem.findViewById(R.id.activitytype);
        TextView beneficiaries = (TextView) listViewItem.findViewById(R.id.beneficiaries);
        TextView ben_group = (TextView) listViewItem.findViewById(R.id.group);
        TextView ben_ref = (TextView) listViewItem.findViewById(R.id.ref);
        TextView ref_contact = (TextView) listViewItem.findViewById(R.id.ref_contact);
        TextView village = (TextView) listViewItem.findViewById(R.id.village);
        TextView district = (TextView) listViewItem.findViewById(R.id.district);
        TextView activitytype = (TextView) listViewItem.findViewById(R.id.activitytype);
        ImageView imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);
        btnPhotoUpload = (Button) listViewItem.findViewById(R.id.btnPhotoUpload);

        created.setVisibility(View.GONE);

        //getting the current name
        Name name = names.get(position);

        String entreprises = "Subcounty/Parish: "+name.getA3()+ "/"+ name.getA4();
        String createdes = " ";
        String topics = "District: "+name.getA1();
        String ben_groupss = "Entreprize: "+name.getA7();
        String ben_Refss = "Reference: "+name.getA11();
        String ben_Ref_phones = "Contact: "+name.getA12();
      //  String subcountys = "Subcounty/Village: "+name.getA6() +  " / "+name.getA7();;
      //  Integer bene = Integer.valueOf(name.getA11())+ Integer.valueOf(name.getA12());
        Integer bene = 0;
        String beneficiariess = "Village:  "+ name.getA4() ;
        String types =  name.getA20();
        String hh = "Rating: "+ name.getA16();

//        if(types.startsWith("0"))
//            activitytype.setText("Activity Type: Planned");
//        else
//            activitytype.setText("Activity Type: Unplanned");


        //setting the name to textview
        textViewName.setText(name.getA9());
        entreprise.setText(entreprises);
        topic.setText(topics);
        //created.setText(createdes);
        beneficiaries.setText(beneficiariess);
        ben_group.setText(ben_groupss);
        ben_ref.setText(ben_Refss);
        ref_contact.setText(ben_Ref_phones);
        village.setText(" ");
        district.setText(hh);

        if (name.getSynced() == 1) {
            btnPhotoUpload.setVisibility(View.VISIBLE);
        } else {
            btnPhotoUpload.setVisibility(View.GONE);
        }

        //Photo Upload
        btnPhotoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   Integer bene = Integer.valueOf(name.getA11())+ Integer.valueOf(name.getA12());
                String types =  name.getA20();
                Intent intent= new Intent(context, PhotoUpload.class);
                intent.putExtra("a1",name.getA1());
                intent.putExtra("a2",name.getA2());
                intent.putExtra("a3",name.getA3());
                intent.putExtra("a4",name.getA4());
                intent.putExtra("a5",name.getA5());
                intent.putExtra("a6",name.getA6());
                intent.putExtra("a7",name.getA7());
                intent.putExtra("a8",name.getA8());
                intent.putExtra("a9",name.getA8());
                intent.putExtra("a10",name.getA10());
                intent.putExtra("a11",name.getA11());
                intent.putExtra("a12",name.getA12());
                intent.putExtra("a13",name.getA13());
                intent.putExtra("a14",name.getA14());
                intent.putExtra("a15",name.getA15());
                intent.putExtra("a16",name.getA16());
                intent.putExtra("a17",name.getA17());
                intent.putExtra("a18",name.getA18());
                intent.putExtra("a19",name.getA19());
                intent.putExtra("a20",name.getA20());
                intent.putExtra("a21",name.getA21());
                intent.putExtra("a22",name.getA22());
                intent.putExtra("a23",name.getA23());
                intent.putExtra("a24",name.getA24());
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
