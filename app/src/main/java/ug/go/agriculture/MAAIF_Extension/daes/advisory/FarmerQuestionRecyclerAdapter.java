package ug.go.agriculture.MAAIF_Extension.daes.advisory;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import ug.go.agriculture.MAAIF_Extension.R;

public class FarmerQuestionRecyclerAdapter extends RecyclerView.Adapter<FarmerQuestionRecyclerAdapter.MyViewHolder> {

    Context context;

    boolean showingfirst = true;
    int myPos = 0;
    public static final String IMAGES_UPLOAD_ROOT = "https://extension.agriculture.go.ug/";
    public static final String QUESTION_IMAGES = "https://extension.agriculture.go.ug/";

    private List<QuestionEntity> OfferList;


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView farmername, question, timestamp, answers;
        ImageView imgProfile, imgPost;

        public MyViewHolder(View view) {
            super(view);

            farmername       = (TextView) view.findViewById(R.id.detailsTextView);
            question  = (TextView) view.findViewById(R.id.nameTextView);
            timestamp   = (TextView) view.findViewById(R.id.dateTextView);
            answers    = (TextView) view.findViewById(R.id.commentsCountTextView);
            imgProfile  = (ImageView) view.findViewById(R.id.authorImageView);
            imgPost     = (ImageView) view.findViewById(R.id.postImageView);
        }
    }

    public FarmerQuestionRecyclerAdapter(Context context, List<QuestionEntity> offerList) {
        this.OfferList = offerList;
        this.context = context;
    }

    @Override
    public FarmerQuestionRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);

        return new FarmerQuestionRecyclerAdapter.MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final FarmerQuestionRecyclerAdapter.MyViewHolder holder, final int position) {

        final QuestionEntity lists = OfferList.get(position);
        holder.farmername.setText(lists.getFarmer());
        holder.question.setText(Html.fromHtml(lists.getBody()));
        holder.timestamp.setText(lists.getCreated_at());
        holder.answers.setText(lists.getResponses());

        //Author image
        String profile_url = String.valueOf("https://extension.agriculture.go.ug/images/users/user.png");
        Glide.with(context).load(profile_url).into(holder.imgProfile);

        if (!lists.getMedia_url().equals("")){
            //holder.imgPost.getLayoutParams().height = 200;
            String url = lists.getMedia_url();
        //Log.e("MEDIA_URL",url);
            Glide.with(context).load(url).into(holder.imgPost);
            //.apply(RequestOptions.overrideOf(200, 200)).into(holder.imgPost);
        }
        //Setting color indicators for unanswered questions
        DateTimeFormatter dateTimeFormatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        LocalDate today = LocalDate.now();
        LocalDate uploadDate = LocalDate.parse(lists.getCreated_at(), dateTimeFormatter);
        Period p = Period.between(uploadDate, today);
        long totalNumberOfDays = ChronoUnit.DAYS.between(uploadDate, today);

        if(Integer.parseInt(lists.getId()) == 0){
            if(totalNumberOfDays <= 1){
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.amber_50));
            }else if(totalNumberOfDays > 1 && totalNumberOfDays < 2){
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.amber_500));
            }else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.red_50));
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, QuestionDetailsActivity.class);
                intent.putExtra("id", OfferList.get(holder.getBindingAdapterPosition()).getId());
                intent.putExtra("farmer_id", lists.getId());
                context.startActivity(intent);
            }
        });


    }


    @Override
    public int getItemCount() {
        return OfferList.size();

    }


}
