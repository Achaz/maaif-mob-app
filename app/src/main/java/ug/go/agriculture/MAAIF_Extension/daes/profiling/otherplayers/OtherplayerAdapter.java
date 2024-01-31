package ug.go.agriculture.MAAIF_Extension.daes.profiling.otherplayers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.daes.profiling.farmergroup.FarmerGroup;

/**
 * Created by Robert Muhereza
 */

public class OtherplayerAdapter extends ArrayAdapter<OtherPlayer> {

    //storing all the other players in the list
    private List<OtherPlayer> otherPlayers;

    //context object
    private Context context;

    //constructor
    public OtherplayerAdapter(Context context, int resource, List<OtherPlayer> otherPlayers) {
        super(context, resource, otherPlayers);
        this.context = context;
        this.otherPlayers = otherPlayers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview itmes
        View listViewItem = inflater.inflate(R.layout.farmergroup, null, true);
        TextView groupName = (TextView) listViewItem.findViewById(R.id.groupName);
        TextView category = (TextView) listViewItem.findViewById(R.id.category);
        TextView level_of_operation = (TextView) listViewItem.findViewById(R.id.level_of_operation);
        TextView contact_person_name = (TextView) listViewItem.findViewById(R.id.contact_person_name);
        TextView phone_number = (TextView) listViewItem.findViewById(R.id.phone_number);
        TextView email = (TextView) listViewItem.findViewById(R.id.email);
        TextView district = (TextView) listViewItem.findViewById(R.id.district);
        TextView subcounty = (TextView) listViewItem.findViewById(R.id.subcounty);
        TextView parish = (TextView) listViewItem.findViewById(R.id.parish);
        TextView village = (TextView) listViewItem.findViewById(R.id.village);
        TextView enterprise = (TextView) listViewItem.findViewById(R.id.enterprise);
        TextView is_registered = (TextView) listViewItem.findViewById(R.id.is_registered);
        TextView registration_number = (TextView) listViewItem.findViewById(R.id.registration_number);
        TextView group_members_count = (TextView) listViewItem.findViewById(R.id.group_members_count);
        TextView synced = (TextView) listViewItem.findViewById(R.id.is_synced);
        ImageView imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);

        //getting the current name
        OtherPlayer otherPlayer = otherPlayers.get(position);

        String farmer_group_name = otherPlayer.getA1();
        String group_category = "Category: " + otherPlayer.getA3();
        String group_loo = "Level of operation: " + otherPlayer.getA8();
        String cp_name = "Contact person name: " + otherPlayer.getA4();
        String cp_contact = "Phone number: " + otherPlayer.getA7();
        String cp_email = "Email: " + otherPlayer.getA5();
        String group_district  = "District: " + otherPlayer.getA10();
        String group_subcounty  = "Sub county: " + otherPlayer.getA11();
        String group_parish  = "Parish: " + otherPlayer.getA12();
        String group_village  = "Trading name: " + otherPlayer.getA2();
        String group_enterprise  = "Entreprise One: " + otherPlayer.getA14();
        String group_registartionID  = "Registration ID: " + otherPlayer.getA6();
        String group_member_count  = "Enterprise Two: " + otherPlayer.getA15();
        String group_registered  = "Enterprise Three: " + otherPlayer.getA16();
        String synced_status = "No";

        //setting the name to textview
        groupName.setText(farmer_group_name);
        category.setText(group_category);
        level_of_operation.setText(group_loo);
        contact_person_name.setText(cp_name);
        phone_number.setText(cp_contact);
        email.setText(cp_email);
        district.setText(group_district);
        subcounty.setText(group_subcounty);
        parish.setText(group_parish);
        village.setText(group_village);
        enterprise.setText(group_enterprise);
        registration_number.setText(group_registartionID);
        group_members_count.setText(group_member_count);
        is_registered.setText(group_registered);

        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        if (otherPlayer.getSynced() == 0)
        {
            imageViewStatus.setBackgroundResource(R.drawable.stopwatch);
        }
        else
        {
            imageViewStatus.setBackgroundResource(R.drawable.success);
            synced_status = "Yes";
        }
        String is_synced  = "Synced: " + synced_status;
        synced.setText(is_synced);

        return listViewItem;
    }
}
