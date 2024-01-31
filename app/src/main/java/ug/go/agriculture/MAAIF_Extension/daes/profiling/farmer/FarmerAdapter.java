package ug.go.agriculture.MAAIF_Extension.daes.profiling.farmer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;

/**
 * Created by Herbert Musoke on 1/27/2017.
 */

public class FarmerAdapter extends ArrayAdapter<Farmer> {

    //storing all the farmers in the list
    private List<Farmer> registeredFarmers;

    //context object
    private Context context;
    private SQLiteHandler FarmerDatabaseHelper;

    //constructor
    public FarmerAdapter(Context context, int resource, List<Farmer> Farmer) {
        super(context, resource, Farmer);
        this.context = context;
        this.registeredFarmers = Farmer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview items
        View listViewItem = inflater.inflate(R.layout.farmer_data, null, true);
        TextView name = (TextView) listViewItem.findViewById(R.id.farmer_name);
        TextView gender = (TextView) listViewItem.findViewById(R.id.gender);
        TextView contact = (TextView) listViewItem.findViewById(R.id.contact);
        TextView email = (TextView) listViewItem.findViewById(R.id.email);
        TextView category = (TextView) listViewItem.findViewById(R.id.category);
        TextView level_of_education = (TextView) listViewItem.findViewById(R.id.level_of_education);
        TextView primary_language = (TextView) listViewItem.findViewById(R.id.primary_language);
        TextView secondary_language = (TextView) listViewItem.findViewById(R.id.secondary_language);
        TextView district = (TextView) listViewItem.findViewById(R.id.district);
        TextView subcounty = (TextView) listViewItem.findViewById(R.id.subcounty);
        TextView parish = (TextView) listViewItem.findViewById(R.id.parish);
        TextView village = (TextView) listViewItem.findViewById(R.id.village);
        TextView synced = (TextView) listViewItem.findViewById(R.id.is_synced);
        TextView is_synced_reason = (TextView) listViewItem.findViewById(R.id.is_synced_reason);
        ImageView imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);
        Button deleteFarmerBtn = listViewItem.findViewById(R.id.btnDeleteFarmer);

        //getting the current name
        Farmer registeredFarmer = registeredFarmers.get(position);

        String a_name = registeredFarmer.getA1() + " " + registeredFarmer.getA2() ;
        String a_gender = "Gender: "+ registeredFarmer.getA4();
        String a_contact = "Phone Number: "+ registeredFarmer.getA7();
        String a_email = "Email: "+ registeredFarmer.getA5();
        String a_category = "Category: "+ registeredFarmer.getA3();
        String a_level_of_education = "Level of Education: "+ registeredFarmer.getA8();
        String a_primary_language = "Primary Language: "+ registeredFarmer.getA9();
        String a_secondary_language = "Secondary Language:  "+ registeredFarmer.getA9x();
        String a_district  = "District: "+ registeredFarmer.getA10();
        String a_subcounty  = "Sub county: "+ registeredFarmer.getA11();
        String a_parish  = "Parish: "+ registeredFarmer.getA12() ;
        String a_village  = "Village: "+ registeredFarmer.getA13() ;
        String synced_status = "No";
        String not_synced_reason = "Not synced reason: " + registeredFarmer.getReason();

        //setting the name to textview
        name.setText(a_name);
        gender.setText(a_gender);
        contact.setText(a_contact);
        email.setText(a_email);
        category.setText(a_category);
        level_of_education.setText(a_level_of_education);
        primary_language.setText(a_primary_language);
        secondary_language.setText(a_secondary_language);
        district.setText(a_district);
        subcounty.setText(a_subcounty);
        parish.setText(a_parish);
        village.setText(a_village);
        is_synced_reason.setText(not_synced_reason);

        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        if (registeredFarmer.getSynced() == 0)
        {
            imageViewStatus.setBackgroundResource(R.drawable.stopwatch);
            is_synced_reason.setVisibility(View.GONE);
            deleteFarmerBtn.setVisibility(View.GONE);
        }
        else if (registeredFarmer.getSynced() == 1)
        {
            imageViewStatus.setBackgroundResource(R.drawable.success);
            synced_status = "Yes";
            is_synced_reason.setVisibility(View.GONE);
            deleteFarmerBtn.setVisibility(View.GONE);
        }
        else if (registeredFarmer.getSynced() == 2)
        {
            imageViewStatus.setBackgroundResource(R.drawable.ic_cloud_off);
            synced_status = "Failed";
        }
        String is_synced  = "Synced: " + synced_status;
        synced.setText(is_synced);

        Button deleteButton = listViewItem.findViewById(R.id.btnDeleteFarmer);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the farmer ID of the clicked item
                int farmerId = registeredFarmer.getId();

                // Call the delete method in the database helper to delete the farmer record
                SQLiteHandler db = new SQLiteHandler(getContext());
                db.deleteFarmerProfile(farmerId);

                // Remove the farmer from the list and update the UI
                remove(registeredFarmer);
                notifyDataSetChanged();

                // Show a Toast message or perform any other action to notify the user
                Toast.makeText(getContext(), "Farmer deleted successfully", Toast.LENGTH_SHORT).show();
            }
        });


        return listViewItem;
    }

}
