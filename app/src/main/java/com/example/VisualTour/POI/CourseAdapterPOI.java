package com.example.VisualTour.POI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.VisualTour.CourseModel;
import com.example.VisualTour.R;

import org.json.JSONException;

import java.util.List;

public class CourseAdapterPOI extends RecyclerView.Adapter<CourseAdapterPOI.Viewholder> {

    private Context context;
    private List<CourseModel> list;
    private  ItemClickListener clickListener;
    // Constructor
    public CourseAdapterPOI(List<CourseModel> list, ItemClickListener clickListener) {
        this.list = list;
        this.clickListener  = clickListener;
    }



    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);

    return new Viewholder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        CourseModel model = list.get(position);
        holder.Nome.setText(model.getCourse_name());
        holder.Tipo.setText("" + model.getCourse_rating());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    clickListener.onItemClick(list.get(position), (String) holder.Nome.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return list.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        private TextView Nome;
        private TextView Tipo;
        public Button bottone;
         RelativeLayout relativeLayout;

        public Viewholder(@NonNull View itemView ){
            super(itemView);
            Nome = itemView.findViewById(R.id.NomePerc);
            Tipo = itemView.findViewById(R.id.Tipo);
            bottone = itemView.findViewById(R.id.remove);

            relativeLayout = itemView.findViewById(R.id.idRVCourse);


        }
    }
    public interface  ItemClickListener {
        public void onItemClick(CourseModel dataModel,String id) throws JSONException;
    }
}