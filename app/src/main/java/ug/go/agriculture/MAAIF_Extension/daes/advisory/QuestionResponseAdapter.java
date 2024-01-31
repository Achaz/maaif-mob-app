package ug.go.agriculture.MAAIF_Extension.daes.advisory;

import static ug.go.agriculture.MAAIF_Extension.daes.advisory.FarmerQuestionRecyclerAdapter.IMAGES_UPLOAD_ROOT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ug.go.agriculture.MAAIF_Extension.R;

public class QuestionResponseAdapter extends RecyclerView.Adapter<QuestionResponseAdapter.MyViewHolder>{
    Context context;

    private List<QuestionResponseEntity> OfferList;
    private int count = 1;


    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView id, answer, timestamp, commenter;
        ImageView author_img;


        public MyViewHolder(View view) {
            super(view);

            answer = view.findViewById(R.id.answerText);
            timestamp = view.findViewById(R.id.dateCommented);
            commenter = view.findViewById(R.id.authorTextView);
            author_img = view.findViewById(R.id.avatarImageView);

        }

    }


    public QuestionResponseAdapter(Context context, List<QuestionResponseEntity> offerList) {
        this.OfferList = offerList;
        this.context = context;
    }

    @Override
    public QuestionResponseAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_response, parent, false);
        return new QuestionResponseAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final QuestionResponseAdapter.MyViewHolder holder, final int position) {

        final QuestionResponseEntity lists = OfferList.get(position);

        holder.answer.setText(lists.getResponse());
        holder.timestamp.setText(lists.getCreated_at());
        holder.commenter.setText(lists.getCommentor());

        if (lists.getId().equals("")){
            //holder.imgPost.getLayoutParams().height = 200;
            String url = IMAGES_UPLOAD_ROOT + String.valueOf(lists.getId());
            Glide.with(context).load(url).into(holder.author_img);
            //.apply(RequestOptions.overrideOf(200, 200)).into(holder.imgPost);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(context, PreviousResultsDetailActivity.class);
//                context.startActivity(intent);
            }
        });

        holder.commenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //farmer
//                if (lists.getAuthorRole().equals("5")){
//                    Intent intent = new Intent(context, FarmerDetailActivity.class);
//                    intent.putExtra("id", lists.getAuthorId());
//                    context.startActivity(intent);
//                }
//                //extension officer
//                if (lists.getAuthorRole().equals("3")){
//                    Intent intent = new Intent(context, ExtensionOfficerProfileActivity.class);
//                    intent.putExtra("id", lists.getAuthorId());
//                    context.startActivity(intent);
//                }
//                //expert
//                if (lists.getAuthorRole().equals("2")){
//
//                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return OfferList.size();

    }
}
